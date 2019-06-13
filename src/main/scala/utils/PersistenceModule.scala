package utils

import com.byteslounge.slickrepo.repository.Repository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import persistence.entities.{User, UserRepository}
import slick.dbio.DBIO

import scala.concurrent.Future


trait Profile {
  val profile: JdbcProfile
}


trait DbModule extends Profile {
  val db: JdbcProfile#Backend#Database

  implicit def executeOperation[T](databaseOperation: DBIO[T]): Future[T] = {
    db.run(databaseOperation)
  }

}

trait PersistenceModule {
  val usersDal: Repository[User, Int]
}


trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("streamlabs")

  override implicit val profile: JdbcProfile = dbConfig.profile
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

  override val usersDal = new UserRepository(profile)
}
