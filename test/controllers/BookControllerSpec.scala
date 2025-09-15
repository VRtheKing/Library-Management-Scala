package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

class BookControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  "BookController" should {

    "add a new book" in {
      val jsonBody = Json.obj("title" -> "Clean Code", "author" -> "Robert C. Martin","isbn"->"825-5-254-62352-1", "stock" -> 1)

      val request = FakeRequest(POST, "/books")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(jsonBody)

      val result = route(app, request).get

      status(result) mustBe CREATED
    }

    "get books" in {
      val request = FakeRequest(GET, "/books")
      val result = route(app, request).get

      status(result) mustBe OK
    }

    "update a book" in {
      val jsonBody = Json.obj("id" -> 1, "author" -> "Charlie")

      val request = FakeRequest(PATCH, "/books")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(jsonBody)

      val result = route(app, request).get

      status(result) mustBe OK
    }
  }
}
