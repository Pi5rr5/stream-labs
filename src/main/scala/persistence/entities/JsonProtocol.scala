package entities

import persistence.entities.{SimpleUserGiveaway, UserGiveaway, SimpleGiveaway, Giveaway, SimpleTip, SimpleUser, Tip, User, UserId}
import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User)
  implicit val userIdFormat = jsonFormat1(UserId)
  implicit val simpleUserFormat = jsonFormat3(SimpleUser)
  implicit val tipFormat = jsonFormat3(Tip)
  implicit val simpleTipFormat = jsonFormat2(SimpleTip)
  implicit val giveawayFormat = jsonFormat3(Giveaway)
  implicit val simpleGiveAwayFormat = jsonFormat2(SimpleGiveaway)
  implicit val userGiveawayFormat = jsonFormat3(UserGiveaway)
  implicit val simpleUserGiveAwayFormat = jsonFormat2(SimpleUserGiveaway)
}
