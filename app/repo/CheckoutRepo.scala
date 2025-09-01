package repo

import models.{Book, Checkout}

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

  def findById(id: Long): Future[Option[Checkout]] = {
    db.run(checkouts.filter(_.id === id).result.headOption)
  }
}
