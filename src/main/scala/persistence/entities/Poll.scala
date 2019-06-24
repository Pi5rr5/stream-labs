package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

case class SimplePoll()

case class Poll(override val id: Option[Int], question: String, option1: Int, option2: Int) extends Entity[Poll, Int] {
  def withId(id: Int): Poll = this.copy(id = Some(id))
}

class PollRepository(override val driver: JdbcProfile) extends Repository[Poll, Int](driver) {

  import driver.api._

  val pkType = implicitly[BaseTypedType[Int]]
  val tableQuery = TableQuery[Polls]
  type TableType = Polls

  class Polls(tag: Tag) extends Table[Poll](tag, "polls") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def question = column[String]("question")

    def option1 = column[Int]("option1")

    def option2 = column[Int]("option2")

    def * = (id.?, question, option1, option2) <> ((Poll.apply _).tupled, Poll.unapply)
  }

}
