package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.JsonProtocol
import persistence.entities._
import akka.http.scaladsl.model.StatusCodes._
import JsonProtocol._
import SprayJsonSupport._
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PollRoutesSpec extends AbstractRestTest with BeforeAndAfterAll {

  import modules.profile.api._

  def actorRefFactory = system

  val modules = new Modules {}
  val polls = new PollRoutes(modules)

  def executeAction[X](action: DBIOAction[X, NoStream, _]): X = {
    Await.result(modules.db.run(action), Duration.Inf)
  }

  override def beforeAll() {
    createSchema()
  }

  override def afterAll() {
    dropSchema()
    shutdownDb()
  }

  def createSchema(): Unit = {
    executeAction(
      DBIO.seq(
        modules.pollsDal.tableQuery.schema.createIfNotExists
      )
    )
  }

  def dropSchema(): Unit = {
    executeAction(
      DBIO.seq(
        modules.pollsDal.tableQuery.schema.drop
      )
    )
  }

  def shutdownDb(): Unit = {
    modules.db.close()
  }

  "User Routes" should {

    "Return all polls" in {
      Get("/polls") ~> polls.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "Create poll" in {
      Post("/polls", SimplePoll("Free chapi ?", "yes", "no")) ~> polls.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Option[Poll]].isEmpty shouldEqual false
        responseAs[Poll].question shouldEqual "Free chapi ?"
        responseAs[Poll].label1 shouldEqual "yes"
        responseAs[Poll].label2 shouldEqual "no"
      }
    }

    "Participate to a non existing poll" in {
      Patch("/polls/participate", PollParticipate(5, 0, 1)) ~> polls.routes ~> check {
        handled shouldEqual true
        status shouldEqual NotFound
      }
    }

    "Invalid participation to existing poll" in {
      Patch("/polls/participate", PollParticipate(1, 1, 1)) ~> polls.routes ~> check {
        handled shouldEqual false
      }
    }

    "Participate to existing poll" in {
      Patch("/polls/participate", PollParticipate(1, 0, 1)) ~> polls.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[Poll]].isEmpty shouldEqual false
        responseAs[Poll].option1 shouldEqual 0
        responseAs[Poll].option2 shouldEqual 1
      }
    }

    "Poll result" in {
      Get("/polls/1/result") ~> polls.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[Poll]].isEmpty shouldEqual false
        responseAs[Poll].option1 shouldEqual 0
        responseAs[Poll].option2 shouldEqual 1
      }
    }
  }
}
