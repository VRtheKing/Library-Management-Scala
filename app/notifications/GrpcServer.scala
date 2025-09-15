package notifications

import io.grpc.stub.StreamObserver

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import services.{CheckoutService, UserService}

import service.notification.notifications._
import scala.util.{Failure, Success}

class NotificationServiceImpl(checkoutService: CheckoutService, userService: UserService)(implicit ec: ExecutionContext)
  extends NotificationServiceGrpc.NotificationService {
  override def subscribeNotifications(request: SubscribeRequest, responseObserver: StreamObserver[Notification]): Unit = {
    checkoutService.findOverdueCheckouts().onComplete {
      case Success(overdueList) =>
        val groupedByUser = overdueList.groupBy(_.userId)

        val userNotificationsFutures = groupedByUser.map { case (userId, checkouts) =>
          val usernameFut = userService.getUsername(userId)

          val finesFut = Future.sequence(checkouts.map { checkout =>
            checkout.id match {
              case Some(id) => checkoutService.calculateFine(id)
              case None     => Future.successful(0)
            }
          })

          for {
            usernameOpt <- usernameFut
            fines <- finesFut
          } yield {
            val username = usernameOpt.getOrElse("Unknown User")

            val overdueBooks = checkouts.zip(fines).map { case (checkout, fine) =>
              service.notification.notifications.OverdueBook(
                bookId = checkout.bookId,
                dueDate = checkout.dueDate.toString,
                fine = fine
              )
            }.toSeq

            val notification = Notification(
              username = username,
              userId = userId,
              overdueBooks = overdueBooks
            )
            responseObserver.onNext(notification)
          }
        }

        Future.sequence(userNotificationsFutures).onComplete { _ =>
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

  lifecycle.addStopHook { () =>
    println("Shutting down gRPC server...")
    Future {
      server.shutdown()
      server.awaitTermination()
      println("gRPC server down.")
    }
  }
}
