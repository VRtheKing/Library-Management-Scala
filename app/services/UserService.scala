package services

import models.User
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
}
