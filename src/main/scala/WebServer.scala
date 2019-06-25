import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import akka.stream.ActorMaterializer
import rest.{GiveawayRoutes, TipRoutes, UserRoutes}
import utils._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App with RouteConcatenation {
  // configuring modules for application, cake pattern for DI
  val modules = new ConfigurationModuleImpl  with ActorModuleImpl with PersistenceModuleImpl
  implicit val system = modules.system
  implicit val materializer = ActorMaterializer()
  implicit val ec = modules.system.dispatcher

  import modules.profile.api._
  Await.result(modules.db.run(modules.usersDal.tableQuery.schema.createIfNotExists), Duration.Inf)
  Await.result(modules.db.run(modules.tipsDal.tableQuery.schema.createIfNotExists), Duration.Inf)
  Await.result(modules.db.run(modules.giveawaysDal.tableQuery.schema.createIfNotExists), Duration.Inf)
  Await.result(modules.db.run(modules.userGiveawaysDal.tableQuery.schema.createIfNotExists), Duration.Inf)

  val bindingFuture = Http().bindAndHandle(
    cors()(new UserRoutes(modules).routes ~
      new TipRoutes(modules).routes ~
      new GiveawayRoutes(modules).routes ~
      SwaggerDocService.assets ~
      SwaggerDocService.routes), "localhost", 8080)

  println(s"Swagger online on http://localhost:8080/swagger/")
}
