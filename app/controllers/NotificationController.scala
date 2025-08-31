package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.NotificationService
import models.Notification
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationController @Inject()(cc: ControllerComponents, notificationService: NotificationService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def getNotifications: Action[AnyContent] = Action.async {
    notificationService.getAllNotifications().map { notifications =>
      Ok(Json.toJson(notifications))
    }
  }
}
