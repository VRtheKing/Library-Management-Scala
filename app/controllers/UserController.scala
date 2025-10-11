package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.{User, UserLogin, UserPatch}
import services.UserService
import models.User.updateUserFormat
import security.JwtAction
import security.JwtUtil.hasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
                                cc: ControllerComponents,
                                userService: UserService,
                                jwtAction: JwtAction
                              )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // POST /users
  def createUser: Action[JsValue] = jwtAction.async(parse.json) { request =>
    if (hasRole(List("Admin", "Librarian", "User"))(request)) {
      request.body
        .validate[User]
        .fold(
          errors =>
            Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
          user => {
            userService.createUser(user).map { _ =>
              Created(Json.obj("status" -> "User created"))
            }
          }
        )
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to create a user")))
    }
  }

  // PATCH /users
  def updateUser: Action[JsValue] = jwtAction.async(parse.json) { request =>
    if (hasRole(List("Admin", "Librarian", "User"))(request)) {
      request.body
        .validate[UserPatch]
        .fold(
          errors =>
            Future.successful(BadRequest(Json.obj("status" -> "Invalid JSON"))),
          updatedUser => {
            userService.updateUser(updatedUser).map {
              case Left(msg) => Ok(Json.obj("status" -> msg))
              case Right(user) =>
                Created(
                  Json.obj("Status" -> "User Updated", "Updated User" -> user)
                )
            }
          }
        )
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to update a user")))
    }
  }

  // GET /users

  def listUsers: Action[AnyContent] = jwtAction.async { request =>
    if (hasRole(List("Admin", "Librarian", "User"))(request)) {
      userService.listUser().map { users =>
        Ok(Json.toJson(users))
      }
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to view all users")))
    }
  }

  // GET /borrowedBooks
  def borrowedBooks(userId: Long): Action[AnyContent] = jwtAction.async { request =>
    if (hasRole(List("Admin", "Librarian", "User"))(request)) {
      userService.listBorrowedBooks(userId).map { books =>
        Ok(Json.toJson(books))
      }
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to view borrowed books")))
    }
  }

  // DELETE /users/:userId
  def deleteUser(userId: Long): Action[AnyContent] = jwtAction.async { request =>
    if (hasRole(List("Admin"))(request)) {
      userService.deleteUser(userId).map {
        case 0 => Ok(Json.toJson("Status" -> "User Not Found"))
        case _ => Ok(Json.toJson("Status" -> "User Deleted"))
      }
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to delete a book")))
    }
  }

  // POST /login
  def userLogin: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UserLogin].fold(
      errors => Future.successful(BadRequest(Json.obj("status" -> "Invalid JSON"))),
      loginUser => {
        userService.loginUser(loginUser).map {
          case Left(errorMessage) => Ok(Json.obj("status" -> errorMessage))
          case Right(token) =>
            Created(Json.obj("status" -> "User Logged In", "JWT" -> token))
        }
      }
    )
  }
}
