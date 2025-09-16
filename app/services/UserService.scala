package services

import models.{BorrowedBook, User, UserPatch}

import scala.concurrent.{ExecutionContext, Future}
import repo.UserRepo

import javax.inject.Inject

class UserService @Inject() (userRepo: UserRepo)(implicit ec:ExecutionContext) {
  def createUser(user: User): Future[Int] = {
    userRepo.createUser(user)
  }
  def listUser(): Future[Seq[User]] = {
    userRepo.listUsers()
  }
  def getUsername(userId: Long): Future[Option[String]] = {
    userRepo.findById(userId).map {
      case Some(user) => Some(user.name)
      case None => None
    }
  }
  def listBorrowedBooks(userId: Long): Future[Seq[BorrowedBook]] = {
    userRepo.listBorrowedBooks(userId)
  }

  def updateUser(updatedUser: UserPatch): Future[Either[String, User]] = {
    userRepo.updateUser(updatedUser)
  }
}
