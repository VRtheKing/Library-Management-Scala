package models

import play.api.libs.json._
import slick.jdbc.PostgresProfile.api._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

sealed trait Role {
  def id: Int
  def name: String
}

object Role {
  case object User extends Role {
    val id = 1;
    val name = "USER"
  }

  case object Librarian extends Role {
    val id = 2;
    val name = "LIBRARIAN"
  }

  case object Admin extends Role {
    val id = 3;
    val name = "ADMIN"
  }

  val all: Seq[Role] = Seq(User, Librarian, Admin)

  def fromId(id: Int): Option[Role] = all.find(_.id == id)

  def fromName(name: String): Option[Role] = all.find(_.name.equalsIgnoreCase(name.trim))

  implicit val roleColumnType: JdbcType[Role] with BaseTypedType[Role] =
    MappedColumnType.base[Role, Int](
      _.id,
      id => fromId(id).getOrElse(throw new IllegalArgumentException(s"Invalid Role ID: $id"))
    )

  implicit val roleFormat: Format[Role] = new Format[Role] {
    def writes(role: Role): JsValue = JsString(role.name)

    def reads(json: JsValue): JsResult[Role] = json match {
      case JsString(name) =>
        Role.fromName(name).map(JsSuccess(_)).getOrElse(JsError(s"Invalid role name: $name"))
      case _ => JsError("Role must be a string")
    }
  }
}
