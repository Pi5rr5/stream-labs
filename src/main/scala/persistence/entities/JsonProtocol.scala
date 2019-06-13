package entities

import persistence.entities.{SimpleUser, User}
import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User)
  implicit val simpleUserFormat = jsonFormat3(SimpleUser)
}
