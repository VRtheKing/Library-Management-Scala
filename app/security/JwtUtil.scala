package security

import com.typesafe.config.ConfigFactory
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import io.circe.parser._
import io.circe.syntax._

import java.time.Instant
import scala.util._


object JwtUtil {
  private val config = ConfigFactory.load()
  private val secret: String = config.getString("jwt.secret")
  val SecretKey = secret
  val Ttl = 180 // seconds

  def createToken(secretKey: String, data: Map[String, String]): String = {
    val claimsJson = data.asJson.noSpaces
    val claim = JwtClaim(
      content = claimsJson,
      issuedAt = Some(Instant.now.getEpochSecond),
      expiration = Some(Instant.now.plusSeconds(Ttl).getEpochSecond)
    )
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }

  def validateToken(token: String, secretKey: String): Option[Map[String, String]] = {
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(claim) =>
        decode[Map[String, String]](claim.content) match {
          case Right(data) => Some(data)
          case Left(_) => None
        }
      case Failure(_) => None
    }
  }

  def hasRole[A](requiredRoles: List[String])(request: JwtRequest[A]): Boolean = {
//    println(request.toString())
    requiredRoles.contains(request.role)
  }
}
