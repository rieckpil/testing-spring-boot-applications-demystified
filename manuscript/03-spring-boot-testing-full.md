# Chapter 3: Testing with `@SpringBootTest`

While test slices provide focused and fast testing for individual layers, sometimes we need to test our application as a whole.

The `@SpringBootTest` annotation loads the complete application context, enabling comprehensive integration testing that closely mimics production behavior.

## Full Application Context Testing

### When to Use Full Context Tests

Full context tests serve a different purpose than sliced tests. They verify that all components work together correctly, ensuring our application functions as an integrated system.

Use `@SpringBootTest` when:
- Testing the complete request-to-response flow
- Verifying component interactions across layers
- Testing application startup and configuration
- Validating production-like scenarios
- Testing features that require the full Spring context

### Configuring the Application Context for Tests

`@SpringBootTest` offers several configuration options.

Let's explore a comprehensive example:

```java
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BookshelfApplicationIT {
    // Test implementation
}
```

This configuration showcases `@SpringBootTest`'s flexibility:

- `RANDOM_PORT` starts a real embedded servlet container (usually Tomcat) on an available port, preventing conflicts when tests run in parallel

The `webEnvironment` options include:
- `MOCK` (default): Loads a web ApplicationContext with a mock servlet environment
- `RANDOM_PORT`: Starts an embedded server on a random port
- `DEFINED_PORT`: Starts an embedded server on the port defined in properties
- `NONE`: Loads an ApplicationContext without any web environment

### Managing Context Caching for Performance

#### Understanding Context Caching Theory

Spring Test framework implements an intelligent caching mechanism for application contexts. When a test runs, Spring creates an application context and caches it for reuse.

The cache key is based on:

1. **Configuration classes**: The `@SpringBootTest` classes or `@ContextConfiguration`
2. **Active profiles**: Set via `@ActiveProfiles`
3. **Properties**: Defined in `@TestPropertySource` or test properties
4. **Context customizers**: Including `@MockBean`, `@SpyBean`, and custom test configurations
5. ... and further context customizations, see the [docs](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html).

Here's how context caching works in practice:

```java
// These tests share the same context (identical configuration)
@SpringBootTest
class BookshelfServiceIT{ }

@SpringBootTest
class BookRepositoryIT { }
```

The first two tests have identical configurations, so they share one context. The second test runs much faster because it reuses the cached context.

Here's what breaks context caching:

```java
// Different context due to different properties
@SpringBootTest(properties = "spring.jpa.show-sql=true")
class DebugBookServiceIT { }
```

Different properties mean different application behavior, forcing a new context.

Mocking also breaks caching:

```java
// Different context due to @MockBean
@SpringBootTest
class BookControllerIT {
  @MockBean
  private ExternalBookService externalService;
}
```

`@MockBean` creates a new context because Spring must replace the real bean with a mock. Understanding these rules is crucial for optimizing test suite performance.

#### Optimizing for Context Caching

1. **Create a standardized base test configuration**:

Start with a base test class that all integration tests can extend:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
```

This base class establishes common configuration for all integration tests.

Add a shared database container:

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres =
  new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");
```

The static PostgreSQL container starts once and is reused across all test classes. `@ServiceConnection` (Spring Boot 3.1+) automatically configures the datasource. This base class maximizes context caching since all subclasses share identical configuration.

Now multiple test classes can share this configuration:

```java
class BookServiceIT extends BaseIntegrationTest {
  @Autowired
  private BookService bookService;

  @Test
  void shouldCreateBook() {
    // This test reuses the cached context
  }
}
```

This test class inherits the base configuration and gets fast context startup.

```java
class BookControllerIT extends BaseIntegrationTest {
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void shouldGetBooks() {
    // This test also reuses the same cached context
  }
}
```

This second test class also inherits the configuration. Spring recognizes the identical setup and reuses the same context. The first test class creates the context (slow), but all subsequent classes reuse it (fast). This pattern can reduce test suite runtime from minutes to seconds.

2. **Understanding what breaks context caching**:

Here's an anti-pattern that breaks caching:

```java
// Different context due to @MockBean
@SpringBootTest
class DifferentContextWithMockTest {
  @MockBean
  private BookRepository bookRepository; // Creates new context!

  @Test
  void testWithMock() {
    // This test has its own context, slower startup
  }
}
```

Each `@MockBean` creates a unique context because Spring must create a custom configuration with that specific mock.

Property differences also break caching:

