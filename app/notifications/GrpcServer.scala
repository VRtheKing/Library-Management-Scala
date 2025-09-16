package notifications

import io.grpc.stub.StreamObserver

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import services.{CheckoutService, UserService}
import com.typesafe.config.ConfigFactory
import service.notification.notifications._

import scala.util.{Failure, Success}

class NotificationServiceImpl(checkoutService: CheckoutService, userService: UserService)(implicit ec: ExecutionContext)
  extends NotificationServiceGrpc.NotificationService {
  override def subscribeNotifications(request: SubscribeRequest, responseObserver: StreamObserver[Notification]): Unit = {
    checkoutService.findOverdueCheckouts().onComplete {
      case Success(overdueList) =>
        val groupedByUser = overdueList.groupBy(_.userId)

        val userNotificationsFutures = groupedByUser.map { case (userId, checkouts) =>
          val usernameFut = userService.getUsername(userId) // Get the username of the user

          val finesFut = Future.sequence(checkouts.map { checkout =>
            checkout.id match {
              case Some(id) => checkoutService.calculateFine(id) // calculate the fine for the user
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
              ) // Get all the overdue books of the user
            }.toSeq

            val notification = Notification(
              username = username,
              userId = userId,
              overdueBooks = overdueBooks
            ) // Create the RPC stream of the notification
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
  private val config = ConfigFactory.load()
  private val gRPC_PORT: Int = config.getInt("gRPC.port")
  private val server: Server = ServerBuilder
    .forPort(gRPC_PORT) // Port from application.conf
    .addService(NotificationServiceGrpc.bindService(notificationService, ec))
    .build()
    .start()

  println(s"Notification gRPC Server started on port $gRPC_PORT")

  lifecycle.addStopHook { () => // Hook to ensure proper shutting down of server
    println("Shutting down gRPC server...")
    Future {
      server.shutdown()
      server.awaitTermination()
      println("gRPC server down.")
    }
  }
}
