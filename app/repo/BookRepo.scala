package repo

import models.Book
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.Inject

class BookRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  val books = TableQuery[models.BookModel]

  def createBook(book: Book): Future[Int] = {
    db.run(books += book)
  }

  def listAllBooks(): Future[Seq[Book]] = {
    db.run(books.result)
  }

  def addStockCount(book: Book): Future[Int] = {
    db.run(books += book)
  }

  def decreaseStock(bookId: Long): Future[Int] = {
    val query = books.filter(_.id === bookId).map(_.stock).result.headOption.flatMap {
      case Some(stock) if stock > 0 =>
        books.filter(_.id === bookId).map(_.stock).update(stock - 1)
      case Some(_) =>
        DBIO.successful(0)
      case None =>
        DBIO.successful(0)
    }
    db.run(query)
  }


  def increaseStock(bookId: Long): Future[Int] = {
    val query = books.filter(_.id === bookId).map(_.stock).result.headOption.flatMap {
      case Some(stock) =>
        books.filter(_.id === bookId).map(_.stock).update(stock + 1)
      case None =>
        DBIO.successful(0)
    }
    db.run(query)
  }

  def isOutOfStock(bookId: Long): Future[Boolean] = {
    db.run(books.filter(_.id === bookId).map(_.stock).result.headOption).map {
      case Some(stock) => stock <= 0
      case None        => true
    }
  }
}
