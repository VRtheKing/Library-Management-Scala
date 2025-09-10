package controllers

import jobs.OverdueCheckerJob

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.Book
import services.BookService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookController @Inject()(cc: ControllerComponents, bookService: BookService, job: OverdueCheckerJob)(implicit ec: ExecutionContext) extends AbstractController(cc) {

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

  // GET /books
  def listBooks: Action[AnyContent] = Action.async {
    bookService.listBook().map { book =>
      Ok(Json.toJson(book))
    }
  }
}
