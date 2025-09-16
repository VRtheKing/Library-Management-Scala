package repo

import models.{BorrowedBook, Checkout, User, UserPatch}

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject

class UserRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{
  val users = TableQuery[models.UserModel]
  var checkout = TableQuery[models.CheckoutModel]
  def createUser(user: User): Future[Int] = {
    db.run(users.map(_.insertProjection) += user) // Creates a new user
  }
  def listUsers(): Future[Seq[User]] = {
    db.run(users.result) // Fetches all users
  }
  def findById(userId: Long): Future[Option[User]] = {
    db.run(users.filter(_.id === userId).result.headOption) // Finds user by ID
  }
  def updateUser(updatedUser: UserPatch): Future[Either[String, User]] = {
    val finder = users.filter(_.id === updatedUser.id)
    db.run(finder.result.headOption).flatMap{
      case None => Future(Left("User not Found")) // Returns if user not found
      case Some(existingUser) => {
        val updateUser = existingUser.copy(
          name = updatedUser.name.getOrElse(existingUser.name),
          email = updatedUser.email.getOrElse(existingUser.email)
        ) // Updates the entry of the existing use
        db.run(finder.update(updateUser)).flatMap{ _ =>
          db.run(finder.result.headOption).map{
            case Some(user) => {
              if (existingUser==updateUser) Left("No Changes are made") // If both the previous and changed values are same it returns no changes
              else Right(user) // Returns the updated user
            }
          }
        }
      }
    }
  }
  def listBorrowedBooks(userId: Long): Future[Seq[BorrowedBook]] = {
    val books = TableQuery[models.BookModel]
    val query = for {
      c <- checkout if c.userId === userId && !c.returned // Checks if the checkout is pending or overdue
      b <- books if b.id === c.bookId // matches the book by id
    } yield (c.id, b.title) // returns the checkout id and the book title
    db.run(query.result).map(_.map {
      case (checkoutId, title) => BorrowedBook(checkoutId, title)
    })
  }
}
