package de.rieckpil.blog.examples.chapter2;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import de.rieckpil.blog.Book;
import de.rieckpil.blog.BookRepository;
import de.rieckpil.blog.BookStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Autowired private BookRepository bookRepository;

  @Nested
  @DisplayName("Native Query tests")
  class NativeQueryTests {

    @Test
    void shouldSearchBooksByTitleWithRanking() {
      // Given: Books with varying title matches
      Book book1 =
          new Book("978-1", "The Lord of the Rings", "J.R.R. Tolkien", LocalDate.of(1954, 7, 29));
      Book book2 =
          new Book(
              "978-2",
              "The Hobbit: There and Back Again",
              "J.R.R. Tolkien",
              LocalDate.of(1937, 9, 21));
      Book book3 =
          new Book("978-3", "Fellowship of the Ring", "J.R.R. Tolkien", LocalDate.of(1954, 7, 29));

      bookRepository.saveAll(List.of(book1, book2, book3));

      // When: Searching for "rings"
      List<Book> results = bookRepository.searchBooksByTitleWithRanking("rings");

      // Then: Books with "rings" in title, ranked by relevance
      assertThat(results).hasSize(2);
      assertThat(results.get(0).getTitle()).isEqualTo("The Lord of the Rings"); // Best match
      assertThat(results.get(1).getTitle()).isEqualTo("Fellowship of the Ring"); // Contains "ring"
    }
  }

  @Nested
  @DisplayName("findByIsbn tests")
  class FindByIsbnTests {

    @Test
    @DisplayName("Should find book by ISBN when it exists")
    void shouldFindBookByIsbnWhenExists() {
      String isbn = "9781234567890";
      Book book = new Book(isbn, "Test Book", "Test Author", LocalDate.now().minusYears(1));
      book.setStatus(BookStatus.AVAILABLE);
      book.setThumbnailUrl("https://example.com/cover.jpg");

      bookRepository.save(book);

      Optional<Book> result = bookRepository.findByIsbn(isbn);

      assertThat(result).isPresent();
      assertThat(result.get())
          .satisfies(
              foundBook -> {
                assertThat(foundBook.getIsbn()).isEqualTo(isbn);
                assertThat(foundBook.getTitle()).isEqualTo("Test Book");
                assertThat(foundBook.getAuthor()).isEqualTo("Test Author");
                assertThat(foundBook.getStatus()).isEqualTo(BookStatus.AVAILABLE);
                assertThat(foundBook.getThumbnailUrl()).isEqualTo("https://example.com/cover.jpg");
              });
    }

    @Test
    @DisplayName("Should return empty when book with ISBN does not exist")
    void shouldReturnEmptyWhenBookDoesNotExist() {
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      bookRepository.save(book);

      Optional<Book> result = bookRepository.findByIsbn("9780987654321");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Basic repository operations")
  class BasicOperationsTests {

    @Test
    @DisplayName("Should save and retrieve multiple books")
    void shouldSaveAndRetrieveMultipleBooks() {
      Book book1 =
          new Book("9781234567890", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
      Book book2 =
          new Book("9780134685991", "Spring in Action", "Craig Walls", LocalDate.of(2018, 10, 1));
      Book book3 =
          new Book(
              "9781491950357", "Building Microservices", "Sam Newman", LocalDate.of(2015, 2, 20));

      bookRepository.saveAll(List.of(book1, book2, book3));
      List<Book> allBooks = bookRepository.findAll();

      assertThat(allBooks).hasSize(3);
      assertThat(allBooks)
          .extracting(Book::getIsbn)
          .containsExactlyInAnyOrder("9781234567890", "9780134685991", "9781491950357");
    }

    @Test
    @DisplayName("Should update book status")
    void shouldUpdateBookStatus() {
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      book.setStatus(BookStatus.AVAILABLE);
      Book savedBook = bookRepository.save(book);

      savedBook.setStatus(BookStatus.BORROWED);
      bookRepository.save(savedBook);

      Optional<Book> updatedBook = bookRepository.findById(savedBook.getId());
      assertThat(updatedBook).isPresent();
      assertThat(updatedBook.get().getStatus()).isEqualTo(BookStatus.BORROWED);
    }

    @Test
    @DisplayName("Should ensure test isolation")
    void shouldEnsureTestIsolation() {
      assertThat(bookRepository.count()).isZero();

      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      bookRepository.save(book);

      assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete book")
    void shouldDeleteBook() {
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      Book savedBook = bookRepository.save(book);
      Long bookId = savedBook.getId();

      bookRepository.deleteById(bookId);

      assertThat(bookRepository.findById(bookId)).isEmpty();
      assertThat(bookRepository.count()).isZero();
    }
  }
}
