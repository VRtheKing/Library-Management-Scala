package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

case class User(id: Option[Long], name: String)

class UserModel(tag: Tag) extends Table[User](tag, "users"){
  def id = column[Long]("id", O.Unique, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (User.tupled, User.unapply)
}

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}

