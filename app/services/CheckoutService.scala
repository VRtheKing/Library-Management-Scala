package services

import models.{Book, Checkout}
import repo.{BookRepo, CheckoutRepo, UserRepo}

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckoutService @Inject()(checkoutRepo: CheckoutRepo, bookRepo: BookRepo, userRepo: UserRepo)(implicit ec: ExecutionContext) {

  def createCheckout(checkout: Checkout): Future[Either[String, Int]] = {
    userRepo.findById(checkout.userId).flatMap {
      case None => Future.successful(Left("User not found"))
      case Some(_) =>
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
  }

  def findOverdueCheckouts(): Future[Seq[Checkout]] = {
    checkoutRepo.findOverdueCheckouts(LocalDate.now())
  }

  def createReturn(checkoutId: Long): Future[Either[String, BigDecimal]] = {
    checkoutRepo.findById(checkoutId).flatMap {
      case Some(checkout) =>
        if (checkout.returned) {
          Future.successful(Left("Book already returned"))
        } else {
          val today = checkout.returnDate.getOrElse(LocalDate.now())
          val fine = if (today.isAfter(checkout.dueDate)) {
            val daysLate = ChronoUnit.DAYS.between(checkout.dueDate, today)
            BigDecimal(daysLate * 1)
          } else BigDecimal(0)

          checkoutRepo.returnBook(checkoutId, today, Some(fine)).flatMap { _ =>
            bookRepo.increaseStock(checkout.bookId).map { _ =>
              Right(fine)
            }
          }
        }
      case None => Future.successful(Left("Checkout not found"))
    }
  }

  def calculateFine(checkoutId: Long): Future[Int] = {
    checkoutRepo.findById(checkoutId).flatMap {
      case Some(checkout) =>
        val today = checkout.returnDate.getOrElse(LocalDate.now())
//        val today = checkout.returnDate > LocalDate.now() ? LocalDate.now() : checkout.returnDate
        val fine = if (today.isAfter(checkout.dueDate)) {
          val daysLate = ChronoUnit.DAYS.between(checkout.dueDate, today)
            Some(BigDecimal(daysLate * 1))
          } else None
      checkoutRepo.calculateFine(checkoutId, fine)
    }
  }
}
