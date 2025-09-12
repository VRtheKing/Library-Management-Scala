package repo

import models.{Book, Checkout, CheckoutPatch}

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import java.time.LocalDate

class CheckoutRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  val checkouts = TableQuery[models.CheckoutModel]
  val books = TableQuery[models.BookModel]

  def createCheckout(checkout: Checkout): Future[Int] = {
    db.run(checkouts += checkout)
  }
  def findOverdueCheckouts(currentDate: LocalDate): Future[Seq[Checkout]] = {
    db.run(checkouts.filter(c => !c.returned && c.dueDate < currentDate).result)
  }

  def updateCheckout(updateCheckout: CheckoutPatch): Future[Either[String, Checkout]] = {
    val finder = checkouts.filter(_.id === updateCheckout.id)
    db.run(finder.result.headOption).flatMap {
      case None =>
        Future.successful(Left("Checkout Not Found"))
      case Some(existingCheckout) =>
        val updatedCheckout = existingCheckout.copy(
          userId = updateCheckout.userId.getOrElse(existingCheckout.userId),
          bookId = updateCheckout.bookId.getOrElse(existingCheckout.bookId),
          dueDate = updateCheckout.dueDate.getOrElse(existingCheckout.dueDate),
          returnDate = updateCheckout.returnDate.orElse(existingCheckout.returnDate),
          fine = updateCheckout.fine.orElse(existingCheckout.fine),
          returned = updateCheckout.returned.getOrElse(existingCheckout.returned)
        )
        if (updatedCheckout == existingCheckout) {
          Future.successful(Left("No changes are made"))
        } else {
          db.run(finder.update(updatedCheckout)).flatMap { _ =>
            db.run(finder.result.headOption).map {
              case Some(updated) => Right(updated)
            }
          }
        }
    }
  }


  def findPendingCheckouts(currentDate: LocalDate): Future[Seq[Checkout]] = {
    db.run(checkouts.filter((c => !c.returned && c.dueDate>currentDate)).result)
  }

  def returnBook(checkoutId: Long, returnDate: LocalDate, fine: Option[BigDecimal]): Future[Int] = {
    val query = checkouts.filter(_.id === checkoutId)
      .map(c => (c.returnDate, c.fine, c.returned))
      .update((Some(returnDate), fine, true))

    db.run(query)
  }

  def calculateFine(checkoutId: Long, fine: Option[BigDecimal]): Future[Int] = {
    val query = checkouts.filter(_.id === checkoutId)
      .map(c => (c.fine, c.returned))
      .update(fine, false)
    db.run(query)
  }

  def listCheckouts(): Future[Seq[Checkout]] = {
    db.run(checkouts.result)
  }

  def findById(id: Long): Future[Option[Checkout]] = {
    db.run(checkouts.filter(_.id === id).result.headOption)
  }
}
