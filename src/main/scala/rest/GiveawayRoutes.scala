package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol._
import io.swagger.annotations._
import javax.ws.rs.Path
import persistence.entities.{Giveaway, SimpleGiveaway, SimpleUserGiveaway, UserGiveaway, UserGiveawayRepository}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.{ActorModule, Configuration, DbModule, PersistenceModule}

import scala.util.{Failure, Success}

@Path("/giveaways")
@Api(value = "/giveaways", produces = "application/json")
class GiveawayRoutes(modules: Configuration with PersistenceModule with DbModule with ActorModule) extends Directives {

  import modules.executeOperation
  import modules.system.dispatcher

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("streamlabs")
  implicit val profile: JdbcProfile = dbConfig.profile
  val userGiveawaysDal = new UserGiveawayRepository(profile)

  @ApiOperation(value = "Return all Giveaways", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Giveaways", response = classOf[Giveaway]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def giveawaysGetRoute: Route = path("giveaways") {
    get {
      onComplete(modules.giveawaysDal.findAll()) {
        case Success(giveaways) => complete(giveaways)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @ApiOperation(value = "Add Giveaway", notes = "", nickname = "", httpMethod = "POST", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Giveaway Object", required = true,
      dataType = "persistence.entities.SimpleGiveaway", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def givewayPostRoute: Route = path("giveaways") {
    post {
      entity(as[SimpleGiveaway]) { giveawayToInsert =>
        onComplete(modules.giveawaysDal.save(Giveaway(None, Option(giveawayToInsert.description)))) {
          case Success(giveaway) => complete(Created, giveaway)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Delete Giveaway", notes = "", nickname = "", httpMethod = "DELETE", produces = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "Giveaway Id", required = false, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 203, message = "Entity Deleted"),
    new ApiResponse(code = 404, message = "Giveaway Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def giveawayDeleteRoute: Route = path("giveaways" / IntNumber) { id =>
    delete {
      onComplete(modules.giveawaysDal.delete(Giveaway(Option(id), None))) {
        case Success(_) => complete(NoContent)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @Path("/participate")
  @ApiOperation(value = "Participate to giveaway", notes = "", nickname = "", httpMethod = "POST", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "UserGiveaway Object", required = true,
      dataType = "persistence.entities.SimpleUserGiveaway", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def userGiveawayPostRoute: Route = path("giveaways" / "participate") {
    post {
      entity(as[SimpleUserGiveaway]) { userGiveawayToInsert =>
        onComplete(modules.usersDal.findOne(userGiveawayToInsert.user_id)) {
          case Success(userOpt) => userOpt match {
            case Some(user) =>
              validate(user.blacklist == Option(0), s"{ error: 'The user ${userGiveawayToInsert.user_id} is blacklisted !' }") {
                onComplete(modules.userGiveawaysDal.save(UserGiveaway(None, Option(userGiveawayToInsert.giveaway_id), Option(userGiveawayToInsert.user_id)))) {
                  case Success(userGiveaway) => complete(Created, userGiveaway)
                  case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
                }
              }
          }
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Return Giveway", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "Giveway Id", required = false, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Giveaway", response = classOf[Giveaway]),
    new ApiResponse(code = 400, message = "The Giveaway id should be greater than zero"),
    new ApiResponse(code = 404, message = "Return Giveaway Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def giveawayGetRoute: Route = path("giveaways" / IntNumber) { id =>
    get {
      onComplete(modules.giveawaysDal.findOne(id)) {
        case Success(giveawayOpt) => giveawayOpt match {
          case Some(giveaway) => complete(giveaway)
          case None => complete(NotFound, s"""{ error: "The giveaway $id doesn't exist !" }""")
        }
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  @Path("/{id}/draw")
  @ApiOperation(value = "Return a drawn user", notes = "", nickname = "", httpMethod = "GET", produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "Giveway Id", required = false, dataType = "int", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return a drawn user", response = classOf[Giveaway]),
    new ApiResponse(code = 400, message = "The Giveaway id should be greater than zero"),
    new ApiResponse(code = 404, message = "Return Giveaway Not Found"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def drawGetRoute: Route = path("giveaways" / IntNumber / "draw") { id =>
    get {
      onComplete(userGiveawaysDal.draw(id)) {
        case Success(user) => complete(user)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  val routes: Route = giveawaysGetRoute ~ givewayPostRoute ~ giveawayDeleteRoute ~ userGiveawayPostRoute ~ giveawayGetRoute ~ drawGetRoute
}

