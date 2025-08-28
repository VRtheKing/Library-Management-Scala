package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models.Book
import services.CheckoutService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckoutController @Inject()(cc: ControllerComponents, checkoutService: CheckoutService)(implicit ec: ExecutionContext) extends AbstractController(cc){

}
