package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

class LibraryApiSpec extends PlaySpec with GuiceOneAppPerTest {

  "Library API" should {

    "create a user" in {
      val json = Json.obj("name" -> "Alice", "email" -> "alice@example.com")
      val request = FakeRequest(POST, "/users").withJsonBody(json)

      val result = route(app, request).get

      status(result) mustBe CREATED
      contentType(result) mustBe Some("application/json")
    }

    "add a book" in {
      val json = Json.obj("title" -> "Clean Code", "author" -> "Robert C. Martin", "stock" -> 2)
      val request = FakeRequest(POST, "/books").withJsonBody(json)

      val result = route(app, request).get

      status(result) mustBe CREATED
    }

    "get books" in {
      val request = FakeRequest(GET, "/books")
      val result = route(app, request).get

      status(result) mustBe OK
    }

    "checkout book" in {
      val json = Json.obj(
        "userId" -> 1,
        "bookId" -> 1,
        "dueDate" -> "2025-08-21",
        "returned" -> false
      )
      val request = FakeRequest(POST, "/checkouts").withJsonBody(json)

      val result = route(app, request).get

      status(result) mustBe CREATED
    }

    "return book" in {
      val request = FakeRequest(POST, "/checkouts/1/return")
      val result = route(app, request).get

      status(result) must (be(OK) or be(NO_CONTENT))
    }

    "get notifications" in {
      val request = FakeRequest(GET, "/notifications")
      val result = route(app, request).get

      status(result) mustBe OK
    }

    "borrowed books by user" in {
      val request = FakeRequest(GET, "/borrowedBooks/1")
      val result = route(app, request).get

      status(result) mustBe OK
    }
  }
}
