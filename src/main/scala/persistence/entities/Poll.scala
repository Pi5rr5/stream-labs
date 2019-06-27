package persistence.entities

import com.byteslounge.slickrepo.meta.{Entity, Keyed}
import com.byteslounge.slickrepo.repository.Repository
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

case class SimplePoll(question: String, label1: String, label2: String)

case class PollParticipate(id: Int, option1: Int, option2: Int)

case class Poll(override val id: Option[Int], question: String, label1: String, option1: Int, label2: String, option2: Int) extends Entity[Poll, Int] {
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

    def label1 = column[String]("label1")

    def option1 = column[Int]("option1")

    def label2 = column[String]("label12")

    def option2 = column[Int]("option2")

    def * = (id.?, question, label1, option1, label2, option2) <> ((Poll.apply _).tupled, Poll.unapply)
  }
}
