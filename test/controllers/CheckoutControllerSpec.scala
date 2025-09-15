package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

class CheckoutControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  "CheckoutController" should {

    "create a normal checkout" in {
      val jsonBody = Json.obj(
        "userId" -> 2,
        "bookId" -> 3,
        "dueDate" -> "2025-08-17",
        "returnDate" -> JsNull,
        "fine" -> JsNull,
        "returned" -> false
      )

      val request = FakeRequest(POST, "/checkouts")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(jsonBody)

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
    }

//    "return a book" in {
//      val request = FakeRequest(POST, "/checkouts/6/return")
//      val result = route(app, request).get
//
//      status(result) mustBe OK
//    }

//    "return a late book" in {
//      val request = FakeRequest(POST, "/checkouts/3/return")
//      val result = route(app, request).get
//
//      status(result) mustBe OK
//    }

    "duplicate return should fail" in {
      val request = FakeRequest(POST, "/checkouts/2/return")
      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
    }

    "non-existing checkout return should return 400" in {
      val request = FakeRequest(POST, "/checkouts/74/return")
      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
    }

    "get checkouts by status" in {
      val request = FakeRequest(GET, "/checkouts?status=PENDING")
      val result = route(app, request).get

      status(result) mustBe OK
    }
  }
}
