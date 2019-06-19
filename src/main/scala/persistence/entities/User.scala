package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

case class UserId(id: Int)

case class SimpleUser(pseudo: String, sub: Int, blacklist: Int)

case class User(override val id: Option[Int], pseudo: Option[String], sub: Option[Int], blacklist: Option[Int]) extends Entity[User, Int] {
  def withId(id: Int): User = this.copy(id = Some(id))
}

class UserRepository(override val driver: JdbcProfile) extends Repository[User, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Users]
  type TableType = Users

  class Users(tag: Tag) extends Table[User](tag, "users") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def pseudo = column[String]("pseudo")

    def sub = column[Int]("sub")

    def blacklist = column[Int]("blacklist")

    def * = (id.?, pseudo.?, sub.?, blacklist.?) <> ((User.apply _).tupled, User.unapply)
  }

}