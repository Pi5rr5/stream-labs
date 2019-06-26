package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol
import persistence.entities.{SimpleTip, Tip, TipRepository, User}
import utils.{ActorModule, Configuration, DbModule, PersistenceModule}
import JsonProtocol._
import SprayJsonSupport._

import scala.util.{Failure, Success}
import io.swagger.annotations._
import javax.ws.rs.Path
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

@Path("/tips")
@Api(value = "/tips", produces = "application/json")
class TipRoutes(modules: Configuration with PersistenceModule with DbModule with ActorModule) extends Directives {

  import modules.executeOperation
  import modules.system.dispatcher

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("streamlabs")
  implicit val profile: JdbcProfile = dbConfig.profile
  val tipsDal = new TipRepository(profile)

  @ApiOperation(value = "Return all Tips", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Tips", response = classOf[Tip]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def tipsGetRoute: Route = path("tips") {
    get {
      onComplete(modules.tipsDal.findAll()) {
        case Success(tips) => complete(tips)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @ApiOperation(value = "Add Tip", notes = "", nickname = "", httpMethod = "POST", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Tip Object", required = true,
      dataType = "persistence.entities.SimpleTip", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def tipPostRoute: Route = path("tips") {
    post {
      entity(as[SimpleTip]) { tipToInsert =>
        onComplete(modules.usersDal.findOne(tipToInsert.user_id)) {
          case Success(userOpt) => userOpt match {
            case Some(_) =>
              onComplete(modules.tipsDal.save(Tip(None, Option(tipToInsert.user_id), Option(tipToInsert.amount)))) {
                case Success(tip) => complete(tip)
                case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
              }
            case None => complete(NotFound, s"""{ error: "The user ${tipToInsert.user_id} doesn't exist !" }""")
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Delete Tip", notes = "", nickname = "", httpMethod = "DELETE", produces = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "Tip Id", required = false, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Entity Deleted"),
    new ApiResponse(code = 404, message = "Tip Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def tipDeleteRoute: Route = path("tips" / IntNumber) { id =>
    delete {
      onComplete(modules.tipsDal.delete(Tip(Option(id), None, None))) {
        case Success(_) => complete(OK)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @Path("/users")
  @ApiOperation(value = "Return donators", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Users", response = classOf[User]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def donatorsGetRoute: Route = path("tips" / "users") {
    get {
      onComplete(tipsDal.getDonators()) {
        case Success(users) => complete(users)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  val routes: Route = tipsGetRoute ~ tipPostRoute ~ tipDeleteRoute ~ donatorsGetRoute
}

