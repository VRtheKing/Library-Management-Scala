import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.BookService
import repo.BookRepo

import java.time.LocalDateTime

class BookServiceSpec extends PlaySpec with MockitoSugar {

  
  val mockBookRepo = mock[BookRepo]
  val bookService = new BookService(mockBookRepo)
  
  "createBook" should {
    "successfully create a book" in {
      
      val newBook = Book(
        id = None,
        title = "Art of War",
        author = "Sun Tzu",
        isbn = "351-0-245-43525-7",
        stock = 13,
        updated_at = None,  
        fine = 1
      )

      when(mockBookRepo.createBook(any[Book])).thenReturn(Future.successful(1))
      val result = bookService.createBook(newBook)

      result.map { res =>
        res mustBe 1
        verify(mockBookRepo).createBook(any[Book])  
      }
    }
  }

  
  "listBook" should {
    "return a list of books" in {
      
      val books = Seq(
        Book(
          id = Some(1L),
          title = "Art of War",
          author = "Sun Tzu",
          isbn = "351-0-245-43525-7",
          stock = 13,
          updated_at = Some(LocalDateTime.now),
          fine = 1
        ),
        Book(
          id = Some(2L),
          title = "The Art of Programming",
          author = "Donald Knuth",
          isbn = "123-0-987-65432-1",
          stock = 5,
          updated_at = Some(LocalDateTime.now),
          fine = 2
        )
      )

      
      when(mockBookRepo.listAllBooks).thenReturn(Future.successful(books))
      val result = bookService.listBook()

      result.map { res =>
        res mustBe books
        verify(mockBookRepo).listAllBooks  
      }
    }
  }

  
  "updateBook" should {
    "successfully update a book" in {
      
      val updatedBook = BookPatch(
        id = 1L,
        title = Some("Art of War - Updated"),
        author = Some("Sun Tzu"),
        isbn = Some("351-0-245-43525-7"),
        stock = Some(14)
      )

      val updatedBookResult = Book(
        id = Some(1L),
        title = "Art of War - Updated",
        author = "Sun Tzu",
        isbn = "351-0-245-43525-7",
        stock = 14,
        updated_at = Some(LocalDateTime.now),
        fine = 1
      )

      
      when(mockBookRepo.updateBook(any[BookPatch])).thenReturn(Future.successful(Right(updatedBookResult)))
      val result = bookService.updateBook(updatedBook)
      
      result.map { res =>
        res mustBe Right(updatedBookResult)
        verify(mockBookRepo).updateBook(any[BookPatch])  
      }
    }

    "fail if the book does not exist" in {
      
      val updatedBook = BookPatch(
        id = 999L,
        title = Some("Non-Existent Book"),
        author = Some("No Author"),
        isbn = Some("000-0-000-00000-0"),
        stock = Some(0)
      )

      
      when(mockBookRepo.updateBook(any[BookPatch])).thenReturn(Future.successful(Left("Book not found")))
      val result = bookService.updateBook(updatedBook)

      result.map { res =>
        res mustBe Left("Book not found")
        verify(mockBookRepo).updateBook(any[BookPatch])  
      }
    }
  }

  
  "deleteBook" should {
    "successfully delete a book" in {
      
      val bookId = 1L
      when(mockBookRepo.deleteBook(bookId)).thenReturn(Future.successful(1))
      val result = bookService.deleteBook(bookId)
      
      result.map { res =>
        res mustBe 1
        verify(mockBookRepo).deleteBook(bookId)  
      }
    }

    "fail if the book does not exist" in {
      
      val bookId = 999L
      when(mockBookRepo.deleteBook(bookId)).thenReturn(Future.successful(0))
      val result = bookService.deleteBook(bookId)
      result.map { res =>
        res mustBe 0
        verify(mockBookRepo).deleteBook(bookId)  
      }
    }
  }
}
