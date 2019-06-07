import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import utils.SQLiteHelpers
import utils.FromMap.to

import scala.io.StdIn

object WebServer {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  case class User(id: Int, pseudo: String, sub: Int, blacklist: Int)

  case class Users(vec: Vector[User])

  implicit val userFormat = jsonFormat4(User)
  implicit val usersFormat = jsonFormat1(Users)

  def main(args: Array[String]) {

    val url = s"""jdbc:sqlite:${args(0)}"""

    val route: Route =
      get {
        pathPrefix("users") {
          val req = SQLiteHelpers.request(url, "SELECT * FROM users", Seq("id", "pseudo", "sub", "blacklist"))
          //todo case id or no id
          req match {
            case Some(r) => val values = r.flatMap(v => to[User].from(v))
              complete(values)
            case None => complete("error")
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ ⇒ system.terminate())
  }
}