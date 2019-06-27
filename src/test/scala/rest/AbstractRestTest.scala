package rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.byteslounge.slickrepo.repository.Repository
import org.scalatest.{Matchers, WordSpec}
import org.specs2.mock.Mockito
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.{ActorModule, ConfigurationModuleImpl, DbModule, PersistenceModule}
import persistence.entities.{User, Tip, Giveaway, UserGiveaway, Poll}
import com.typesafe.config.{Config, ConfigFactory}


class AbstractRestTest extends WordSpec with Matchers with ScalatestRouteTest with Mockito {

  trait Modules extends ConfigurationModuleImpl with ActorModule with PersistenceModule with DbModule {
    val system = AbstractRestTest.this.system

    private val dbConfig : DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("streamlabs")
    override implicit val profile: JdbcProfile = dbConfig.profile
    override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

    override val usersDal = mock[Repository[User, Int]]
    override val tipsDal = mock[Repository[Tip, Int]]
    override val giveawaysDal = mock[Repository[Giveaway, Int]]
    override val userGiveawaysDal = mock[Repository[UserGiveaway, Int]]
    override val pollsDal = mock[Repository[Poll, Int]]
  }

  def getConfig: Config = ConfigFactory.empty()

}
