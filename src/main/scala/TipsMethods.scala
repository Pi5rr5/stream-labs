import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object TipsMethods {
  def tipsRoutes(): Route =
    get {
      pathPrefix("pong") {
        complete(getTips())
      }
    }

  def getTips(): String =
    "PING!"
}
