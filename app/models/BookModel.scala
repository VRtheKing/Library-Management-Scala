package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

case class Book(id: Option[Long], title: String, author: String, isbn:String, stock: Int, updated_at: Option[LocalDateTime], fine: Int)
case class BookPatch(id: Long, title: Option[String], author: Option[String], isbn: Option[String], stock: Option[Int])

// BookModel
class BookModel(tag: Tag) extends Table[Book](tag,"books"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def author = column[String]("author")
  def isbn = column[String]("isbn")
  def stock = column[Int]("stock")
  def updated_at = column[LocalDateTime]("updated_at")
  def fine = column[Int]("fine")
  def * = (id.?, title, author, isbn, stock, updated_at.?, fine).mapTo[Book] // Projection Mapping
}

object Book {
  implicit val bookFormat: OFormat[Book] = Json.format[Book]
  implicit val bookPatchFormat: OFormat[BookPatch] = Json.format[BookPatch] // Book patch serialization
}