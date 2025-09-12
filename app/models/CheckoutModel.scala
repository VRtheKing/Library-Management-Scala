package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._
import slick.model.ForeignKeyAction.Cascade

import java.time.LocalDate

case class Checkout(id: Option[Long], userId: Long, bookId: Long, dueDate: LocalDate, returnDate: Option[LocalDate], fine: Option[BigDecimal], returned: Boolean)
case class CheckoutPatch(id: Long, userId: Option[Long], bookId: Option[Long], dueDate: Option[LocalDate], returnDate: Option[LocalDate], fine: Option[BigDecimal], returned: Option[Boolean])

class CheckoutModel(tag: Tag) extends Table[Checkout](tag, "checkouts"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Long]("user_id")
  def bookId = column[Long]("book_id")
  def dueDate = column[LocalDate]("due_date")
  def returnDate = column[Option[LocalDate]]("return_date")
  def fine = column[Option[BigDecimal]]("fine")
  def returned = column[Boolean]("returned")

  val User = TableQuery[models.UserModel]
  val Book = TableQuery[models.BookModel]
  def userId_fk = foreignKey("userId_fk", userId, User)(_.id, onDelete = Cascade)
  def bookId_fk = foreignKey("bookId_fk", bookId, Book)(_.id, onDelete = Cascade)

  def * = (id.?, userId, bookId, dueDate, returnDate, fine, returned).mapTo[Checkout]
}

object Checkout {
  implicit val checkoutFormat: OFormat[Checkout] = Json.format[Checkout]
  implicit val checkoutPatchFormat: OFormat[CheckoutPatch] = Json.format[CheckoutPatch]
}
