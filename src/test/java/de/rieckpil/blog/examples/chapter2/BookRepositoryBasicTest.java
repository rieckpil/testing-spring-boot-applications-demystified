package de.rieckpil.blog.examples.chapter2;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import de.rieckpil.blog.Book;
import de.rieckpil.blog.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestPropertySource(properties = "spring.flyway.enabled=false")
@DataJpaTest
class BookRepositoryBasicTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private BookRepository bookRepository;

  @Test
  void shouldFindBookByIsbn() {
    Book book = new Book();
    book.setTitle("Effective Java");
    book.setAuthor("Joshua Bloch");
    book.setIsbn("978-0134685991");
    book.setPublishedDate(LocalDate.of(2008, 8, 1));

    entityManager.persistAndFlush(book);

    Optional<Book> found = bookRepository.findByIsbn("978-0134685991");

    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("Effective Java");
  }

  @Test
  void shouldFindBooksByAuthor() {
    Book book1 =
        new Book("978-0132350884", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1));
    Book book2 =
        new Book("978-0137081073", "The Clean Coder", "Robert C. Martin", LocalDate.of(2008, 8, 1));
    Book book3 =
        new Book("978-0134685991", "Effective Java", "Joshua Bloch", LocalDate.of(2008, 8, 1));

    entityManager.persist(book1);
    entityManager.persist(book2);
    entityManager.persist(book3);
    entityManager.flush(); // Force SQL execution

    List<Book> martinBooks = bookRepository.findByAuthorContainingIgnoreCase("martin");

    assertThat(martinBooks).hasSize(2);
    assertThat(martinBooks)
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Clean Code", "The Clean Coder");
  }

  @Test
  void shouldSearchBooksByTitleWithRanking() {
    // This will FAIL with H2
    Book book = new Book();
    book.setTitle("Effective Java");
    book.setAuthor("Joshua Bloch");
    book.setIsbn("978-0134685991");
    book.setPublishedDate(LocalDate.of(2008, 8, 1));

    entityManager.persistAndFlush(book);

    assertThrows(
        InvalidDataAccessResourceUsageException.class,
        () -> bookRepository.searchBooksByTitleWithRanking("rings"));

    // Test never gets here - H2 doesn't support to_tsvector!
  }
}
