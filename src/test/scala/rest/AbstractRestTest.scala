package rest

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import utils.{ActorModule, ConfigurationModuleImpl, DbModule, PersistenceModule}
import persistence.entities._
import com.typesafe.config.{Config, ConfigFactory}


class AbstractRestTest extends WordSpec with Matchers with ScalatestRouteTest  {

  trait Modules extends ConfigurationModuleImpl with ActorModule with PersistenceModule with DbModule {
    val system = AbstractRestTest.this.system

    private val dbConfig : DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("streamlabs")
    override implicit val profile: JdbcProfile = dbConfig.profile
    override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

    override val usersDal = new UserRepository(profile)
    override val tipsDal = new TipRepository(profile)
    override val giveawaysDal = new GiveawayRepository(profile)
    override val userGiveawaysDal = new UserGiveawayRepository(profile)
    override val pollsDal = new PollRepository(profile)
  }

  def getConfig: Config = ConfigFactory.empty()

}
