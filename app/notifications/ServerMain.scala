package notifications

import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import service.notification.notifications.{Notification, NotificationServiceGrpc, SubscribeRequest}
import services.{CheckoutService, UserService}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class NotificationServiceImpl(checkoutService: CheckoutService, userService: UserService)(implicit ec: ExecutionContext)
  extends NotificationServiceGrpc.NotificationService {

  override def subscribeNotifications(request: SubscribeRequest, responseObserver: StreamObserver[Notification]): Unit = {
    checkoutService.findOverdueCheckouts().onComplete {
      case Success(overdueList) =>
        val notificationFutures = overdueList.map { checkout =>
          val userId = checkout.userId
          val usernameFut = userService.getUsername(userId)
          val fineFut = checkout.id match {
            case Some(id) => checkoutService.calculateFine(id)
            case None     => Future.successful(0)
          }
          for {
            usernameOpt <- usernameFut
            fine <- fineFut
          } yield {
            val username = usernameOpt.getOrElse("Unknown User")
            print(fine, fineFut)
            val msg =
              s"User $username (Id: $userId) has an overdue book (Book ID: ${checkout.bookId}). Due: ${checkout.dueDate}. Fine: ₹$fine"
            responseObserver.onNext(Notification(message = msg))
          }
        }
        Future.sequence(notificationFutures).onComplete { _ =>
          responseObserver.onCompleted()
        }
      case Failure(ex) =>
        responseObserver.onError(ex)
    }
  }
}

object ServerMain extends App {
  val application: Application = new GuiceApplicationBuilder().build()
  implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]

  val checkoutService = application.injector.instanceOf[CheckoutService]
  val userService = application.injector.instanceOf[UserService]

  val notificationService = new NotificationServiceImpl(checkoutService, userService)

  val server: Server = ServerBuilder
    .forPort(50051)
    .addService(NotificationServiceGrpc.bindService(notificationService, ec))
    .build()
    .start()

  println("✅ Notification gRPC Server started on port 50051")
  server.awaitTermination()
}
