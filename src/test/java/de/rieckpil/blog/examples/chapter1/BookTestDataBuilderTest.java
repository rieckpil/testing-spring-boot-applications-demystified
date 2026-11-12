package de.rieckpil.blog.examples.chapter1;

import de.rieckpil.blog.Book;
import de.rieckpil.blog.BookStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookTestDataBuilderTest {

  @Test
  void shouldBuildBookWithSensibleDefaults() {
    Book book = new BookTestDataBuilder().build();

    assertThat(book.getTitle()).isEqualTo("Default Title");
    assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
  }

  @Test
  void shouldOverrideOnlySpecifiedValues() {
    Book book =
        new BookTestDataBuilder().withTitle("Clean Code").withStatus(BookStatus.AVAILABLE).build();

    assertThat(book.getTitle()).isEqualTo("Clean Code");
    assertThat(book.getAuthor()).isEqualTo("Default Author");
  }
}
