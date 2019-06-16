package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import entities.JsonProtocol._
import io.swagger.annotations._
import javax.ws.rs.Path
import persistence.entities.{Tip}
import utils.{ActorModule, Configuration, DbModule, PersistenceModule}

import scala.util.{Failure, Success}

@Path("/tips")
@Api(value = "/tips", produces = "application/json")
class TipRoutes(modules: Configuration with PersistenceModule with DbModule with ActorModule) extends Directives {

  import modules.executeOperation
  import modules.system.dispatcher

  @ApiOperation(value = "Return all Users", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return Tips", response = classOf[Tip]),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def tipsGetRoute = path("tips") {
    get {
      onComplete(modules.usersDal.findAll()) {
        case Success(tips) => complete(tips)
        case Failure(ex) => complete(InternalServerError, s"{ error: 'An error occurred: ${ex.getMessage}' }")
      }
    }
  }

  val routes: Route = tipsGetRoute
}

