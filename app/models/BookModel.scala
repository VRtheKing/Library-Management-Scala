package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

case class Book(id: Option[Long], title: String, author: String, isbn:String, stock: Int)
case class BookPatch(id: Long, title: Option[String], author: Option[String], isbn: Option[String], stock: Option[Int])

class BookModel(tag: Tag) extends Table[Book](tag,"books"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def author = column[String]("author")
  def isbn = column[String]("isbn")
  def stock = column[Int]("stock")
  def * = (id.?, title, author, isbn, stock).mapTo[Book]
}

object Book {
  implicit val bookFormat: OFormat[Book] = Json.format[Book]
  implicit val bookPatchFormat: OFormat[BookPatch] = Json.format[BookPatch]
}