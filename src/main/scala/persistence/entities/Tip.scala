package persistence.entities

import java.sql.Timestamp

import com.byteslounge.slickrepo.meta.{Entity}

case class SimpleTip(user_id: Int, amount: Int, date: String)
case class TipId(id: Int)

case class Tip(override val id: Option[Int], user_id: Option[Int], amount: Option[Int], date: Option[Timestamp]) extends Entity[Tip, Int]{
  def withId(id: Int): Tip = this.copy(id = Some(id))
}