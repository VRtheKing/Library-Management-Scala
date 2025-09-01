package jobs

import services.{CheckoutService, NotificationService}

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.apache.pekko.actor.ActorSystem
import java.time.LocalDate

import models.Checkout
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import java.time.LocalDate

@Singleton
class OverdueCheckerJob @Inject()(actorSystem: ActorSystem, checkoutService: CheckoutService, notificationService: NotificationService)(implicit ec: ExecutionContext) {

  val checkouts = TableQuery[models.CheckoutModel]

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 40.seconds) { () =>
    println("[JOB] Checking for overdue checkouts...")

    checkoutService.findOverdueCheckouts().foreach { overdueCheckouts =>
      overdueCheckouts.foreach { checkout =>
        val msg = s"User ${checkout.userId} has an overdue book (Book ID: ${checkout.bookId}). Due: ${checkout.dueDate}"
        notificationService.notify(msg)
        checkout.id.foreach( id =>
          checkoutService.calculateFine(id)
        )
      }
    }
  }
}
