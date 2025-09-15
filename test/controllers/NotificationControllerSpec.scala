package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test._
import play.api.test.Helpers._

class NotificationControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  "NotificationController" should {

    "get notifications" in {
      val request = FakeRequest(GET, "/notifications")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
    }
  }
}
