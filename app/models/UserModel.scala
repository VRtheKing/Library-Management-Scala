package models

import java.time.LocalDateTime
import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._
import models.Role

case class User(
                 id: Option[Long],
                 name: String,
                 email: String,
                 passwordHash: String,
                 role: Role, // Admin, Librarian, User
                 createdAt: Option[LocalDateTime]
               )

case class UserPatch(id: Long, name: Option[String], email: Option[String], role: Option[String])

case class BorrowedBook(checkoutId: Long, title: String)

case class UserLogin(email: String, passwordHash: String)

class UserModel(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.Unique, O.AutoInc)

  def name = column[String]("name")

  def email = column[String]("email", O.Unique)

  def passwordHash = column[String]("passwordHash")

  def role = column[Role]("role_id")

  def createdAt = column[LocalDateTime]("created_at")

  def * = (id.?, name, email, passwordHash, role, createdAt.?).mapTo[User]

  def insertProjection() = (name, email, passwordHash, role) <> (
    (User(None, _, _, _, _, None)).tupled,
    (u: User) =>
      Some(
        (u.name, u.email, u.passwordHash, u.role)
      ) // Partial Projection for insertion without id and createdAt
  )
}

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val updateUserFormat: OFormat[UserPatch] = Json.format[UserPatch]
}

object BorrowedBook {
  implicit val format: OFormat[BorrowedBook] = Json.format[BorrowedBook]
}

object UserLogin {
  implicit val format: OFormat[UserLogin] = Json.format[UserLogin]
}