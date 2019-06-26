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
  def usersGetRoute: Route = path("users") {
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
    new ApiResponse(code = 404, message = "Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userGetRoute: Route = path("users" / IntNumber) { id =>
    get {
      onComplete(modules.usersDal.findOne(id)) {
        case Success(userOpt) => userOpt match {
          case Some(user) => complete(user)
          case None => complete(NotFound, s"{ error: 'The user $id doesn't exist !' }")
        }
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @ApiOperation(value = "Add User", notes = "", nickname = "", httpMethod = "POST", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User Object", required = true,
      dataType = "persistence.entities.SimpleUser", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Created", response = classOf[User]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userPostRoute: Route = path("users") {
    post {
      entity(as[SimpleUser]) { userToInsert =>
        onComplete(modules.usersDal.save(User(None, Option(userToInsert.pseudo), Option(userToInsert.sub), Option(userToInsert.blacklist)))) {
          case Success(user) => complete(Created, user)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Update User", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "User Id", required = false, dataType = "int", paramType = "path"),
    new ApiImplicitParam(name = "body", value = "User Object", required = true,
      dataType = "persistence.entities.SimpleUser", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 200, message = "Updated", response = classOf[User]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userPatchRoute: Route = path("users" / IntNumber) { id =>
    patch {
      onComplete(modules.usersDal.findOne(id)) {
        case Success(userOpt) => userOpt match {
          case Some(_) =>
            entity(as[SimpleUser]) { userToUpdate =>
              onComplete(modules.usersDal.update(User(Option(id), Option(userToUpdate.pseudo), Option(userToUpdate.sub), Option(userToUpdate.blacklist)))) {
                case Success(user) => complete(user)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            }
          case None => complete(NotFound, s"{ error: 'The user $id doesn't exist !' }")
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Delete User", notes = "", nickname = "", httpMethod = "DELETE", produces = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "User Id", required = false, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 404, message = "Not Found"),
    new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userDeleteRoute: Route = path("users" / IntNumber) { id =>
    delete {
      onComplete(modules.usersDal.findOne(id)) {
        case Success(userOpt) => userOpt match {
          case Some(_) =>
            onComplete(modules.usersDal.delete(User(Option(id), None, None, None))) {
              case Success(_) => complete(NoContent)
              case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
            }
          case None => complete(NotFound, s"{ error: 'The user $id doesn't exist !' }")
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
  def usersGetBlacklistedRoute: Route = path("users" / "blacklist") {
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
    new ApiResponse(code = 201, message = "Blacklisted", response = classOf[User]),
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def usersBlacklistRoute: Route = path("users" / "blacklist") {
    patch {
      entity(as[UserId]) { userToUpdate =>
        onComplete(modules.usersDal.findOne(userToUpdate.user_id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) =>
              onComplete(modules.usersDal.update(User(user.id, user.pseudo, user.sub, Option(1)))) {
                case Success(_) => complete(user)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            case None => complete(NotFound, s"{ error: 'The user ${userToUpdate.user_id} doesn't exist !' }")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/unblacklist")
  @ApiOperation(value = "Unblacklist user", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User id Object", required = true,
      dataType = "persistence.entities.UserId", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Unblacklisted", response = classOf[User]),
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def usersUnblacklistRoute: Route = path("users" / "unblacklist") {
    patch {
      entity(as[UserId]) { userToUpdate =>
        onComplete(modules.usersDal.findOne(userToUpdate.user_id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) =>
              onComplete(modules.usersDal.update(User(user.id, user.pseudo, user.sub, Option(0)))) {
                case Success(_) => complete(user)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            case None => complete(NotFound, s"{ error: 'The user ${userToUpdate.user_id}  doesn't exist !' }")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/subs")
  @ApiOperation(value = "Return all subs users", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return subscribers", response = classOf[User]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def usersGetSubsRoute: Route = path("users" / "subs") {
    get {
      onComplete(modules.usersDal.findAll()) {
        case Success(users) => complete(users.filter(user => user.sub.get > 0))
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @Path("/subs")
  @ApiOperation(value = "sub user", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User id Object", required = true,
      dataType = "persistence.entities.UserId", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Subscribed", response = classOf[User]),
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userPatchSubRoute: Route = path("users" / "subs") {
    patch {
      entity(as[UserId]) { userToUpdate =>
        onComplete(modules.usersDal.findOne(userToUpdate.user_id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) =>
              onComplete(modules.usersDal.update(User(user.id, user.pseudo, Option(1), user.blacklist))) {
                case Success(_) => complete(user)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            case None => complete(NotFound, s"""{ error: "The user ${userToUpdate.user_id} doesn't exist !" }""")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/unsubs")
  @ApiOperation(value = "unsubs user", notes = "", nickname = "", httpMethod = "PATCH", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "User id Object", required = true,
      dataType = "persistence.entities.UserId", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Unsubscribed", response = classOf[User]),
    new ApiResponse(code = 404, message = "Not found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def userPatchUnsubRoute: Route = path("users" / "unsubs") {
    patch {
      entity(as[UserId]) { userToUpdate =>
        onComplete(modules.usersDal.findOne(userToUpdate.user_id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) =>
              onComplete(modules.usersDal.update(User(user.id, user.pseudo, Option(0), user.blacklist))) {
                case Success(_) => complete(user)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            case None => complete(NotFound, s"""{ error: "The user ${userToUpdate.user_id} doesn't exist !" }""")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  val routes: Route = usersGetRoute ~ userGetRoute ~ userPostRoute ~ userPatchRoute ~ userDeleteRoute ~ usersGetBlacklistedRoute ~ usersBlacklistRoute ~ usersUnblacklistRoute ~ usersGetSubsRoute ~ userPatchSubRoute ~ userPatchUnsubRoute
}

