package repo

import models.Book

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.Inject

class BookRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  val books = TableQuery[models.BookModel]

  def createBook(book: Book): Future[Int] = {
    db.run(books += book)
  }
  def listAllBooks(): Future[Seq[Book]] = {
    db.run(books.result)
  }
}
