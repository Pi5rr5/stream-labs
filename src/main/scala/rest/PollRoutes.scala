package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol
import persistence.entities.{Poll, PollParticipate, SimplePoll}
import utils.{ActorModule, Configuration, DbModule, PersistenceModule}
import JsonProtocol._
import SprayJsonSupport._

import scala.util.{Failure, Success}
import io.swagger.annotations._
import javax.ws.rs.Path

@Path("/polls")
@Api(value = "/polls", produces = "application/json")
class PollRoutes(modules: Configuration with PersistenceModule with DbModule with ActorModule) extends Directives {

  import modules.executeOperation
  import modules.system.dispatcher

  @ApiOperation(value = "Return all polls", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Polls", response = classOf[Poll]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def pollsGetRoute: Route = path("polls") {
    get {
      onComplete(modules.pollsDal.findAll()) {
        case Success(polls) => complete(polls)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @ApiOperation(value = "New poll", notes = "", nickname = "", httpMethod = "POST", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Poll Object", required = true,
      dataType = "persistence.entities.SimplePoll", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Created")
  ))
  def pollsPostRoute: Route = path("polls") {
    post {
      entity(as[SimplePoll]) { pollToInsert =>
        onComplete(modules.pollsDal.save(Poll(None, pollToInsert.question, pollToInsert.label1, 0, pollToInsert.label2, 0))) {
          case Success(poll) => complete(Created, poll)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @ApiOperation(value = "Participate to a poll", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Participation poll Object", required = true,
      dataType = "persistence.entities.PollParticipate", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 404, message = "Not Found"),
    new ApiResponse(code = 200, message = "Participation validated")
  ))
  def pollsPatchRoute: Route = path("polls") {
    patch {
      entity(as[PollParticipate]) { pollToUpdate =>
        onComplete(modules.pollsDal.findOne(pollToUpdate.id)) {
          case Success(userOpt) => userOpt match {
            case Some(poll) =>
              validate(
                (pollToUpdate.option1 == 0 || pollToUpdate.option2 == 0) && (pollToUpdate.option1 == 1 || pollToUpdate.option2 == 1)
                , s"{ error: 'Require one and only one response !' }") {
                onComplete(modules.pollsDal.update(Poll(poll.id, poll.question, poll.label1, poll.option1 + pollToUpdate.option1, poll.label2, poll.option2 + pollToUpdate.option2))) {
                  case Success(updatedPoll) => complete(updatedPoll)
                  case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
                }
              }
            case None => complete(NotFound, s"""{ error: "The poll ${pollToUpdate.id} doesn't exist !" }""")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/{id}/result")
  @ApiOperation(value = "Return poll result", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "Poll id", required = true, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Poll", response = classOf[Poll]),
    new ApiResponse(code = 404, message = "Poll Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def pollGetResultRoute: Route = path("polls" / IntNumber / "result") { id =>
    get {
      onComplete(modules.pollsDal.findOne(id)) {
        case Success(pollOpt) => pollOpt match {
          case Some(poll) => complete(poll)
          case None => complete(NotFound, s"""{ error: "The poll $id doesn't exist !" }""")
        }
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  val routes: Route = pollsGetRoute ~ pollsPostRoute ~ pollsPatchRoute ~ pollGetResultRoute
}
