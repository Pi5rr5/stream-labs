package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol
import persistence.entities.{Poll}
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
  def PollsGetRoute = path("polls") {
    get {
      onComplete(modules.pollsDal.findAll()) {
        case Success(polls) => complete(polls)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  val routes: Route = PollsGetRoute
}
