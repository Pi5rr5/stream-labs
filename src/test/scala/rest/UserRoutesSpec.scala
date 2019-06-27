package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import entities.JsonProtocol
import persistence.entities.{SimpleUser, User, UserRepository}
import akka.http.scaladsl.model.StatusCodes._
import JsonProtocol._
import SprayJsonSupport._
import slick.dbio.DBIOAction

import scala.concurrent.Future

class UserRoutesSpec extends AbstractRestTest {

  def actorRefFactory = system
  val modules = new Modules {}
  val users = new UserRoutes(modules)

  "User Routes" should {

    "return all users" in {
      val testUser = User(None, Some("test"), Some(0), Some(0))
      val dbAction = DBIOAction.from(Future.successful(Seq(testUser)))
      modules.usersDal.findAll() returns dbAction
      Get("/users") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
      }
    }

    "return an empty array of users" in {
      val dbAction = DBIOAction.from(Future(None))
      modules.usersDal.findOne(1) returns dbAction
      Get("/users/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual NotFound
      }
    }

    "return an array with 1 user" in {
      val dbAction = DBIOAction.from(Future(Some(User(None, Some("test"), Some(0), Some(0)))))
      modules.usersDal.findOne(1) returns dbAction
      Get("/users/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[User]].isEmpty shouldEqual false
      }
    }

    "delete a user" in {
      val testUser = User(None, Some("test"), Some(0), Some(0))
      val dbAction = DBIOAction.from(Future(testUser))
      modules.usersDal.delete(User(Option(1), None, None, None)) returns dbAction
      Delete("/users/1") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual(NoContent)
      }
    }

    "create a user with the data in the body" in {
      val testUser = User(None, Some("test"), Some(0), Some(0))
      val dbAction = DBIOAction.from(Future.successful(testUser))
      modules.usersDal.save(testUser) returns dbAction
      Post("/users", SimpleUser("test", 0, 0)) ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
        responseAs[Option[User]].isEmpty shouldEqual false
        responseAs[User].pseudo shouldEqual Some("test")
        responseAs[User].sub shouldEqual Some(0)
        responseAs[User].blacklist shouldEqual Some(0)
      }
    }

    "patch a user with the data in the body" in {
      val testUser = User(Some(1), Some("test"), Some(0), Some(0))
      val dbAction = DBIOAction.from(Future.successful(testUser))
      modules.usersDal.save(testUser) returns dbAction
      Patch("/users/1", SimpleUser("test2", 0, 0)) ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[User]].isEmpty shouldEqual false
        responseAs[User].pseudo shouldEqual Some("test2")
      }
    }

    "return all blacklisted users" in {
      val testUser1 = User(None, Some("test"), Some(0), Some(1))
      val testUser2 = User(None, Some("test"), Some(0), Some(0))
      val dbAction = DBIOAction.from(Future.successful(Seq(testUser1, testUser2)))
      modules.usersDal.findAll() returns dbAction
      Get("/users/blacklist") ~> users.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Seq[User]].isEmpty shouldEqual false
      }
    }

    "not handle the invalid json" in {
      Post("/users","{\"test\":\"1\"}") ~> users.routes ~> check {
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
