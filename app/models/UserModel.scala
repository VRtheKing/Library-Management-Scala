package models

import java.time.LocalDateTime
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

case class User(id: Option[Long], name: String, email: String, createdAt: Option[LocalDateTime])
case class UserPatch(id: Long, name: Option[String], email: Option[String])
case class BorrowedBook(checkoutId: Long, title: String)

class UserModel(tag: Tag) extends Table[User](tag, "users"){
  def id = column[Long]("id", O.Unique, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def createdAt = column[LocalDateTime]("created_at")

  def * = (id.?, name, email, createdAt.?).mapTo[User]
  def insertProjection() = (name, email) <> (
    (User(None, _, _, None)).tupled,
    (u: User) => Some((u.name, u.email))
  )
}

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val updateUserFormat: OFormat[UserPatch] = Json.format[UserPatch]
}

object BorrowedBook {
  implicit val format: OFormat[BorrowedBook] = Json.format[BorrowedBook]
}