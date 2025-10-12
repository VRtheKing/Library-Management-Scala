package controllers

import models.Checkout.checkoutPatchFormat

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.{Checkout, CheckoutPatch}
import security.JwtAction
import security.JwtUtil.hasRole
import services.CheckoutService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckoutController @Inject()(
                                    cc: ControllerComponents,
                                    checkoutService: CheckoutService,
                                    jwtAction: JwtAction
                                  )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // POST /checkouts
  def createCheckout: Action[JsValue] = jwtAction.async(parse.json) { request =>
    if (hasRole(List(1,2,3))(request)) {
      request.body
        .validate[Checkout]
        .fold(
          errors => Future.successful(BadRequest(JsError.toJson(errors))),
          checkout => {
            checkoutService.createCheckout(checkout).map {
              case Right(_) => Created(Json.obj("status" -> "Checkout created"))
              case Left(msg) => BadRequest(Json.obj("error" -> msg))
            }
          }
        )
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to create a checkout")))
    }
  }

  // GET /checkouts
  def getCheckouts(status: String): Action[AnyContent] = jwtAction.async { request =>
    if (hasRole(List(1,2,3))(request)) {
      checkoutService.listCheckouts(status).map { checkouts =>
        Ok(Json.toJson(checkouts))
      }
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to view checkouts")))
    }
  }

  // PATCH /checkout
  def updateCheckout(): Action[JsValue] = jwtAction.async(parse.json) { request =>
    if (hasRole(List(1,2))(request)) {
      request.body
        .validate[CheckoutPatch]
        .fold(
          errors => Future.successful(BadRequest(JsError.toJson(errors))),
          checkout => {
            checkoutService.updateCheckout(checkout).map {
              case Left(msg) => Ok(Json.obj("status" -> msg))
              case Right(checkout) =>
                Created(
                  Json.obj("status" -> "Checkout Updated", "checkout" -> checkout)
                )
            }
          }
        )
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to update checkout")))
    }
  }

  // POST    /checkouts/:checkoutId/return
  def returnBook(checkoutId: Long): Action[AnyContent] = jwtAction.async { request =>
    if (hasRole(List(1,2,3))(request)) {
      checkoutService.createReturn(checkoutId).map {
        case Right(fine) =>
          Ok(Json.obj("status" -> "Book returned successfully", "fine" -> fine))
        case Left(msg) => BadRequest(Json.obj("error" -> msg))
      }
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to return the book")))
    }
  }

  // DELETE /checkout/:checkoutId
  def deleteCheckout(checkoutId: Long): Action[AnyContent] = jwtAction.async { request =>
    if (hasRole(List(1))(request)) {
      checkoutService.deleteCheckout(checkoutId).map {
        case 0 => Ok(Json.toJson("status" -> "Checkout not found"))
        case _ => Ok(Json.toJson("status" -> "Checkout deleted"))
      }
    } else {
      Future.successful(Forbidden(Json.obj("status" -> "You do not have permission to delete a checkout")))
    }
  }
}
