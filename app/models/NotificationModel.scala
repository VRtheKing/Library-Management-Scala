package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

case class Notification(id: Option[Long], message: String, timestamp: LocalDateTime)

class NotificationModel(tag: Tag) extends Table[Notification](tag, "notifications") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")
  def timestamp = column[LocalDateTime]("timestamp")

  def * = (id.?, message, timestamp) <> (Notification.tupled, Notification.unapply)
}

object Notification {
  implicit val format: OFormat[Notification] = Json.format[Notification]
}
