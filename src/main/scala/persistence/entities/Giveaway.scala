package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

case class SimpleGiveaway(user_id: Int, description: String)
case class SimpleUserGiveaway(giveaway_id: Int, user_id: Int)

case class Giveaway(override val id: Option[Int], user_id: Option[Int], description: Option[String]) extends Entity[Giveaway, Int] {
  def withId(id: Int): Giveaway = this.copy(id = Some(id))
}

case class UserGiveaway(override val id: Option[Int], giveaway_id: Option[Int], user_id: Option[Int]) extends Entity[UserGiveaway, Int] {
  def withId(id: Int): UserGiveaway = this.copy(id = Some(id))
}

class GiveawayRepository(override val driver: JdbcProfile) extends Repository[Giveaway, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Giveaways]
  type TableType = Giveaways

  class Giveaways(tag: Tag) extends Table[Giveaway](tag, "giveaways") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def user_id = column[Option[Int]]("user_id")

    def description = column[String]("description")

    def * = (id.?, user_id, description.?) <> ((Giveaway.apply _).tupled, Giveaway.unapply)

    def user_fk = foreignKey("user_fk", user_id, tableQuery)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  val tableQuery2 = TableQuery[UserGiveaways]
  type TableType2 = UserGiveaway

  class UserGiveaways(tag: Tag) extends Table[UserGiveaway](tag, "user_giveaways") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def giveaway_id = column[Option[Int]]("giveaway_id")

    def user_id = column[Int]("user_id")

    def * = (id.?, giveaway_id, user_id.?) <> ((UserGiveaway.apply _).tupled, UserGiveaway.unapply)

    def giveaway_fk = foreignKey("giveaway_fk", giveaway_id, tableQuery)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user_fk = foreignKey("user_fk", user_id, tableQuery)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

}


