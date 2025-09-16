package repo

import models.Notification
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  val notifications = TableQuery[models.NotificationModel]

  def logNotification(message: String): Future[Int] = {
    val notification = Notification(None, message, LocalDateTime.now()) // Creates notification
    db.run(notifications += notification) // logs into DB
  }

  def listNotifications: Future[Seq[Notification]] = {
    db.run(notifications.result) // get all notification
  }
}
