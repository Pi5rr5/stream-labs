package utils

import akka.http.scaladsl.model.StatusCodes
import com.github.swagger.akka._
import com.github.swagger.akka.model.Info
import rest.{GiveawayRoutes, PollRoutes, TipRoutes, UserRoutes}
import scala.collection.immutable.Set

object SwaggerDocService extends SwaggerHttpService  {
  override val apiClasses: Set[Class[_]] = Set(classOf[UserRoutes], classOf[TipRoutes], classOf[PollRoutes], classOf[GiveawayRoutes])
  override val host = "localhost:8080"
  override val info = Info(version = "2.0")

  def assets = pathPrefix("swagger") {
    getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }
}