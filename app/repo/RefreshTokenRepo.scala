package repo

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

import models.{RefreshToken, RefreshTokenModel}
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

@Singleton
class RefreshTokenRepo @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[slick.jdbc.PostgresProfile] {

  private val refreshTokens = TableQuery[RefreshTokenModel]

  def upsert(token: RefreshToken): Future[RefreshToken] = {
    val action = for {
      _ <- refreshTokens.filter(_.userId === token.userId).delete
      id <- (refreshTokens returning refreshTokens.map(_.id)) += token
    } yield token.copy(id = id)
    db.run(action.transactionally)
  }

  def findByToken(tokenStr: String): Future[Option[RefreshToken]] = {
    db.run(
      refreshTokens.filter(_.token === tokenStr).result.headOption
    )
  }

  def revokeByUserId(userId: Long): Future[Int] = {
    db.run(
      refreshTokens
        .filter(rt => rt.userId === userId && rt.revoked === false)
        .map(_.revoked)
        .update(true)
    )
  }

  def deleteExpired(): Future[Int] = {
    val now = Instant.now()
    db.run(
      refreshTokens
        .filter(rt => rt.expiresAt < now)
        .delete
    )
  }
}
