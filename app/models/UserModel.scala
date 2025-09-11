package models

import java.time.LocalDateTime
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

case class User(id: Option[Long], name: String, email: String, createdAt: Option[LocalDateTime])

class UserModel(tag: Tag) extends Table[User](tag, "users"){
  def id = column[Long]("id", O.Unique, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def createdAt = column[LocalDateTime]("created_at")

  def * = (id.?, name, email, createdAt.?).mapTo[User]
}

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}

