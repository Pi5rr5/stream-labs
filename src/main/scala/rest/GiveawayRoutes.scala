package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol._
import io.swagger.annotations._
import javax.ws.rs.Path
import persistence.entities.{Giveaway, GiveawayRepository, SimpleGiveaway, SimpleTip, Tip}
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
  val giveawaysDal = new GiveawayRepository(profile)

  @ApiOperation(value = "Return all Giveaways", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Giveaways", response = classOf[Giveaway]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def giveawaysGetRoute: Route = path("giveaways") {
    get {
      onComplete(giveawaysDal.getGiveaways()) {
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
  def givewayPostRoute = path("giveaways") {
    post {
      entity(as[SimpleGiveaway]) { giveawayToInsert =>
        onComplete(modules.giveawaysDal.save(Giveaway(None, Option(giveawayToInsert.user_id), Option(giveawayToInsert.description)))) {
          case Success(giveaway) => complete(giveaway)
          case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
        }
      }
    }
  }

  val routes: Route = giveawaysGetRoute ~ givewayPostRoute
}

