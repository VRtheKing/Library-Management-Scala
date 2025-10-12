package models

import java.time.Instant
import slick.jdbc.PostgresProfile.api._
import models.UserModel
import play.api.libs.json.{Json, OFormat}

case class RefreshToken(
                         id: Long = 0L,
                         userId: Long,
                         token: String,
                         issuedAt: Instant,
                         expiresAt: Instant,
                         revoked: Boolean = false
                       )

class RefreshTokenModel(tag: Tag) extends Table[RefreshToken](tag, "refresh_tokens") {
  def id         = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId     = column[Long]("user_id")
  def token      = column[String]("token", O.Unique)
  def issuedAt   = column[Instant]("issued_at")
  def expiresAt  = column[Instant]("expires_at")
  def revoked    = column[Boolean]("revoked")

  def * = (id, userId, token, issuedAt, expiresAt, revoked).mapTo[RefreshToken]
  def userFK = foreignKey("fk_refresh_token_user", userId, TableQuery[UserModel])(_.id, onDelete = ForeignKeyAction.Cascade)
  def uniqueUser = index("idx_unique_user", userId, unique = true)
}

case class RefreshRequest(refreshToken: String)
case class LogoutRequest(refreshToken: String)
case class TokenPair(accessToken: String, refreshToken: String)

object RefreshRequest {
  implicit val format: OFormat[RefreshRequest] = Json.format[RefreshRequest]
}

object LogoutRequest {
  implicit val format: OFormat[LogoutRequest] = Json.format[LogoutRequest]
}