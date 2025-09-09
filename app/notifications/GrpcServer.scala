package notifications

import io.grpc.stub.StreamObserver

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import service.notification.notifications.{Notification, NotificationServiceGrpc, SubscribeRequest}
import services.{CheckoutService, UserService}

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

@Singleton
class GrpcServer @Inject() (checkoutService: CheckoutService, userService: UserService, lifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext) {

  private val notificationService = new NotificationServiceImpl(checkoutService, userService)

  private val server: Server = ServerBuilder
    .forPort(50051)
    .addService(NotificationServiceGrpc.bindService(notificationService, ec))
    .build()
    .start()

  println("Notification gRPC Server started on port 50051")
}
