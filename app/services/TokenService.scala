package services

import models.{RefreshToken, User}
import repo.{RefreshTokenRepo, UserRepo}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TokenService @Inject()(
                                     refreshTokenRepo: RefreshTokenRepo,
                                     userRepo: UserRepo
                                   )(implicit ec: ExecutionContext) {

  private val refreshTokenTtl = 30 * 24 * 60 * 60 // 30 days in seconds

  def generateRefreshToken(user: User): Future[String] = {
    val userId: Long = user.id.getOrElse(
      throw new IllegalArgumentException("User ID is required for refresh token")
    )
    val token = UUID.randomUUID().toString + UUID.randomUUID().toString
    val issuedAt = Instant.now()
    val expiresAt = issuedAt.plusSeconds(refreshTokenTtl)

    val refreshToken = RefreshToken(
      userId = userId,
      token = token,
      issuedAt = issuedAt,
      expiresAt = expiresAt,
      revoked = false
    )

    refreshTokenRepo.upsert(refreshToken).map(_ => token)
  }

  def validateRefreshToken(token: String): Future[Either[String, User]] = {
    refreshTokenRepo.findByToken(token).flatMap {
      case Some(rt) if !rt.revoked && rt.expiresAt.isAfter(Instant.now()) =>
        userRepo.findById(rt.userId).map {
          case Some(user) => Right(user)
          case None => Left("User not found")
        }
      case _ => Future.successful(Left("Invalid or expired refresh token"))
    }
  }

  def revokeToken(token: String): Future[Unit] = {
    refreshTokenRepo.findByToken(token).flatMap {
      case Some(rt) => refreshTokenRepo.revokeByUserId(rt.userId).map(_ => ())
      case None     => Future.unit
    }
  }
}
