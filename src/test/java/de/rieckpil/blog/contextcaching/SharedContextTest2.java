package de.rieckpil.blog.contextcaching;

import de.rieckpil.blog.BaseIntegrationTest;
import de.rieckpil.blog.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

// This test shares the same context as SharedContextTest1
class SharedContextTest2 extends BaseIntegrationTest {

  @Autowired private BookService bookService;

  @Test
  void shouldHaveAccessToBookService() {
    assertThat(bookService).isNotNull();
  }

  @Test
  void shouldShareSameContext() {
    // This test will reuse the same Spring context as SharedContextTest1
    // The context is cached and reused because both tests extend BaseIntegrationTest
    System.out.println(
        "SharedContextTest2: Service hash = " + System.identityHashCode(bookService));
  }
}
