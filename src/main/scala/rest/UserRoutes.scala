package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol
import persistence.entities.{SimpleUser, User}
import utils.{ActorModule, Configuration, DbModule, PersistenceModule}
import JsonProtocol._
import SprayJsonSupport._

import scala.util.{Failure, Success}
import io.swagger.annotations._
import javax.ws.rs.Path

@Path("/users")
@Api(value = "/users", produces = "application/json")
class UserRoutes(modules: Configuration with PersistenceModule with DbModule with ActorModule) extends Directives {
  import modules.executeOperation
  import modules.system.dispatcher

  @Path("/{id}")
  @ApiOperation(value = "Return User", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "User Id", required = false, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return User", response = classOf[User]),
    new ApiResponse(code = 400, message = "The User id should be greater than zero"),
    new ApiResponse(code = 404, message = "Return User Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userGetRoute = path("users" / IntNumber) { (id) =>
    get {
      validate(id > 0, "The supplier id should be greater than zero") {

        onComplete(modules.usersDal.findOne(id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) => complete(user)
            case None => complete(NotFound, s"The user doesn't exist")
          }
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }

  @ApiOperation(value = "Add User", notes = "", nickname = "", httpMethod = "POST", produces = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User Object", required = true,
      dataType = "persistence.entities.SimpleUser", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def userPostRoute = path("users") {
    post {
      entity(as[SimpleUser]) { userToInsert =>
        onComplete(modules.usersDal.save(User(None, userToInsert.pseudo, userToInsert.sub, userToInsert.blacklist))) {
          case Success(_) => complete(Created)
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }

  val routes: Route = userPostRoute ~ userGetRoute

}

