package controllers

import models.Book.bookPatchFormat

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.{Book, BookPatch}
import services.BookService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookController @Inject()(cc: ControllerComponents, bookService: BookService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // POST /books
  def createBook: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Book].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      book => {
        bookService.createBook(book).map {
          case -1 => Ok(Json.obj("status" -> "Book already exists"))
          case _ => Created(Json.obj("status" -> "Book created"))
        }
      }
    )
  }

  // PATCH /books
  def updateBook(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[BookPatch].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      book => {
        bookService.updateBook(book).map {
          case Left(msg) => Ok(Json.obj("status" -> msg))
          case Right(book) => Created(Json.obj("Status" -> "Book Updated", "updatedBook" -> book))
        }
      }
    )
  }

  // GET /books
  def listBooks: Action[AnyContent] = Action.async {
    bookService.listBook().map { book =>
      Ok(Json.toJson(book))
    }
  }
}
