package de.rieckpil.blog.contextcaching;

import de.rieckpil.blog.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

// This test creates a DIFFERENT context due to different properties
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.jpa.show-sql=true" // This property creates a different context!
    )
@Testcontainers
@ActiveProfiles("test")
class DifferentContextWithPropertyTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @Autowired private BookRepository bookRepository;

  @Test
  void shouldHaveDifferentContextDueToProperty() {
    // This test has its own Spring context because of the custom property
    System.out.println(
        "DifferentPropertyTest: Repository hash = " + System.identityHashCode(bookRepository));
    assertThat(bookRepository).isNotNull();
  }

  @Test
  void demonstratePropertyImpact() {
    // The spring.jpa.show-sql=true property means:
    // - SQL statements will be logged to console
    // - Different context cache key
    // - No context sharing with other tests
    bookRepository.count(); // This will show SQL in console
  }
}
