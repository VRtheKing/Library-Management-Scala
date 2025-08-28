package repo

import models.Checkout

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.Inject

class CheckoutRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  val checkouts = TableQuery[models.CheckoutModel]
  def createCheckout(checkout: Checkout): Future[Int] = {
    db.run(checkouts += checkout)
  }
}
