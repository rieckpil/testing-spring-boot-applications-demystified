package de.rieckpil.blog.contextcaching;

import de.rieckpil.blog.OpenLibraryApiClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;

// Base class for tests that need to mock external services
// This approach maintains context caching while allowing mocks
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class OptimizedContextCachingTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  // Use @TestConfiguration instead of @MockBean to preserve context caching
  @TestConfiguration
  static class MockConfiguration {

    @Bean
    @Primary
    public OpenLibraryApiClient mockOpenLibraryApiClient() {
      return mock(OpenLibraryApiClient.class);
    }
  }
}
