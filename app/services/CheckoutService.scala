package services

import models.Checkout
import repo.CheckoutRepo
import scala.concurrent.{ExecutionContext, Future}

class CheckoutService(checkoutRepo: CheckoutRepo)(implicit ec: ExecutionContext) {
  def createCheckout(checkout: Checkout): Future[Int] = {
    checkoutRepo.createCheckout(checkout)
  }
}
