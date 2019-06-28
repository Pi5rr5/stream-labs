package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.JsonProtocol
import persistence.entities.{SimpleTip, SimpleUser, Tip, User}
import akka.http.scaladsl.model.StatusCodes._
import JsonProtocol._
import SprayJsonSupport._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TipRoutesSpec extends AbstractRestTest with BeforeAndAfterAll {

  import modules.profile.api._

  def actorRefFactory = system

  val modules = new Modules {}
  val tips = new TipRoutes(modules)
  val users = new UserRoutes(modules)

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
        modules.usersDal.tableQuery.schema.createIfNotExists,
        modules.tipsDal.tableQuery.schema.createIfNotExists
      )
    )
  }

  def dropSchema(): Unit = {
    executeAction(
      DBIO.seq(
        modules.usersDal.tableQuery.schema.drop,
        modules.tipsDal.tableQuery.schema.drop
      )
    )
  }

  def shutdownDb(): Unit = {
    modules.db.close()
  }

  "Tip Routes" should {
    "return all tips" in {
      Get("/tips") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "New tips with false user id" in {
      Post("/tips", SimpleTip(1, 10)) ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual NotFound
      }
    }

    "create user with id 1" in {
      Post("/users", SimpleUser("test", 0, 0)) ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Option[User]].isEmpty shouldEqual false
        responseAs[User].pseudo shouldEqual Some("test")
        responseAs[User].sub shouldEqual Some(0)
        responseAs[User].blacklist shouldEqual Some(0)
      }
    }

    "New tips with existing user id" in {
      Post("/tips", SimpleTip(1, 10)) ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Option[Tip]].isEmpty shouldEqual false
        responseAs[Tip].user_id shouldEqual Some(1)
        responseAs[Tip].amount shouldEqual Some(10)
      }
    }

    "return all donators" in {
      Get("/tips/users") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "return tips sum" in {
      Get("/tips/sum") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "return tips sum for user 1" in {
      Get("/tips/users/1/sum") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "return tips sum group by user" in {
      Get("/tips/users/sum") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "delete tips with id 1" in {
      Delete("/tips/1") ~> tips.routes ~> check {
        handled shouldEqual true
        status shouldEqual (NoContent)
      }
    }

    "not handle the invalid json" in {
      Post("/tips", "{\"test\":\"1\"}") ~> tips.routes ~> check {
        handled shouldEqual false
      }
    }

    "not handle an empty post" in {
      Post("/tips") ~> tips.routes ~> check {
        handled shouldEqual false
      }
    }
  }
}
