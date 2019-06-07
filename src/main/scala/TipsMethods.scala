import akka.http.scaladsl.server.Directives.{complete, get, path}

object TipRoutes {
  val routes =
    get {
      path("pong") {
        complete("PING!")
      }
    }
}
