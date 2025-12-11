package de.rieckpil.blog.contextcaching;

import de.rieckpil.blog.BaseIntegrationTest;
import de.rieckpil.blog.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

// This test shares the same context as SharedContextTest2
class SharedContextTest1 extends BaseIntegrationTest {

  @Autowired private BookRepository bookRepository;

  @Test
  void shouldHaveAccessToBookRepository() {
    assertThat(bookRepository).isNotNull();
    assertThat(bookRepository.count()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldShareSameContext() {
    // This test will reuse the same Spring context as SharedContextTest2
    // because they have identical configuration
    System.out.println(
        "SharedContextTest1: Context hash = " + System.identityHashCode(bookRepository));
  }
}
