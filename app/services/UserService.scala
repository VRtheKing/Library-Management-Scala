package services

import models.{BorrowedBook, User, UserPatch}

import scala.concurrent.{ExecutionContext, Future}
import repo.UserRepo

import javax.inject.Inject

class UserService @Inject() (userRepo: UserRepo)(implicit ec:ExecutionContext) {
  def createUser(user: User): Future[Int] = {
    userRepo.createUser(user) // Creates a user
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
}
