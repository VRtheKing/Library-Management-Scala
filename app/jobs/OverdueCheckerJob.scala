package jobs

import services.{CheckoutService, NotificationService, UserService}

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.apache.pekko.actor.ActorSystem

@Singleton
class OverdueCheckerJob @Inject()(actorSystem: ActorSystem, checkoutService: CheckoutService, notificationService: NotificationService, userService: UserService)(implicit ec: ExecutionContext) {

  println("[JOB] OD Constructor Init")

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 5.minutes) { () => // Code runs every 5 minutes
    println("[JOB] Checking for overdue checkouts...")

    checkoutService.findOverdueCheckouts().foreach { overdueCheckouts =>
      overdueCheckouts.foreach { checkout => // finds all the overdue checkouts
        userService.getUsername(checkout.userId).foreach {
          case Some(username) =>
            val msg = s"User $username (Id: ${checkout.userId}) has an overdue book (Book ID: ${checkout.bookId}). Due: ${checkout.dueDate}"
            notificationService.notify(msg) // Sends out the notification
          case None =>
            val msg = s"UserId: ${checkout.userId} (username not found) has an overdue book (Book ID: ${checkout.bookId}). Due: ${checkout.dueDate}"
            notificationService.notify(msg) // Sends out the notification
        }
        checkout.id.foreach( id =>
          checkoutService.calculateFine(id) // Updates the fine to the DB
        )
      }
    }
  }
}
