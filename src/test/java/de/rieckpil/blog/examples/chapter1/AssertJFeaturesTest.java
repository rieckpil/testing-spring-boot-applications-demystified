package de.rieckpil.blog.examples.chapter1;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import de.rieckpil.blog.Book;
import de.rieckpil.blog.BookAlreadyExistsException;
import de.rieckpil.blog.BookStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssertJFeaturesTest {

  @Test
  void shouldAssertDateProperties() {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    LocalDate tomorrow = today.plusDays(1);
    LocalDate nextWeek = today.plusWeeks(1);

    assertThat(today).isBefore(tomorrow);
    assertThat(yesterday).isBefore(today);
    assertThat(nextWeek).isAfter(tomorrow);
    assertThat(nextWeek).isEqualTo(today.plusDays(7));
  }

  @Test
  void shouldAssertCollectionProperties() {
    List<Book> books =
        Arrays.asList(
            new Book("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6)),
            new Book("9781617292545", "Spring in Action", "Craig Walls", LocalDate.of(2018, 10, 1)),
            new Book(
                "9781491950357",
                "Building Microservices",
                "Sam Newman",
                LocalDate.of(2015, 2, 20)));

    assertThat(books)
        .hasSize(3)
        .extracting(Book::getTitle)
        .contains("Effective Java", "Spring in Action")
        .doesNotContain("Clean Code");

    assertThat(books)
        .extracting(Book::getAuthor)
        .containsExactlyInAnyOrder("Joshua Bloch", "Craig Walls", "Sam Newman");

    assertThat(books)
        .allMatch(book -> book.getIsbn().startsWith("978"))
        .anyMatch(book -> book.getTitle().contains("Spring"))
        .noneMatch(book -> book.getAuthor().isEmpty());
  }

  @Test
  void shouldAssertExceptionBehavior() {
    assertThatThrownBy(
            () -> {
              throw new BookAlreadyExistsException("9780134685991");
            })
        .isInstanceOf(BookAlreadyExistsException.class)
        .hasMessageContaining("9780134685991");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> validateIsbn(null))
        .withMessage("ISBN cannot be null");

    assertThatNoException()
        .isThrownBy(
            () -> {
              validateIsbn("9780134685991");
              new Book("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.now());
            });
  }

  @Test
  void shouldAssertBookPropertiesWithMultipleApproaches() {
    Book book =
        new Book("9780134685991", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
    book.setStatus(BookStatus.AVAILABLE);
    book.setThumbnailUrl("https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg");

    // Approach 1: Using extracting
    assertThat(book)
        .extracting(Book::getIsbn, Book::getTitle, Book::getAuthor, Book::getStatus)
        .containsExactly("9780134685991", "Effective Java", "Joshua Bloch", BookStatus.AVAILABLE);

    // Approach 2: Individual assertions
    assertThat(book.getIsbn()).isEqualTo("9780134685991");
    assertThat(book.getTitle()).startsWith("Effective");
    assertThat(book.getAuthor()).endsWith("Bloch");
    assertThat(book.getPublishedDate()).isBefore(LocalDate.now());
    assertThat(book.getThumbnailUrl()).contains("openlibrary.org");

    // Approach 3: Using satisfies (recommended for complex assertions)
    assertThat(book)
        .satisfies(
            b -> {
              assertThat(b.getIsbn()).isEqualTo("9780134685991");
              assertThat(b.getTitle()).startsWith("Effective").endsWith("Java");
              assertThat(b.getAuthor()).isNotBlank();
              assertThat(b.getStatus()).isEqualTo(BookStatus.AVAILABLE);
              assertThat(b.getThumbnailUrl()).isNotNull();
            });
  }

  @Test
  void shouldFilterAndAssertBooks() {
    List<Book> books =
        Arrays.asList(
            createBook("9780134685991", "Effective Java", "Joshua Bloch", 2018),
            createBook("9781617292545", "Spring in Action", "Craig Walls", 2018),
            createBook("9781491950357", "Building Microservices", "Sam Newman", 2015));

    assertThat(books)
        .filteredOn(book -> book.getPublishedDate().getYear() == 2018)
        .hasSize(2)
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Effective Java", "Spring in Action");

    assertThat(books)
        .filteredOn("author", "Joshua Bloch")
        .singleElement()
        .satisfies(
            book -> {
              assertThat(book.getTitle()).isEqualTo("Effective Java");
              assertThat(book.getIsbn()).startsWith("978");
            });
  }

  private void validateIsbn(String isbn) {
    if (isbn == null) {
      throw new IllegalArgumentException("ISBN cannot be null");
    }
  }

  private Book createBook(String isbn, String title, String author, int year) {
    return new Book(isbn, title, author, LocalDate.of(year, 1, 1));
  }
}
