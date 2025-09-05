package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.User
import services.UserService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(cc: ControllerComponents, userService: UserService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // POST /users
  def createUser: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      user => {
        userService.createUser(user).map { _ =>
          Created(Json.obj("status" -> "User created"))
        }
      }
    )
  }

  // GET /users
  def listUsers: Action[AnyContent] = Action.async {
    userService.listUser().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def borrowedBooks(userId: Long): Action[AnyContent] = Action.async{
    userService.listBorrowedBooks(userId).map { books=>
      Ok(Json.toJson(books))
    }
  }


}
