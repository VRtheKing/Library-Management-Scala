package services

import models.{Checkout, CheckoutPatch}
import repo.{BookRepo, CheckoutRepo, UserRepo}

import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckoutService @Inject()(checkoutRepo: CheckoutRepo, bookRepo: BookRepo, userRepo: UserRepo)(implicit ec: ExecutionContext) {

  def createCheckout(checkout: Checkout): Future[Either[String, Int]] = {
    val queryTransaction: DBIO[Either[String, Int]] = for {
      user <- userRepo.users.filter(_.id === checkout.userId).result.headOption
      result <- user match {
        case None => DBIO.successful(Left("User not found"))

        case Some(_) =>
          for {
            stockAvl <- bookRepo.books.filter(_.id === checkout.bookId).map(_.stock).forUpdate.result.headOption
            outcome <- stockAvl match {
              case None => DBIO.successful(Left("Book not found"))
              case Some(stock) if stock <= 0 => DBIO.successful(Left("Book out of stock"))
              case Some(_) if LocalDate.now().isAfter(checkout.dueDate) =>
                DBIO.successful(Left("Due date is in the Past"))
              case Some(stock) =>
                for {
                  _ <- checkoutRepo.checkouts += checkout
                  _ <- bookRepo.books.filter(_.id === checkout.bookId).map(_.stock).update(stock - 1)
                } yield Right(1)
            }
          } yield outcome
      }
    } yield result
    checkoutRepo.createCheckout(queryTransaction)
  }


  def findOverdueCheckouts(): Future[Seq[Checkout]] = {
    checkoutRepo.findOverdueCheckouts(LocalDate.now())
  }

  def updateCheckout(update: CheckoutPatch): Future[Either[String, Checkout]] = {
    checkoutRepo.findById(update.id).flatMap {
      case None => Future.successful(Left("Checkout not found"))
      case Some(existingCheckout) =>
        val updatedBookId = update.bookId.getOrElse(existingCheckout.bookId)

        val resetOldStockF = if (updatedBookId != existingCheckout.bookId) {
          bookRepo.increaseStock(existingCheckout.bookId) // rollback old book's stock
        } else Future.successful(())

        val decreaseNewStockF = if (updatedBookId != existingCheckout.bookId) {
          bookRepo.decreaseStock(updatedBookId) // decrease new book's stock
        } else Future.successful(())

        for {
          _ <- resetOldStockF
          _ <- decreaseNewStockF
          result <- checkoutRepo.updateCheckout(update) // update the checkout
        } yield result
    }
  }

  def listCheckouts(status: String): Future[Seq[Checkout]] = {
    status match {
      case "ALL" => checkoutRepo.listCheckouts() // Gets all the checkouts
      case "PENDING" => checkoutRepo.findPendingCheckouts(LocalDate.now()) // Gets the checkouts that are pending
      case "OVERDUE" => checkoutRepo.findOverdueCheckouts(LocalDate.now()) // Gets the checkouts that are overdue
      case _ => Future.failed(new IllegalArgumentException("Invalid Query Option"))
    }
  }

  // Create a Return
  def createReturn(checkoutId: Long): Future[Either[String, BigDecimal]] = {
    checkoutRepo.findById(checkoutId).flatMap {
      case Some(checkout) =>
        if (checkout.returned) {
          Future.successful(Left("Book already returned")) // If a duplicate return request is made
        } else {
          val today = checkout.returnDate.getOrElse(LocalDate.now())
          bookRepo.getBookFine(checkout.bookId).flatMap{ fine_amt =>
          val fine = if (today.isAfter(checkout.dueDate)) {
            val daysLate = ChronoUnit.DAYS.between(checkout.dueDate, today)
            BigDecimal(daysLate * fine_amt) // Logic for calculating the fine
          } else BigDecimal(0) // 0 fine if there is no delay in return from teh dueDate
          checkoutRepo.returnBook(checkoutId, today, Some(fine)).flatMap { _ =>
            bookRepo.increaseStock(checkout.bookId).map { _ => // Increase the stock is the return is successful
              Right(fine)
            }
          }
        }
      }
      case None => Future.successful(Left("Checkout not found")) // Returns Left if no checkout is found on the id
    }
  }

  // Calculate fine based on the no_of_days and the fine_per_day of the book
  def calculateFine(checkoutId: Long): Future[Int] = {
    checkoutRepo.findById(checkoutId).flatMap {
      case Some(checkout) =>
        val today = checkout.returnDate.getOrElse(LocalDate.now())
        bookRepo.getBookFine(checkout.bookId).flatMap{ fine_amt => // Gets the fine amount of that book for a day from DB
          val fineAmount = if (today.isAfter(checkout.dueDate)) {
            val daysLate = ChronoUnit.DAYS.between(checkout.dueDate, today)
            daysLate.toInt * fine_amt // Logic for calculating the fine
          } else {
            0 // 0 fine if there is no delay in return from teh dueDate
          }
        checkoutRepo.calculateFine(checkoutId, Some(fineAmount)).map(_ => fineAmount) // Updates the Fine to the DB
        }
      case None =>
        Future.successful(0)
    }
  }
}
