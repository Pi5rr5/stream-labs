package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.JsonProtocol
import persistence.entities.{SimpleUser, User}
import akka.http.scaladsl.model.StatusCodes._
import JsonProtocol._
import SprayJsonSupport._
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UserRoutesSpec extends AbstractRestTest with BeforeAndAfterAll {

  import modules.profile.api._

  def actorRefFactory = system

  val modules = new Modules {}
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
        modules.usersDal.tableQuery.schema.createIfNotExists
      )
    )
  }

  def dropSchema(): Unit = {
    executeAction(
      DBIO.seq(
        modules.usersDal.tableQuery.schema.drop
      )
    )
  }

  def shutdownDb(): Unit = {
    modules.db.close()
  }

  "User Routes" should {

    "return all users" in {
      Get("/users") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "return user not found" in {
      Get("/users/1") ~> users.routes ~> check {
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

    "return user with id 1" in {
      Get("/users/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[User]].isEmpty shouldEqual false
      }
    }

    "return empty array of blacklisted users" in {
      Get("/users/blacklist") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Seq[User]].isEmpty shouldEqual true
      }
    }

    "blacklist users" in {
      Post("/users/blacklist/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[User].blacklist shouldEqual Some(1)
      }
    }

    "return array of blacklisted users" in {
      Get("/users/blacklist") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Seq[User]].isEmpty shouldEqual false
      }
    }

    "unblacklist users" in {
      Post("/users/unblacklist/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[User].blacklist shouldEqual Some(0)
      }
    }

    "return empty array of sub users" in {
      Get("/users/subs") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Seq[User]].isEmpty shouldEqual true
      }
    }

    "sub users" in {
      Post("/users/subs/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[User].sub shouldEqual Some(1)
      }
    }

    "return array of sub users" in {
      Get("/users/subs") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Seq[User]].isEmpty shouldEqual false
      }
    }

    "unsub users" in {
      Post("/users/unsubs/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[User].sub shouldEqual Some(0)
      }
    }

    "patch user with id 1" in {
      Patch("/users/1", SimpleUser("test2", 0, 0)) ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[User]].isEmpty shouldEqual false
        responseAs[User].pseudo shouldEqual Some("test2")
      }
    }

    "delete user with id 1" in {
      Delete("/users/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual (NoContent)
      }
    }

    "not handle the invalid json" in {
      Post("/users", "{\"test\":\"1\"}") ~> users.routes ~> check {
        handled shouldEqual false
      }
    }

    "not handle an empty post" in {
      Post("/users") ~> users.routes ~> check {
        handled shouldEqual false
      }
    }
  }

}
