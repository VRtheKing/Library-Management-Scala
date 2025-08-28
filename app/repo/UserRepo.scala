package repo

import models.User

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.Inject

class UserRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{
  val users = TableQuery[models.UserModel]
  def createUser(user: User): Future[Int] = {
    db.run(users += user)
  }
  def listUsers(): Future[Seq[User]] = {
    db.run(users.result)
  }
}
