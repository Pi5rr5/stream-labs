package streamlabs.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{concat, pathEnd, pathPrefix, rejectEmptyResponse}
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.FutureDirectives._
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.MarshallingDirectives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.Future
import streamlabs.methods.UserMethods._
import streamlabs.methods.User
import streamlabs.methods.Users
import streamlabs.utils.JsonSupport


trait UserRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[UserRoutes])

  def userMethods: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val userRoutes: Route =
  pathPrefix("users") {
    concat(
      pathEnd {
        concat(
          get {
            val users: Future[Users] = (userMethods ? GetUsers).mapTo[Users]
            complete(users)
          },
          post {
            entity(as[User]) { user =>
              val userCreated: Future[ActionPerformed] =
                (userMethods ? CreateUser(user)).mapTo[ActionPerformed]
              onSuccess(userCreated) { performed =>
                log.info("Created user [{}]: {}", user.pseudo, performed.description)
                complete((StatusCodes.Created, performed))
              }
            }
            //complete("post")
          }
        )
      },
      path(Segment) { id =>
        concat(
          get {
            val maybeUser: Future[Option[User]] =
              (userMethods ? GetUser(id.toInt)).mapTo[Option[User]]
            rejectEmptyResponse {
              complete(maybeUser)
            }
          },
          delete {
            /*val userDeleted: Future[ActionPerformed] =
              (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
            onSuccess(userDeleted) { performed =>
              log.info("Deleted user [{}]: {}", name, performed.description)
              complete((StatusCodes.OK, performed))
            }*/
            complete("delete")
          }
        )
      }
    )
  }
}
