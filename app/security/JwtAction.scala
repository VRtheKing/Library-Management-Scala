package security

import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

case class JwtRequest[A](claims: Map[String, String], request: Request[A]) extends WrappedRequest[A](request)

class JwtAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilder[JwtRequest, AnyContent] {
  override def parser: BodyParser[AnyContent] = parser
  override protected def executionContext: ExecutionContext = ec

  private val bearerTokenPrefix = "Bearer "

  override def invokeBlock[A](request: Request[A], block: JwtRequest[A] => Future[Result]): Future[Result] = {
    request.headers.get("Authorization") match {
      case Some(header) if header.startsWith(bearerTokenPrefix) =>
        val token = header.substring(bearerTokenPrefix.length)
        JwtUtil.validateToken(token, JwtUtil.SecretKey) match {
          case Some(claims) => block(JwtRequest(claims, request))
          case None => Future.successful(Results.Unauthorized("Invalid or expired token"))
        }
      case _ => Future.successful(Results.Unauthorized("Authorization header missing or malformed"))
    }
  }
}
