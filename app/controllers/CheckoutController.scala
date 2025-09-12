package controllers

import models.Checkout.checkoutPatchFormat

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.{Checkout, CheckoutPatch}
import services.CheckoutService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckoutController @Inject()(cc: ControllerComponents, checkoutService: CheckoutService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def createCheckout: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Checkout].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      checkout => {
        checkoutService.createCheckout(checkout).map {
          case Right(_) => Created(Json.obj("status" -> "Checkout created"))
          case Left(msg) => BadRequest(Json.obj("error" -> msg))
        }
      }
    )
  }

  def getCheckouts(status: String): Action[AnyContent] = Action.async {
    checkoutService.listCheckouts(status).map{ checkouts =>
      Ok(Json.toJson(checkouts))
    }
  }

  // PATCH /checkout
  def updateCheckout(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CheckoutPatch].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      checkout => {
        checkoutService.updateCheckout(checkout).map {
          case Left(msg) => Ok(Json.obj("status" -> msg))
          case Right(checkout) => Created(Json.obj("status" -> "Checkout Updated", "checkout" -> checkout))
        }
      }
    )
  }

  def returnBook(checkoutId: Long): Action[AnyContent] = Action.async {
    checkoutService.createReturn(checkoutId).map {
      case Right(fine) => Ok(Json.obj("status" -> "Book returned successfully", "fine" -> fine))
      case Left(msg) => BadRequest(Json.obj("error" -> msg))
    }
  }
}
