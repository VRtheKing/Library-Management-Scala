package repo

import models.{Book, BookPatch}

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime
import javax.inject.Inject

class BookRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  val books = TableQuery[models.BookModel]

  def createBook(book: Book): Future[Int] = {
    bookExists(book.isbn) flatMap {
      case Some(_) => Future.successful(-1)
      case None => db.run(books += book)
    }
  }

  def listAllBooks: Future[Seq[Book]] = {
    db.run(books.result)
  }

  def updateBook(book: BookPatch): Future[Either[String, Book]] = {
    val finder = books.filter(_.id === book.id)
    db.run(finder.result.headOption).flatMap {
      case None => Future.successful(Left("Book Not Found"))
      case Some(existingBook) =>
        val updatedBook = existingBook.copy(
          title = book.title.getOrElse(existingBook.title),
          author = book.author.getOrElse(existingBook.author),
          isbn = book.isbn.getOrElse(existingBook.isbn),
          stock = book.stock.getOrElse(existingBook.stock),
          updated_at = Some(LocalDateTime.now())
        )
        if (existingBook == updatedBook.copy(updated_at = existingBook.updated_at)) {
          Future.successful(Left("No changes are made"))
        } else {
          db.run(finder.update(updatedBook)).flatMap { _ =>
            db.run(finder.result.headOption).map {
              case Some(book) => Right(book)
              case None => Left("Book Not Found")
            }
          }
        }
    }
  }

  def bookExists(isbn: String): Future[Option[Book]] = {
    db.run(books.filter(_.isbn === isbn).result.headOption)
  }

  def getBookFine(bookId: Long): Future[Int] = {
    db.run(books.filter(_.id === bookId).map(_.fine).result.headOption).flatMap {
      case Some(fine) => Future.successful(fine)
      case None       => Future.failed(new NoSuchElementException(s"Book with ID $bookId not found"))
    }
  }

  def decreaseStock(bookId: Long): Future[Int] = {
    val query = books.filter(_.id === bookId).map(_.stock).result.headOption.flatMap {
      case Some(stock) if stock > 0 =>
        books.filter(_.id === bookId).map(_.stock).update(stock - 1)
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
