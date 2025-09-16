package services

import models.Notification
import repo.NotificationRepo

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NotificationService @Inject()(notificationRepo: NotificationRepo)(implicit ec: ExecutionContext) {
  def notify(message: String): Future[Int] = {
    println(s"[NOTIFICATION] -> $message") // Console Notification
    notificationRepo.logNotification(message) // Logs the notification to the console and DB
  }
  def getAllNotifications(): Future[Seq[Notification]] = {
    notificationRepo.listNotifications // Get all notification from the DB
  }

}