```java
// Different context due to properties
@SpringBootTest(properties = "spring.jpa.show-sql=true")
class DifferentContextWithPropertyTest {
  @Test
  void testWithSqlLogging() {
    // Different property = different context
  }
}
```

Different properties mean different application behavior, requiring a new context. If you have 10 test classes each with different `@MockBean` annotations, you'll have 10 different contexts, significantly slowing down your test suite.

3. **Smart mocking strategy to preserve context caching**:

Instead of using `@MockBean` which breaks caching, use `@TestConfiguration`:

```java
@SpringBootTest
public abstract class OptimizedContextCachingIT {

  @TestConfiguration
  static class MockConfiguration {
    @Bean
    @Primary
    public OpenLibraryApiClient mockOpenLibraryApiClient() {
      return mock(OpenLibraryApiClient.class);
    }
  }
}
```

This pattern provides mocks without breaking context caching. `@TestConfiguration` loads only for tests, while `@Primary` ensures the mock takes precedence over the real bean. Since this configuration is part of the base class, all subclasses share the same mock setup and cached context.

This approach:
- Maintains context caching
- Provides mock functionality
- Reduces test execution time
- Saves memory

3. **Use `@DirtiesContext` judiciously**:

```java
// Only use when absolutely necessary
@Test
@DirtiesContext // Forces context recreation after this test
void shouldModifyApplicationState() {
  // Test that modifies singleton beans or system properties
}
```

`@DirtiesContext` is the nuclear option - it marks the context as dirty, forcing Spring to discard it and create a new one for the next test.

