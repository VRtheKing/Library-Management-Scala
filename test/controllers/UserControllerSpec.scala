package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

class UserControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  "UserController" should {

    "create a new user" in {
      val jsonBody = Json.obj("name" -> "Alice", "email" -> "alice3@example.com")

      val request = FakeRequest(POST, "/users")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(jsonBody)

      val result = route(app, request).get

      status(result) mustBe CREATED
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("User created")
    }

    "fetch all users" in {
      val request = FakeRequest(GET, "/users")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }

    "update a user" in {
      val jsonBody = Json.obj("id" -> 2, "name" -> "Alice", "email" -> "alice2@example.com")

      val request = FakeRequest(PATCH, "/users")
        .withHeaders("Content-Type" -> "application/json")
        .withJsonBody(jsonBody)

      val result = route(app, request).get

      status(result) mustBe CREATED
    }
    "get borrowed books by user ID" in {
      val request = FakeRequest(GET, "/borrowedBooks?userId=2")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }
}
