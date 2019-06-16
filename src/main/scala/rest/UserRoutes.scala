package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol
import persistence.entities.{SimpleUser, User, UserId}
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

  @ApiOperation(value = "Return all Users", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Users", response = classOf[User]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def usersGetRoute = path("users") {
    get {
      onComplete(modules.usersDal.findAll()) {
        case Success(users) => complete(users)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Return User", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
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
      validate(id > 0, s"{ error: 'The supplier id should be greater than zero !' }") {
        onComplete(modules.usersDal.findOne(id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) => complete(user)
            case None => complete(NotFound, s"{ error: 'The user ${id} doesn't exist !' }")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @ApiOperation(value = "Add User", notes = "", nickname = "", httpMethod = "POST", produces = "application/json")
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
        onComplete(modules.usersDal.save(User(None, Option(userToInsert.pseudo), Option(userToInsert.sub), Option(userToInsert.blacklist)))) {
          case Success(user) => complete(user)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @ApiOperation(value = "Update User", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User Object", required = true,
      dataType = "persistence.entities.User", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Updated")
  ))
  def userPatchRoute = path("users") {
    patch {
      entity(as[User]) { userToUpdate =>
        onComplete(modules.usersDal.update(userToUpdate)) {
          case Success(user) => complete(user)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @ApiOperation(value = "Delete User", notes = "", nickname = "", httpMethod = "DELETE", produces = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User Object", required = true,
      dataType = "persistence.entities.UserId", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Deleted")
  ))
  def userDeleteRoute = path("users") {
    delete {
      entity(as[UserId]) { userIdToDelete =>
        onComplete(modules.usersDal.delete(User(Option(userIdToDelete.id), None, None, None))) {
          case Success(_) => complete(OK)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/blacklist")
  @ApiOperation(value = "Return all blacklisted users", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return blacklisted users", response = classOf[User]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def usersGetBlacklistedRoute = path("users" / "blacklist") {
    get {
      onComplete(modules.usersDal.findAll()) {
        case Success(users) => complete(users.filter(user => user.blacklist.get > 0))
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @Path("/blacklist")
  @ApiOperation(value = "Blacklist user", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User id Object", required = true,
      dataType = "persistence.entities.UserId", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Blacklist users"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def usersBlacklistRoute = path("users" / "blacklist") {
    patch {
      entity(as[UserId]) { userToUpdate =>
        onComplete(modules.usersDal.findOne(userToUpdate.id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) => {
              onComplete(modules.usersDal.update(User(user.id, user.pseudo, user.sub, Option(1)))) {
                case Success(user) => complete(user)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            }
            case None => complete(NotFound, s"{ error: 'The user ${userToUpdate} doesn't exist !' }")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }


  val routes: Route = usersGetRoute ~ userGetRoute ~ userPostRoute ~ userPatchRoute ~ userDeleteRoute ~ usersGetBlacklistedRoute ~ usersBlacklistRoute

}

