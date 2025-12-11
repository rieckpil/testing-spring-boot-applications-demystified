package de.rieckpil.blog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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

  @Autowired private BookRepository cut;

  @BeforeEach
  void setUp() {
    cut.deleteAll();
  }

  @Nested
  @DisplayName("findByIsbn tests")
  class FindByIsbnTests {

    @Test
    @DisplayName("Should find book by ISBN when it exists")
    void shouldFindBookByIsbnWhenExists() {
      // Arrange
      String isbn = "9781234567890";
      Book book = new Book(isbn, "Test Book", "Test Author", LocalDate.now().minusYears(1));
      book.setStatus(BookStatus.AVAILABLE);
      book.setThumbnailUrl("https://example.com/cover.jpg");

      cut.save(book);

      // Act
      Optional<Book> result = cut.findByIsbn(isbn);

      // Assert
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
      // Arrange
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      cut.save(book);

      // Act
      Optional<Book> result = cut.findByIsbn("9780987654321");

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Basic repository operations")
  class BasicOperationsTests {

    @Test
    @DisplayName("Should save and retrieve multiple books")
    void shouldSaveAndRetrieveMultipleBooks() {
      // Arrange
      Book book1 =
          new Book("9781234567890", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
      Book book2 =
          new Book("9780134685991", "Spring in Action", "Craig Walls", LocalDate.of(2018, 10, 1));
      Book book3 =
          new Book(
              "9781491950357", "Building Microservices", "Sam Newman", LocalDate.of(2015, 2, 20));

      // Act
      cut.saveAll(List.of(book1, book2, book3));
      List<Book> allBooks = cut.findAll();

      // Assert
      assertThat(allBooks).hasSize(3);
      assertThat(allBooks)
          .extracting(Book::getIsbn)
          .containsExactlyInAnyOrder("9781234567890", "9780134685991", "9781491950357");
    }

    @Test
    @DisplayName("Should update book status")
    void shouldUpdateBookStatus() {
      // Arrange
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      book.setStatus(BookStatus.AVAILABLE);
      Book savedBook = cut.save(book);

      // Act
      savedBook.setStatus(BookStatus.BORROWED);
      cut.save(savedBook);

      // Assert
      Optional<Book> updatedBook = cut.findById(savedBook.getId());
      assertThat(updatedBook).isPresent();
      assertThat(updatedBook.get().getStatus()).isEqualTo(BookStatus.BORROWED);
    }

    @Test
    @DisplayName("Should ensure test isolation")
    void shouldEnsureTestIsolation() {
      // Act & Assert - No books should exist at start
      assertThat(cut.count()).isZero();

      // Arrange
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      cut.save(book);

      // Assert - After adding, count should be 1
      assertThat(cut.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should delete book")
    void shouldDeleteBook() {
      // Arrange
      Book book = new Book("9781234567890", "Test Book", "Test Author", LocalDate.now());
      Book savedBook = cut.save(book);
      Long bookId = savedBook.getId();

      // Act
      cut.deleteById(bookId);

      // Assert
      assertThat(cut.findById(bookId)).isEmpty();
      assertThat(cut.count()).isZero();
    }
  }
}
