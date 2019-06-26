package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

case class SimpleTip(user_id: Int, amount: Int)

case class Tip(override val id: Option[Int], user_id: Option[Int], amount: Option[Int]) extends Entity[Tip, Int] {
  def withId(id: Int): Tip = this.copy(id = Some(id))
}

class TipRepository(override val driver: JdbcProfile) extends Repository[Tip, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Tips]
  type TableType = Tips

  lazy val userRepository = new UserRepository(driver)

  class Tips(tag: Tag) extends Table[Tip](tag, "tips") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def user_id = column[Option[Int]]("user_id")

    def amount = column[Int]("amount")

    def * = (id.?, user_id, amount.?) <> ((Tip.apply _).tupled, Tip.unapply)

    def user = foreignKey("USER_FK", user_id, userRepository.tableQuery)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  def getDonators(): DBIO[Seq[User]] = {
    (tableQuery join userRepository.tableQuery on (_.user_id === _.id))
      .map(x => x._2)
      .distinct
      .result
  }

}


