package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

case class SimpleUserGiveaway(giveaway_id: Int, user_id: Int)

case class UserGiveaway(override val id: Option[Int], giveaway_id: Option[Int], user_id: Option[Int]) extends Entity[UserGiveaway, Int] {
  def withId(id: Int): UserGiveaway = this.copy(id = Some(id))
}

class UserGiveawayRepository(override val driver: JdbcProfile) extends Repository[UserGiveaway, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[UserGiveaways]
  type TableType = UserGiveaways

  lazy val userRepository = new UserRepository(driver)
  lazy val givewayRepository = new GiveawayRepository(driver)
  val userTable = userRepository.tableQuery
  val givewayTable = givewayRepository.tableQuery

  class UserGiveaways(tag: Tag) extends Table[UserGiveaway](tag, "user_giveaways") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def giveaway_id = column[Option[Int]]("giveaway_id")

    def user_id = column[Int]("user_id")

    def * = (id.?, giveaway_id, user_id.?) <> ((UserGiveaway.apply _).tupled, UserGiveaway.unapply)

    def giveaway_fk = foreignKey("giveaway_fk", giveaway_id, givewayTable)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user_fk = foreignKey("user_fk", user_id, userTable)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  def draw(giveawayId: Int): DBIO[Seq[(UserGiveaway, User)]] = {
    val randomFunction = SimpleFunction.nullary[Double]("random")

    val q = for {
      (gar, u) <- tableQuery join userRepository.tableQuery on (_.user_id === _.id)
    } yield (gar, u)

    q.filter(_._1.giveaway_id === giveawayId)
      .filter(_._2.blacklist === 0)
      .sortBy(_ => randomFunction)
      .take(1)
      .result
  }

  def exists(giveawayId: Int, userId: Int) = {
    tableQuery.filter(gar => gar.giveaway_id === giveawayId && gar.user_id === userId).exists.result
  }
}


