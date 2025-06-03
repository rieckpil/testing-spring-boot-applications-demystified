package de.rieckpil.blog.contextcaching;

import de.rieckpil.blog.BookRepository;
import de.rieckpil.blog.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// This test creates a DIFFERENT context due to @MockBean
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class DifferentContextWithMockTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @MockBean private BookRepository bookRepository; // This @MockBean creates a different context!

  @Autowired private BookService bookService;

  @Test
  void shouldHaveDifferentContextDueToMockBean() {
    // This test has its own Spring context because of @MockBean
    // It won't share the context with SharedContextTest1 and SharedContextTest2
    System.out.println(
        "DifferentContextTest: Service hash = " + System.identityHashCode(bookService));

    when(bookRepository.count()).thenReturn(42L);
    assertThat(bookRepository.count()).isEqualTo(42L);
  }

  @Test
  void shouldDemonstrateContextCachingImpact() {
    // This separate context means:
    // 1. Slower test execution (new context startup)
    // 2. More memory usage
    // 3. Cannot share test data with other test classes
    assertThat(bookService).isNotNull();
  }
}
