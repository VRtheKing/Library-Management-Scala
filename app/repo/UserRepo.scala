package repo

import models.{Checkout, User, UserPatch}

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject

class UserRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{
  val users = TableQuery[models.UserModel]
  var checkout = TableQuery[models.CheckoutModel]
  def createUser(user: User): Future[Int] = {
    db.run(users.map(_.insertProjection) += user)
  }
  def listUsers(): Future[Seq[User]] = {
    db.run(users.result)
  }
  def findById(userId: Long): Future[Option[User]] = {
    db.run(users.filter(_.id === userId).result.headOption)
  }
  def updateUser(updatedUser: UserPatch): Future[Either[String, User]] = {
    val finder = users.filter(_.id === updatedUser.id)
    db.run(finder.result.headOption).flatMap{
      case None => Future(Left("User not Found"))
      case Some(existingUser) => {
        val updateUser = existingUser.copy(
          name = updatedUser.name.getOrElse(existingUser.name),
          email = updatedUser.email.getOrElse(existingUser.email)
        )
        db.run(finder.update(updateUser)).flatMap{ _ =>
          db.run(finder.result.headOption).map{
            case Some(user) => {
              if (existingUser==updateUser) Left("No Changes are made")
              else Right(user)
            }
          }
        }
      }
    }
  }
  def listBorrowedBooks(userId: Long): Future[Seq[String]] = {
    val books = TableQuery[models.BookModel]
    val query = for {
      c <- checkout if c.userId === userId && !c.returned
      b <- books if b.id === c.bookId
    } yield b.title

    db.run(query.result)
  }
}
