package services

import models.{BorrowedBook, TokenPair, User, UserLogin, UserPatch}
import play.api.libs.json.Json
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}
import repo.UserRepo
import security.JwtUtil
import com.github.t3hnar.bcrypt._

import javax.inject.Inject

class UserService @Inject() (tokenService: TokenService, userRepo: UserRepo)(implicit
    ec: ExecutionContext
) {
  def createUser(user: User): Future[Int] = {
    userRepo.createUser(user.copy(passwordHash = user.passwordHash.boundedBcrypt)) // Creates a user
  }

  def listUser(): Future[Seq[User]] = {
    userRepo.listUsers() // Lists all users
  }

  def getUsername(userId: Long): Future[Option[String]] = {
    userRepo.findById(userId).map {
      case Some(user) => Some(user.name) // Returns username if exists
      case None => None // None if user not found
    }
  }

  def listBorrowedBooks(userId: Long): Future[Seq[BorrowedBook]] = {
    userRepo.listBorrowedBooks(userId) // Gets all books borrowed by user
  }

  def updateUser(updatedUser: UserPatch): Future[Either[String, User]] = {
    userRepo.updateUser(updatedUser) // Updates the user
  }

  def deleteUser(userId: Long): Future[Int] = {
    userRepo.deleteUser(userId) // Delete User
  }

  def loginUser(user: UserLogin): Future[Either[String, TokenPair]] = {
    userRepo.validateUser(user).flatMap {
      case Some(returnedUser) =>
        if (user.passwordHash.isBcryptedBounded(returnedUser.passwordHash)) {
          val claims = Map("email" -> returnedUser.email, "role" -> returnedUser.role.name)
          val accessToken = JwtUtil.createToken(JwtUtil.SecretKey, claims)
          tokenService.generateRefreshToken(returnedUser).map { refreshToken =>
            Right(TokenPair(accessToken, refreshToken))
          }
        } else {
          Future.successful(Left("Wrong username or password"))
        }
      case None => Future.successful(Left("Wrong username or password"))
    }
  }
}
