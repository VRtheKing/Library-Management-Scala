package services

import models.{Book, BookPatch}

import scala.concurrent.{ExecutionContext, Future}
import repo.BookRepo

import javax.inject.Inject

class BookService @Inject()(bookRepo: BookRepo)(implicit ec: ExecutionContext) {
  def createBook(book: Book): Future[Int] = {
    bookRepo.createBook(book) // Create Book service
  }
  def listBook(): Future[Seq[Book]] = {
    bookRepo.listAllBooks // List all books
  }
  def updateBook(book: BookPatch): Future[Either[String, Book]] = {
    bookRepo.updateBook(book) // Updates the book
  }
}
