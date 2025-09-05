package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

case class Book(id: Option[Long], title: String, author: String, stock: Int)

class BookModel(tag: Tag) extends Table[Book](tag,"books"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def author = column[String]("author")
  def stock = column[Int]("stock")
  def * = (id.?, title, author, stock) <> (Book.tupled, Book.unapply)
}

object Book {
  implicit val bookFormat: OFormat[Book] = Json.format[Book]
}