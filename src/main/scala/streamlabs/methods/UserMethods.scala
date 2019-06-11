package streamlabs.methods

import streamlabs.utils.FromMap.to

import akka.actor.{Actor, ActorLogging, Props}
import streamlabs.utils.SQLiteHelpers

final case class User(id: Option[Int], pseudo: String, sub: Int, blacklist: Int)

final case class Users(users: Seq[User])

object UserMethods {

  final case class ActionPerformed(description: String)

  final case object GetUsers

  final case class CreateUser(user: User)

  final case class GetUser(id: Int)

  final case class DeleteUser(id: Int)

  def props: Props = Props[UserMethods]
}

class UserMethods extends Actor with ActorLogging {

  import UserMethods._

  //todo provide url from config file
  val url =
    s"""jdbc:sqlite:/home/pierre/Documents/stream-labs/src/main/resources/stream-labs.db"""

  def receive: Receive = {
    case GetUsers =>
      val req = SQLiteHelpers.request(url, "SELECT * FROM users", Seq("id", "pseudo", "sub", "blacklist"))
      req match {
        case Some(result) =>
          sender() ! Users(result.flatMap(values => to[User].from(values)))
        case None =>
          sender() ! None
      }
    case CreateUser(user) =>
      SQLiteHelpers.request(url, s"""INSERT INTO users (pseudo, sub, blacklist) VALUES ("${user.pseudo}", ${user.sub}, ${user.blacklist})""", Seq("id"))
      sender() ! ActionPerformed(s"User ${user.pseudo} created.")
    case GetUser(id) =>
      val req = SQLiteHelpers.request(url, "SELECT * FROM users", Seq("id", "pseudo", "sub", "blacklist"))
      req match {
        case Some(result) =>
          sender() ! result.flatMap(values => to[User].from(values)).find(user => user.id match {
            case x:Some[Int] => x.get == id
            case _ => false
          })
        case None =>
          sender() ! None
      }
    case DeleteUser(id) =>
    //users.find(_.name == name) foreach { user => users -= user }
    //sender() ! ActionPerformed(s"User ${name} deleted.")
  }
}