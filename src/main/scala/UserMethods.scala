import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: Seq[User])

object UserMethods {
  final case class ActionPerformed(description: String)
  final case object GetUsers
  final case class CreateUser(user: User)
  final case class GetUser(name: String)
  final case class DeleteUser(name: String)

  //def props: Props = Props[UserMethods]
}

class UserMethods {

  import UserMethods._

  var users = Set.empty[User]

  /*def receive: Receive = {
    case GetUsers =>
    //sender() ! Users(users.toSeq)
    case CreateUser(user) =>
    //users += user
    //sender() ! ActionPerformed(s"User ${user.name} created.")
    case GetUser(name) =>
    //sender() ! users.find(_.name == name)
    case DeleteUser(name) =>
    //users.find(_.name == name) foreach { user => users -= user }
    //sender() ! ActionPerformed(s"User ${name} deleted.")
  }*/
}