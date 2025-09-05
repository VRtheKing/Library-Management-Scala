package services

import models.Notification
import repo.NotificationRepo

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NotificationService @Inject()(notificationRepo: NotificationRepo)(implicit ec: ExecutionContext) {
  def notify(message: String): Future[Int] = {
    println(s"[NOTIFICATION] -> $message")
    notificationRepo.logNotification(message)
  }
  def getAllNotifications(): Future[Seq[Notification]] = {
    notificationRepo.listNotifications
  }

}
