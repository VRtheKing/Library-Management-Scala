package services

import models.{Checkout, Book}
import repo.{CheckoutRepo, BookRepo}

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckoutService @Inject()(checkoutRepo: CheckoutRepo, bookRepo: BookRepo)(implicit ec: ExecutionContext) {

  def createCheckout(checkout: Checkout): Future[Either[String, Int]] = {
    bookRepo.isOutOfStock(checkout.bookId).flatMap {
      case true => Future.successful(Left("Book is out of stock"))
      case false =>
        checkoutRepo.createCheckout(checkout).flatMap { _ =>
          bookRepo.decreaseStock(checkout.bookId).map { _ =>
            Right(1)
          }
        }
    }
  }
  def findOverdueCheckouts(): Future[Seq[Checkout]] = {
    checkoutRepo.findOverdueCheckouts(LocalDate.now())
  }

  def createReturn(checkoutId: Long): Future[Either[String, Int]] = {
    checkoutRepo.findById(checkoutId).flatMap {
      case Some(checkout) =>
        if (checkout.returned) {
          Future.successful(Left("Book already returned"))
        } else {
          val today = checkout.returnDate.getOrElse(LocalDate.now())
          val fine = if (today.isAfter(checkout.dueDate)) {
            val daysLate = ChronoUnit.DAYS.between(checkout.dueDate, today)
            Some(BigDecimal(daysLate * 1))
          } else None

          checkoutRepo.returnBook(checkoutId, today, fine).flatMap { _ =>
            bookRepo.increaseStock(checkout.bookId).map { _ =>
              Right(1)
            }
          }
        }
      case None => Future.successful(Left("Checkout not found"))
    }
  }
}
