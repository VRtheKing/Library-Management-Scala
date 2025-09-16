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

  // Insert Book in DB
  def createBook(book: Book): Future[Int] = {
    bookExists(book.isbn) flatMap {
      case Some(_) => Future.successful(-1) // Book already exists
      case None => db.run(books += book) // Creates a new book
    }
  }

  // Retrieve All Books from DB
  def listAllBooks: Future[Seq[Book]] = {
    db.run(books.result)
  }

  // Update a Book in DB
  def updateBook(book: BookPatch): Future[Either[String, Book]] = {
    val finder = books.filter(_.id === book.id)
    db.run(finder.result.headOption).flatMap {
      case None => Future.successful(Left("Book Not Found")) // If no book is found with that id
      case Some(existingBook) =>
        val updatedBook = existingBook.copy(
          title = book.title.getOrElse(existingBook.title),
          author = book.author.getOrElse(existingBook.author),
          isbn = book.isbn.getOrElse(existingBook.isbn),
          stock = book.stock.getOrElse(existingBook.stock),
          updated_at = Some(LocalDateTime.now())
        ) // Modifies the entry for the update
        if (existingBook == updatedBook.copy(updated_at = existingBook.updated_at)) {
          Future.successful(Left("No changes are made")) // if the previous book and the updated is same, no changes are made is returned
        } else {
          db.run(finder.update(updatedBook)).flatMap { _ =>
            db.run(finder.result.headOption).map {
              case Some(book) => Right(book) // Returns the updated Book
              case None => Left("Book Not Found") // If No book is found after update, very low possibility
            }
          }
        }
    }
  }

  // Check if a Book exists in the DB
  def bookExists(isbn: String): Future[Option[Book]] = {
    db.run(books.filter(_.isbn === isbn).result.headOption)
  }

  // Retrieve the Fine amount for a book from DB
  def getBookFine(bookId: Long): Future[Int] = {
    db.run(books.filter(_.id === bookId).map(_.fine).result.headOption).flatMap {
      case Some(fine) => Future.successful(fine) // Fetch the fine_per_day value from the DB for the book
      case None       => Future.failed(new NoSuchElementException(s"Book with ID $bookId not found"))
    }
  }

  // Decrease the stock of Book when a book is checked out
  def decreaseStock(bookId: Long): Future[Int] = {
    val query = books.filter(_.id === bookId).map(_.stock).result.headOption.flatMap {
      case Some(stock) if stock > 0 =>
        books.filter(_.id === bookId).map(_.stock).update(stock - 1) // Decrease the stock by 1 on checkout
      case None =>
        DBIO.successful(0)
    }
    db.run(query)
  }

  // Increase the stock of Book when a book is checked out
  def increaseStock(bookId: Long): Future[Int] = {
    val query = books.filter(_.id === bookId).map(_.stock).result.headOption.flatMap {
      case Some(stock) =>
        books.filter(_.id === bookId).map(_.stock).update(stock + 1) // updates the stock +1 on return
      case None =>
        DBIO.successful(0)
    }
    db.run(query)
  }

  // Check if a Book is available or out of stock
  def isOutOfStock(bookId: Long): Future[Boolean] = {
    db.run(books.filter(_.id === bookId).map(_.stock).result.headOption).map {
      case Some(stock) => stock <= 0 // Book is in stock
      case None        => true // Books is out of stock
    }
  }
}
