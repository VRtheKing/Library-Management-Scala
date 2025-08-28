package models

import java.time.LocalDate

case class CheckoutModel(id: Option[Long], userId: Long, bookId: Long, dueDate: LocalDate, returnDate: Option[LocalDate], fine: Option[BigDecimal], returned: Boolean)
