import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

class AppControllerSpec extends PlaySpec with GuiceOneAppPerTest {
  var token = ""

  // Create User Test
  "POST /users" should {
    "create a new user" in {
      val jsonBody = Json.obj(
        "name" -> "Alice",
        "email" -> "alice@example.com",
        "passwordHash" -> "12345678",
        "role" -> "ADMIN"
      )

      val request = FakeRequest(POST, "/users")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe CREATED
      (contentAsJson(result) \ "status").as[String] mustBe "User created"
    }
  }

  // Login
  "POST /login" should {
    "login user" in {
      val jsonBody = Json.obj(
        "email" -> "alice@example.com",
        "passwordHash" -> "12345678"
      )

      val request = FakeRequest(POST, "/login")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe CREATED
      (contentAsJson(result) \ "status").as[String] mustBe "User Logged In"
      token = (contentAsJson(result) \ "accessToken").as[String]
    }
  }

  // Fetch Users Test
  "GET /users" should {
    "return a list of users" in {
      val request = FakeRequest(GET, "/users")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
      print(token)
      val result = route(app, request).get
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }

  // Update User Test
  "PATCH /users" should {
    "update an existing user" in {
      val jsonBody = Json.obj(
        "id" -> 1,
        "name" -> "Alice",
        "email" -> "alice@example.com"
      )

      val request = FakeRequest(PATCH, "/users")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe OK
    }
  }

  // Add Book Test
  "POST /books" should {
    "add a new book" in {
      val jsonBody = Json.obj(
        "title" -> "Clean Code",
        "author" -> "Robert C. Martin",
        "isbn" -> "978-1-56619-909-4",
        "stock" -> 1,
        "fine" -> 1
      )

      val request = FakeRequest(POST, "/books")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe CREATED
      (contentAsJson(result) \ "status").as[String] mustBe "Book created"
    }
  }

  // Get Books Test
  "GET /books" should {
    "retrieve a list of books" in {
      val request = FakeRequest(GET, "/books")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }

  // Update Book Test
  "PATCH /books" should {
    "update an existing book" in {
      val jsonBody = Json.obj(
        "id" -> 1,
        "title" -> "La La Land",
        "author" -> "Kip Thorne"
      )

      val request = FakeRequest(PATCH, "/books")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe OK
    }
  }

  // Checkout Normal Test
  "POST /checkouts" should {
    "checkout a book successfully" in {
      val jsonBody = Json.obj(
        "userId" -> 1,
        "bookId" -> 1,
        "dueDate" -> "2025-11-17",
        "returnDate" -> null,
        "fine" -> null,
        "returned" -> false
      )

      val request = FakeRequest(POST, "/checkouts")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe CREATED
    }
  }

  // Checkout Past Date Test
  "POST /checkouts" should {
    "fail to checkout a book for a past due date" in {
      val jsonBody = Json.obj(
        "userId" -> 1,
        "bookId" -> 1,
        "dueDate" -> "2025-11-17",
        "returnDate" -> null,
        "fine" -> null,
        "returned" -> false
      )

      val request = FakeRequest(POST, "/checkouts")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }
  }

  // Checkout Out of Stock Test
  "POST /checkouts" should {
    "fail to checkout a book if out of stock" in {
      val jsonBody = Json.obj(
        "userId" -> 1,
        "bookId" -> 1,
        "dueDate" -> "2025-11-01",
        "returnDate" -> null,
        "fine" -> null,
        "returned" -> false
      )

      val request = FakeRequest(POST, "/checkouts")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")
        .withBody(jsonBody)

      val result = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }
  }

  // Get Checkouts Test
  "GET /checkouts" should {
    "retrieve a list of checkouts" in {
      val request = FakeRequest(GET, "/checkouts?status=ALL")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }

  // Return Book Test
  "POST /checkouts/1/return" should {
    "return a checked out book" in {
      val request = FakeRequest(POST, "/checkouts/1/return")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
    }
  }

  // Duplicate Return Test
  "POST /checkouts/2/return" should {
    "fail to return a book that is already returned" in {
      val request = FakeRequest(POST, "/checkouts/1/return")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }
  }

  // Non Existing Return Test
  "POST /checkouts/74/return" should {
    "fail to return a checkout that does not exist" in {
      val request = FakeRequest(POST, "/checkouts/74/return")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe BAD_REQUEST
    }
  }

  // Notifications Test
  "GET /notifications" should {
    "retrieve user notifications" in {
      val request = FakeRequest(GET, "/notifications")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }

  // Borrowed Books By User Test
  "GET /borrowedBooks/1" should {
    "retrieve borrowed books for a user" in {
      val request = FakeRequest(GET, "/borrowedBooks/1")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
    }
  }

  // Delete User Test
  "DELETE /users/1" should {
    "delete a user" in {
      val request = FakeRequest(DELETE, "/users/1")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
    }
  }

  // Delete Book Test
  "DELETE /books/1" should {
    "delete a book" in {
      val request = FakeRequest(DELETE, "/books/1")
        .withHeaders("X-Requested-With" -> "XMLHttpRequest","Authorization" -> s"Bearer $token")

      val result = route(app, request).get
      status(result) mustBe OK
    }
  }
}
