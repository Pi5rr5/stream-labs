package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.JsonProtocol
import akka.http.scaladsl.model.StatusCodes._
import JsonProtocol._
import SprayJsonSupport._
import org.scalatest.BeforeAndAfterAll
import persistence.entities.{Giveaway, SimpleUser, SimpleGiveaway, SimpleUserGiveaway, User}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GiveawayRoutesSpec extends AbstractRestTest with BeforeAndAfterAll {

  import modules.profile.api._

  def actorRefFactory = system

  val modules = new Modules {}
  val users = new UserRoutes(modules)
  val giveaways = new GiveawayRoutes(modules)

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
        modules.giveawaysDal.tableQuery.schema.createIfNotExists,
        modules.userGiveawaysDal.tableQuery.schema.createIfNotExists
      )
    )
  }

  def dropSchema(): Unit = {
    executeAction(
      DBIO.seq(
        modules.userGiveawaysDal.tableQuery.schema.drop,
        modules.giveawaysDal.tableQuery.schema.drop,
        modules.usersDal.tableQuery.schema.drop
      )
    )
  }

  def shutdownDb(): Unit = {
    modules.db.close()
  }

  "Giveaway Routes" should {

    "return all giveaways" in {
      Get("/giveaways") ~> giveaways.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "create giveaway" in {
      Post("/giveaways", SimpleGiveaway("test")) ~> giveaways.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Option[Giveaway]].isEmpty shouldEqual false
        responseAs[Giveaway].description shouldEqual Some("test")
      }
    }

    "return giveaway with id 1" in {
      Get("/giveaways/1") ~> giveaways.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[Giveaway]].isEmpty shouldEqual false
        responseAs[Giveaway].description shouldEqual Some("test")
      }
    }

    "return giveaway not found" in {
      Get("/giveaways/2") ~> giveaways.routes ~> check {
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

    "Participate to giveaway 1" in {
      Post("/giveaways/participate", SimpleUserGiveaway(1, 1)) ~> giveaways.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
      }
    }

    "create ban user with id 2" in {
      Post("/users", SimpleUser("test", 0, 1)) ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Option[User]].isEmpty shouldEqual false
        responseAs[User].pseudo shouldEqual Some("test")
        responseAs[User].sub shouldEqual Some(0)
        responseAs[User].blacklist shouldEqual Some(1)
      }
    }

    "Try participation to giveaway 1 with ban user" in {
      Post("/giveaways/participate", SimpleUserGiveaway(1, 2)) ~> giveaways.routes ~> check {
        handled shouldEqual false
      }
    }

    "Draw giveaway 1" in {
      Get("/giveaways/1/draw") ~> giveaways.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "delete giveaway with id 1" in {
      Delete("/giveaways/1") ~> giveaways.routes ~> check {
        handled shouldEqual true
        status shouldEqual (NoContent)
      }
    }

    "not handle the invalid json" in {
      Post("/giveaways", "{\"test\":\"1\"}") ~> users.routes ~> check {
        handled shouldEqual false
      }
    }

    "not handle an empty post" in {
      Post("/giveaways") ~> users.routes ~> check {
        handled shouldEqual false
      }
    }

  }
}
