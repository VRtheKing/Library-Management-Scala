package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

case class Notification(id: Option[Long], message: String, created_at: LocalDateTime)

class NotificationModel(tag: Tag) extends Table[Notification](tag, "notifications") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def message = column[String]("message")
  def created_at = column[LocalDateTime]("created_at")

  def * = (id.?, message, created_at).mapTo[Notification]
}

object Notification {
  implicit val format: OFormat[Notification] = Json.format[Notification]
}
