package entities

import persistence.entities.{SimpleTip, SimpleUser, Tip, User, UserId, Poll}
import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User)
  implicit val userIdFormat = jsonFormat1(UserId)
  implicit val simpleUserFormat = jsonFormat3(SimpleUser)
  implicit val tipFormat = jsonFormat3(Tip)
  implicit val simpleTipFormat = jsonFormat2(SimpleTip)
  implicit val PollFormat = jsonFormat4(Poll)
}
