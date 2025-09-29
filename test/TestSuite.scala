import controllers.{
  BookControllerSpec,
  CheckoutControllerSpec,
  UserControllerSpec
}
import org.scalatest.Suites

class TestSuite
    extends Suites(
      new UserControllerSpec,
      new BookControllerSpec,
      new CheckoutControllerSpec
    )
