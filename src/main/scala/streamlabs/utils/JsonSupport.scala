package streamlabs.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import streamlabs.methods.UserMethods.ActionPerformed
import streamlabs.methods.{User, Users}
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat4(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
