package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.Checkout
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

  def returnBook(checkoutId: Long): Action[AnyContent] = Action.async {
    checkoutService.createReturn(checkoutId).map {
      case Right(fine) => Ok(Json.obj("status" -> "Book returned successfully", "fine" -> fine))
      case Left(msg) => BadRequest(Json.obj("error" -> msg))
    }
  }
}