Use this sparingly, only when a test genuinely corrupts the context (like modifying a singleton bean's state). Each use adds significant time to your test suite. Consider refactoring the test to avoid state modification instead.

4. **Monitor context creation**:

```properties
# Enable logging to see context caching
logging.level.org.springframework.test.context.cache=DEBUG
```

This debug logging is invaluable for optimizing test performance.

### Customizing the Application Context

Sometimes we need to customize the context for specific tests. Here's how to override security configuration:

```java
@SpringBootTest
@Import(TestSecurityConfiguration.class)
class SecuredBookshelfIntegrationTest {

  @TestConfiguration
  public static class TestSecurityConfiguration {
```

`@TestConfiguration` allows custom beans just for tests without affecting production code.

Replace expensive password encoding for tests:

```java
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // For testing only!
    }
}
```

Here we replace the production password encoder (likely BCrypt) with NoOpPasswordEncoder, which doesn't encrypt passwords.

This speeds up tests significantly since password hashing is computationally expensive. The `@Primary` ensures this test bean overrides the production one.

Using `@TestPropertySource` for external configuration:

```java
@SpringBootTest
@TestPropertySource(
  locations = "classpath:test-specific.properties",
  properties = {
      "spring.datasource.url=jdbc:h2:mem:testdb",
      "app.book.import.enabled=false"
  }
)
class BookshelfConfigurationIT {
```

`@TestPropertySource` provides two ways to override properties: external files and inline properties. The inline properties take precedence over the file.

Inject and verify properties:

```java
@Value("${app.book.import.enabled}")
private boolean bookImportEnabled;

@Test
void shouldDisableBookImportInTests() {
    assertThat(bookImportEnabled).isFalse();
}
```

This example switches to an in-memory H2 database for tests and disables a book import feature that might slow down tests.

The `@Value` annotation injects the property value so we can verify it's correctly set.

## Testing Different Application Layers Together

### End-to-End Flow Testing

Full context tests excel at verifying complete workflows. Here's a comprehensive integration test setup:

```java
class BookServiceIT extends BaseIntegrationTest {

  @Autowired
  private BookService bookService;

  @Autowired
  private BookRepository bookRepository;
```

We use real Spring beans for internal components to test the actual application flow.

Mock external dependencies:

```java
@MockitoBean
private OpenLibraryApiClient openLibraryApiClient;
```

We mock the external API client to avoid depending on external services that might be slow, unreliable, or have rate limits. `@MockitoBean` provides better integration with Mockito features than `@MockBean`.

Now let's test the complete workflow:

```java
@Test
@DisplayName("Should create book with metadata from external API")
void shouldCreateBookWithMetadata() {
  // Arrange
  String isbn = "9780134685991";
```

The test name clearly describes the scenario we're testing.

Create a realistic request:

```java
BookCreationRequest request = new BookCreationRequest(
  isbn, "Effective Java", "Joshua Bloch",
  LocalDate.of(2018, 1, 6)
);
```

Using a real ISBN and actual book data makes the test more realistic and can help catch edge cases with data validation.

Set up the mock to return metadata:

```java
BookMetadataResponse metadata = new BookMetadataResponse();
metadata.setCoverUrl("https://covers.openlibrary.org/...");
```

We create the expected response from the external API.

Configure the mock behavior:

```java
when(openLibraryApiClient.getBookByIsbn(isbn))
  .thenReturn(metadata);
```

This simulates what the real API would return but gives us complete control over the response. We can test different scenarios by changing the mock's response.

Execute the service method and verify the full flow:

```java
// Act
Long bookId = bookService.createBook(request);

// Assert - Check service layer
assertThat(bookId).isNotNull();
```

We verify that the service returns a valid book ID.

Verify database persistence:

```java
// Assert - Verify database persistence
Book savedBook = bookRepository.findById(bookId).orElseThrow();
assertThat(savedBook.getIsbn()).isEqualTo(isbn);
assertThat(savedBook.getThumbnailUrl())
  .isEqualTo(metadata.getCoverUrl());
```

This integration test verifies the complete flow: service receives request, calls external API (our mock), processes the response, and saves to database. The thumbnail URL assertion confirms that data from the external API was correctly integrated into our domain model.

### Controller-to-Database Integration Tests

Testing the full stack from HTTP request to database using `TestRestTemplate`. First, set up the test infrastructure:

```java
class BookControllerIntegrationIT extends BaseIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private BookRepository bookRepository;
```

`TestRestTemplate` is Spring's test-friendly HTTP client that handles authentication and follows redirects by default.

Test the complete REST API flow:

```java
@Test
@DisplayName("Should create book via REST API")
void shouldCreateBookViaApi() {
  // Arrange - JSON request body
  String requestBody = """
    {
      "isbn": "9780134685991",
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "publishedDate": "2018-01-06"
    }
    """;
```

Java text blocks (triple quotes) make JSON readable in tests. The JSON structure matches what a real client would send. We use actual book data to make the test realistic and catch any issues with date formatting or special characters.

Set up authentication headers:

```java
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.setBasicAuth("user", "password");
```

We set the Content-Type to tell the server we're sending JSON. Basic authentication is added for secured endpoints.

Combine body and headers:

```java
HttpEntity<String> request =
  new HttpEntity<>(requestBody, headers);
```

The HttpEntity combines the body and headers into a single request object. This mimics exactly what a real client would send.

Make the HTTP request and verify:

```java
// Act
ResponseEntity<Void> response = restTemplate.exchange(
  baseUrl + "/api/books",
  HttpMethod.POST,
  request,
  Void.class
);
```

We perform the actual HTTP POST request to create the book.

Verify the HTTP response:

```java
// Assert - HTTP response
assertThat(response.getStatusCode())
  .isEqualTo(HttpStatus.CREATED);
assertThat(response.getHeaders().getLocation())
  .isNotNull();
```

We check for the correct status code and Location header.

Verify database state:

```java
// Assert - Database state
assertThat(bookRepository.count()).isEqualTo(1);
Book savedBook = bookRepository.findAll().get(0);
assertThat(savedBook.getIsbn()).isEqualTo("9780134685991");
```

Finally, we verify that the book was actually saved to the database with the correct data.

### Testing with Mock External Services

#### Introduction to WireMock

WireMock is a library for stubbing and mocking HTTP services. It helps us test components that depend on external HTTP APIs without actually calling those services. This is crucial for:

1. **Test Isolation**: Tests don't depend on external service availability
2. **Predictable Responses**: Control exactly what the external service returns
3. **Error Simulation**: Test how your code handles various error scenarios
4. **Performance**: No network latency in tests

#### Adding WireMock to Your Project

Add the WireMock dependency to your test dependencies:

```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>2.35.0</version>
    <scope>test</scope>
</dependency>
```

The `test` scope ensures WireMock is only available during testing, not in production.

#### Basic WireMock Usage

WireMock can be used as a JUnit extension or started programmatically. Here's a comprehensive example testing an OpenLibrary API client:

```java
class OpenLibraryApiClientTest {
  @RegisterExtension
  static WireMockExtension wireMockServer =
    WireMockExtension.newInstance()
      .options(wireMockConfig().dynamicPort())
      .build();

  private OpenLibraryApiClient cut;
```

`@RegisterExtension` integrates WireMock with JUnit 5's lifecycle.

Configure your client to use the mock server:

```java
@BeforeEach
void setUp() {
  WebClient webClient = WebClient.builder()
    .baseUrl(wireMockServer.baseUrl())
    .build();
  cut = new OpenLibraryApiClient(webClient);
}
```

Key points:
1. `dynamicPort()` avoids port conflicts when tests run in parallel
2. Point the WebClient to WireMock's URL instead of the real API
3. Create a fresh client for each test to ensure isolation

#### Testing Successful Responses

First, set up a successful response stub:

```java
@Test
@DisplayName("Should return book metadata when API returns valid response")
void shouldReturnBookMetadataWhenApiReturnsValidResponse() {
  // Arrange
  String isbn = "9780132350884";

  wireMockServer.stubFor(
    get("/isbn/" + isbn)
      .willReturn(aResponse()
        .withHeader(HttpHeaders.CONTENT_TYPE,
          MediaType.APPLICATION_JSON_VALUE)
        .withBodyFile(isbn + "-success.json"))
  );
```

The `withBodyFile` method reads JSON from `src/test/resources/__files/`.

Create the response file (`9780132350884-success.json`):

```json
{
  "isbn_13": ["9780132350884"],
  "title": "Clean Code",
  "publishers": ["Prentice Hall"],
  "number_of_pages": 431
}
```

This simulates what the real OpenLibrary API would return.

Now execute and verify the test:

```java
// Act
BookMetadataResponse result = cut.getBookByIsbn(isbn);

// Assert
assertThat(result).isNotNull();
assertThat(result.title()).isEqualTo("Clean Code");
assertThat(result.getMainIsbn()).isEqualTo("9780132350884");
assertThat(result.getPublisher()).isEqualTo("Prentice Hall");
assertThat(result.numberOfPages()).isEqualTo(431);
```

Verify that the client correctly parses the mocked response.

#### Testing Error Scenarios

Testing error handling is equally important. Configure WireMock to return an error:

```java
@Test
@DisplayName("Should handle server error when API returns 500")
void shouldHandleServerErrorWhenApiReturns500() {
  // Arrange
  String isbn = "9999999999";

  wireMockServer.stubFor(
    get("/isbn/" + isbn)
      .willReturn(aResponse()
        .withStatus(500)
        .withHeader(HttpHeaders.CONTENT_TYPE,
          MediaType.APPLICATION_JSON_VALUE)
        .withBody("{\"error\": \"Internal Server Error\"}"))
  );
```

WireMock returns a 500 status with an error message.

Verify your client handles the error appropriately:

```java
// Act & Assert
WebClientResponseException exception = assertThrows(
  WebClientResponseException.class,
  () -> cut.getBookByIsbn(isbn)
);

assertThat(exception.getStatusCode().value()).isEqualTo(500);
```

This ensures your application gracefully handles external service failures.

#### Testing Network Issues

WireMock can simulate network problems like slow responses:

```java
@Test
@DisplayName("Should handle slow response from API")
void shouldHandleSlowResponseFromApi() {
  String isbn = "9780132350884";

  wireMockServer.stubFor(
    get("/isbn/" + isbn)
      .willReturn(aResponse()
        .withHeader(HttpHeaders.CONTENT_TYPE,
          MediaType.APPLICATION_JSON_VALUE)
        .withBodyFile(isbn + "-success.json")
        .withFixedDelay(100))  // 100ms delay
  );
```

`withFixedDelay` simulates network latency or slow external services.

Verify your application handles delays gracefully:

```java
// Test should still pass with delay
BookMetadataResponse result = cut.getBookByIsbn(isbn);
assertThat(result).isNotNull();
assertThat(result.getTitle()).isEqualTo("Clean Code");
```

This ensures your timeout and retry configurations work correctly.

## Best Practices for @SpringBootTest

1. **Use sparingly**: Full context tests are slower, reserve them for critical integration scenarios
2. **Prefer test slices**: When testing specific layers, use focused test slices
3. **Manage test data carefully**: Use `@Sql`, `@Transactional`, or custom cleanup strategies
4. **Mock external dependencies**: Use `@MockBean` for external services to ensure test reliability
5. **Profile your tests**: Use different profiles for different testing scenarios
6. **Monitor context caching**: Group tests with similar configurations to maximize context reuse
7. **Keep tests independent**: Each test should be able to run in isolation

By mastering `@SpringBootTest`, we can write comprehensive integration tests that give us confidence our application works correctly as a whole, while still maintaining reasonable test execution times through careful design and configuration.
