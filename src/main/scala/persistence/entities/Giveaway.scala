package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SimpleGiveaway(description: String)

case class Giveaway(override val id: Option[Int], description: Option[String]) extends Entity[Giveaway, Int] {
  def withId(id: Int): Giveaway = this.copy(id = Some(id))
}

class GiveawayRepository(override val driver: JdbcProfile) extends Repository[Giveaway, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Giveaways]
  type TableType = Giveaways

  lazy val userRepository = new UserRepository(driver)
  val userTable = userRepository.tableQuery

  class Giveaways(tag: Tag) extends Table[Giveaway](tag, "giveaways") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def description = column[String]("description")

    def * = (id.?, description.?) <> ((Giveaway.apply _).tupled, Giveaway.unapply)
  }
}


