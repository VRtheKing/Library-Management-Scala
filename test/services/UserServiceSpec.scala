import models._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.{TokenService, UserService}
import repo.UserRepo
import security.JwtUtil
import com.github.t3hnar.bcrypt._
import org.mockito.ArgumentMatchers.any

import java.time.LocalDateTime

class UserServiceSpec extends PlaySpec with MockitoSugar {
  val mockTokenService = mock[TokenService]
  val mockUserRepo = mock[UserRepo]
  val userService = new UserService(mockTokenService, mockUserRepo)

  val testRole: Role = Role.User

  "createUser" should {
    "successfully create a user" in {
      val newUser = User(
        id = None,
        name = "John Doe",
        email = "john.doe@example.com",
        passwordHash = "password123".boundedBcrypt,
        role = testRole,  
        createdAt = Some(LocalDateTime.now())  
      )

      
      when(mockUserRepo.createUser(any[User])).thenReturn(Future.successful(1))

      
      val result = userService.createUser(newUser)

      
      result.map { res =>
        res mustBe 1
        verify(mockUserRepo).createUser(any[User])  
      }
    }
  }

  
  "listUser" should {
    "return a list of users" in {
      
      val users = Seq(
        User(
          id = Some(1L),
          name = "John Doe",
          email = "john.doe@example.com",
          passwordHash = "password123".boundedBcrypt,
          role = testRole,
          createdAt = Some(LocalDateTime.now())
        )
      )

      
      when(mockUserRepo.listUsers()).thenReturn(Future.successful(users))

      
      val result = userService.listUser()

      
      result.map { res =>
        res mustBe users
        verify(mockUserRepo).listUsers()  
      }
    }
  }

  
  "getUsername" should {
    "return the username if user exists" in {
      
      val userId = 1L
      val user = User(
        id = Some(userId),
        name = "John Doe",
        email = "john.doe@example.com",
        passwordHash = "password123".boundedBcrypt,
        role = testRole,
        createdAt = Some(LocalDateTime.now())
      )

      
      when(mockUserRepo.findById(userId)).thenReturn(Future.successful(Some(user)))

      
      val result = userService.getUsername(userId)

      
      result.map { res =>
        res mustBe Some("John Doe")
        verify(mockUserRepo).findById(userId)  
      }
    }

    "return None if user does not exist" in {
      
      val userId = 99L

      
      when(mockUserRepo.findById(userId)).thenReturn(Future.successful(None))

      
      val result = userService.getUsername(userId)

      
      result.map { res =>
        res mustBe None
        verify(mockUserRepo).findById(userId)  
      }
    }
  }

  
  "loginUser" should {
    "successfully log in with correct credentials" in {
      
      val userLogin = UserLogin("john.doe@example.com", "password123".boundedBcrypt)

      val storedUser = User(
        id = Some(1L),
        name = "John Doe",
        email = "john.doe@example.com",
        passwordHash = "password123".boundedBcrypt,
        role = testRole,
        createdAt = Some(LocalDateTime.now())
      )
      val tokenPair = TokenPair("accessToken", "refreshToken")

      
      when(mockUserRepo.validateUser(any[UserLogin])).thenReturn(Future.successful(Some(storedUser)))
      when(mockTokenService.generateRefreshToken(storedUser)).thenReturn(Future.successful("refreshToken"))

      
      val result = userService.loginUser(userLogin)

      
      result.map { res =>
        res mustBe Right(tokenPair)
        verify(mockUserRepo).validateUser(any[UserLogin])  
        verify(mockTokenService).generateRefreshToken(storedUser)  
      }
    }

    "fail if username or password is incorrect" in {
      
      val userLogin = UserLogin("john.doe@example.com", "wrongpassword".boundedBcrypt)

      
      when(mockUserRepo.validateUser(any[UserLogin])).thenReturn(Future.successful(None))

      
      val result = userService.loginUser(userLogin)

      
      result.map { res =>
        res mustBe Left("Wrong username or password")
        verify(mockUserRepo).validateUser(any[UserLogin])  
      }
    }
  }

  
  "deleteUser" should {
    "delete a user successfully" in {
      
      val userId = 1L

      
      when(mockUserRepo.deleteUser(userId)).thenReturn(Future.successful(1))

      
      val result = userService.deleteUser(userId)

      
      result.map { res =>
        res mustBe 1
        verify(mockUserRepo).deleteUser(userId)  
      }
    }
  }
}
