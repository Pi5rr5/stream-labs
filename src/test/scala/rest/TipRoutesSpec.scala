package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.JsonProtocol
import persistence.entities.{Tip, SimpleTip}
import akka.http.scaladsl.model.StatusCodes._
import JsonProtocol._
import SprayJsonSupport._
import slick.dbio.DBIOAction

import scala.concurrent.Future

class TipRoutesSpec extends AbstractRestTest {

  def actorRefFactory = system
  val modules = new Modules {}
  val tips = new TipRoutes(modules)

  "Tip Routes" should {

    "return all tips" in {
      val testTip = Tip(None, Some(1), Some(10))
      val dbAction = DBIOAction.from(Future.successful(Seq(testTip)))
      modules.tipsDal.findAll() returns dbAction
      Get("/tips") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }
  }
}
