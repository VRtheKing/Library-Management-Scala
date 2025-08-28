package services

import models.Book
import scala.concurrent.{ExecutionContext, Future}
import repo.BookRepo
import javax.inject.Inject

class BookService @Inject()(bookRepo: BookRepo)(implicit ec: ExecutionContext) {
  def createBook(book: Book): Future[Int] = {
    bookRepo.createBook(book)
  }
  def listBook(): Future[Seq[Book]] = {
    bookRepo.listAllBooks()
  }
}
