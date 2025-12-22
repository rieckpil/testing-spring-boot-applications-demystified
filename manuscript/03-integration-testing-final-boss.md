# Chapter 3: Boss Fight #3 - The Integration Testing Final Boss

![Integration Testing Final Boss](resources/integration-testing-boss.png)

## The Final Chamber

You've journeyed deep into the testing maze. Behind you lie the defeated Unit Testing Guardian and the Sliced Testing Hydra. You've mastered isolation, learned to slice contexts, and collected the Lightning Shield.

But now you face the ultimate challenge: **The Integration Testing Final Boss**.

This boss represents the complete, assembled system. Unlike the previous bosses that tested components in isolation, this one demands you test everything working together:

- Controllers talking to services
- Services querying repositories
- Repositories executing SQL against real databases
- External HTTP APIs being called
- Security protecting the entire flow
- The complete request-response journey

The question the Final Boss asks is simple but profound:

> **"Does your entire application actually work as a cohesive system?"**

## Why We Need Integration Tests

You might wonder: "Haven't we tested everything already?"

- ✅ Unit tests verify business logic
- ✅ `@WebMvcTest` tests controllers
- ✅ `@DataJpaTest` tests repositories

So why do we need more?

Because **integration is where things break**. Here's what can go wrong even with perfect unit and slice tests:

**Configuration Issues:**
- Properties not loaded correctly
- Beans not wired together properly
- Database connection pool misconfigured

**Layer Integration Problems:**
- Controller expects JSON format A, service produces format B
- Service calls repository method that doesn't exist
- Transaction boundaries not set correctly

**External Dependencies:**
- External API changed its response format
- Network timeouts not handled
- Authentication with external services fails

**The Complete Journey:**
- Request passes security
- Controller deserializes JSON
- Service executes business logic
- Repository saves to database
- Response serialized correctly
- HTTP status and headers correct

Integration tests are your **final safety net** before production.

## Understanding @SpringBootTest

Meet your weapon for the final boss: `@SpringBootTest`.

Unlike test slices that load minimal contexts, `@SpringBootTest` loads **everything**:

```java
@SpringBootTest
class BookshelfApplicationIT {
  // The entire Spring application is available
}
```

When you use `@SpringBootTest`, Spring Boot:

1. Searches for `@SpringBootApplication`
2. Loads all beans (controllers, services, repositories)
3. Applies all configurations
4. Configures all auto-configurations
5. Prepares the complete application context

**The Trade-offs:**

| Aspect | @SpringBootTest | Test Slices |
|--------|----------------|-------------|
| Context Size | Full (all beans) | Minimal (relevant only) |
| Startup Time | 5-30 seconds | 1-5 seconds |
| Memory Usage | High | Low |
| Test Realism | Very High | Medium |
| Best For | Critical flows | Component testing |

The key insight: Use `@SpringBootTest` sparingly for critical integration tests. Use slices for everything else.

## Boss Strategy #1: Testing with a Running Server

`@SpringBootTest` has different modes. The most realistic mode starts an actual embedded server:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerIntegrationIT {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldCreateBookViaRestApi() {
    // Given - prepare JSON request
    String requestBody = """
      {
        "isbn": "9780134685991",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

    // When - make real HTTP request
    ResponseEntity<Void> response = restTemplate.exchange(
      "/api/books",
      HttpMethod.POST,
      request,
      Void.class
    );

    // Then - verify HTTP response
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation()).isNotNull();

    // And verify database state
    assertThat(bookRepository.count()).isEqualTo(1);
    Book savedBook = bookRepository.findAll().get(0);
    assertThat(savedBook.getIsbn()).isEqualTo("9780134685991");
    assertThat(savedBook.getTitle()).isEqualTo("Effective Java");
  }
}
```

Let's break down what's happening:

**`webEnvironment = RANDOM_PORT`**: Starts Tomcat (or Jetty/Undertow) on a random available port. This ensures tests can run in parallel without port conflicts.

**`TestRestTemplate`**: Spring Boot's test-friendly HTTP client that:
- Automatically points to the running server
- Follows redirects by default
- Supports authentication
- Simplifies request/response handling

**The Complete Flow**:
1. We make a real HTTP POST request
2. Request travels through all Spring MVC filters
3. Spring Security checks authentication (we'll add that next)
4. Controller deserializes JSON to `BookCreationRequest`
5. Validation runs on the request object
6. Service layer executes business logic
7. Repository saves to the database
8. Controller returns HTTP 201 with Location header

**Two-Level Verification**:
- First: HTTP response is correct (status, headers)
- Second: Database state is correct (book actually saved)

This is **true integration testing**. We're not mocking anything in our application—we're testing the real thing.

## Boss Strategy #2: Using Testcontainers for Real Infrastructure

Integration tests need infrastructure: databases, message queues, cache servers. We have two options:

**Option 1: Mock everything** ❌
- Tests don't reflect production behavior
- Database-specific features untested
- False confidence

**Option 2: Use Testcontainers** ✅
- Real PostgreSQL running in Docker
- Production-like behavior
- Catch database-specific issues

Here's how to use Testcontainers with `@SpringBootTest`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookshelfApplicationIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private BookRepository bookRepository;

  @BeforeEach
  void setUp() {
    bookRepository.deleteAll();
  }

  @Test
  void shouldCreateAndRetrieveBook() {
    // Given - create a book via API
    String createRequest = """
      {
        "isbn": "9780134685991",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Void> createResponse = restTemplate.exchange(
      "/api/books",
      HttpMethod.POST,
      new HttpEntity<>(createRequest, headers),
      Void.class
    );

    String location = createResponse.getHeaders()
      .getLocation()
      .toString();

    // When - retrieve the book via API
    ResponseEntity<Book> getResponse = restTemplate.getForEntity(
      location,
      Book.class
    );

    // Then - verify complete round-trip
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    assertThat(getResponse.getBody().getIsbn())
      .isEqualTo("9780134685991");
    assertThat(getResponse.getBody().getTitle())
      .isEqualTo("Effective Java");
  }
}
```

Key points about this setup:

**`static PostgreSQLContainer<?>`**: Container starts once for all tests in the class. Starting containers is expensive (2-5 seconds), so we reuse it.

**`@ServiceConnection`** (Spring Boot 3.1+): Automatically configures:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

No manual configuration needed!

**`@BeforeEach void setUp()`**: Cleans database before each test. This ensures test isolation—each test starts with a clean slate.

**The Test Flow**:
1. Create book via POST to `/api/books`
2. Extract Location header (e.g., `/api/books/1`)
3. Retrieve book via GET to the location
4. Verify all data round-tripped correctly

This tests:
- JSON serialization (Java → JSON)
- Database persistence (Java → SQL)
- Database retrieval (SQL → Java)
- JSON deserialization (JSON → Java)

## Boss Strategy #3: Mocking External HTTP Services

Real applications call external APIs. But we don't want integration tests to:
- Depend on external service uptime
- Make actual network calls (slow, unreliable)
- Risk hitting rate limits
- Need internet connectivity

Solution: **WireMock** - a library for stubbing HTTP services.

Here's our scenario: The Shelfie app calls the OpenLibrary API to fetch book metadata:

```java
@Service
public class BookService {
  private final OpenLibraryApiClient apiClient;
  private final BookRepository bookRepository;

  public Long createBook(BookCreationRequest request) {
    // Fetch metadata from external API
    BookMetadataResponse metadata =
      apiClient.getBookByIsbn(request.isbn());

    Book book = new Book();
    book.setIsbn(request.isbn());
    book.setTitle(request.title());
    book.setAuthor(request.author());
    book.setThumbnailUrl(metadata.getCoverUrl()); // From external API

    Book saved = bookRepository.save(book);
    return saved.getId();
  }
}
```

Let's test this with WireMock. Start with the test class setup:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookServiceIntegrationIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine");

  static WireMockServer wireMockServer;
}
```

We combine `@SpringBootTest` for full context with Testcontainers for PostgreSQL. The `static WireMockServer` will mock our external API.

Set up WireMock lifecycle methods:

```java
@BeforeAll
static void beforeAll() {
  wireMockServer = new WireMockServer(8089);
  wireMockServer.start();
  WireMock.configureFor("localhost", 8089);
}

@AfterAll
static void afterAll() {
  wireMockServer.stop();
}
```

`@BeforeAll` starts WireMock once before all tests. We use port 8089 to avoid conflicts. `@AfterAll` ensures clean shutdown.

Point our application to WireMock instead of the real API:

```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
  registry.add("openlibrary.api.base-url",
    () -> "http://localhost:8089");
}
```

`@DynamicPropertySource` overrides application properties at runtime. Our `OpenLibraryApiClient` now calls WireMock instead of the real OpenLibrary API.

Clean up between tests:

```java
@Autowired
private TestRestTemplate restTemplate;

@Autowired
private BookRepository bookRepository;

@BeforeEach
void setUp() {
  bookRepository.deleteAll();
  wireMockServer.resetAll();
}
```

Each test starts with an empty database and fresh WireMock stubs.

Now write the actual test. First, stub the external API response:

```java
@Test
void shouldEnrichBookWithExternalMetadata() {
  // Given - stub external API response
  String isbn = "9780132350884";

  stubFor(get(urlEqualTo("/isbn/" + isbn + ".json"))
    .willReturn(aResponse()
      .withStatus(200)
      .withHeader("Content-Type", "application/json")
      .withBody("""
        {
          "isbn_13": ["9780132350884"],
          "title": "Clean Code",
          "publishers": ["Prentice Hall"],
          "covers": [12345],
          "number_of_pages": 431
        }
        """)));
}
```

`stubFor()` configures WireMock: when a GET request to `/isbn/9780132350884.json` arrives, return this JSON with HTTP 200.

Make the HTTP request to create a book:

```java
  // When - create book via API
  String createRequest = """
    {
      "isbn": "9780132350884",
      "title": "Clean Code",
      "author": "Robert Martin",
      "publishedDate": "2008-08-01"
    }
    """;

  HttpHeaders headers = new HttpHeaders();
  headers.setContentType(MediaType.APPLICATION_JSON);

  ResponseEntity<Void> response = restTemplate.exchange(
    "/api/books",
    HttpMethod.POST,
    new HttpEntity<>(createRequest, headers),
    Void.class
  );
```

We POST book data to our application. Behind the scenes, our service calls the OpenLibrary API (actually WireMock) to fetch metadata.

Verify the HTTP response and database state:

```java
  // Then - verify book was enriched with external data
  assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

  Book savedBook = bookRepository.findAll().get(0);
  assertThat(savedBook.getIsbn()).isEqualTo("9780132350884");
  assertThat(savedBook.getThumbnailUrl())
    .contains("covers.openlibrary.org/b/id/12345");
```

The book was created (201 status) and saved with a thumbnail URL from the external API data.

Finally, verify the external API was actually called:

```java
  // Verify external API was called
  verify(getRequestedFor(urlEqualTo("/isbn/" + isbn + ".json")));
}
```

WireMock's `verify()` confirms our application made the expected HTTP call.
  }
}
```

Breaking down the WireMock setup:

**`WireMockServer wireMockServer`**: Local HTTP server for stubbing

**`@BeforeAll static void beforeAll()`**: Starts WireMock once before all tests

**`@DynamicPropertySource`**: Overrides application properties at runtime. Points our `OpenLibraryApiClient` to WireMock instead of the real API.

**`stubFor(get(...))`**: Configures WireMock to return a specific response when a request matches.

**`verify(getRequestedFor(...))`**: Confirms our application actually called the external API.

Now our test:
- ✅ Uses a real database (Testcontainers)
- ✅ Makes real HTTP calls to our application
- ✅ Mocks external dependencies predictably
- ✅ Verifies the complete integration
- ✅ Runs without internet connectivity

## Boss Strategy #4: Testing Security End-to-End

Integration tests must verify that security actually works in the complete flow. Let's test a secured endpoint:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookControllerSecurityIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void shouldRejectUnauthenticatedRequest() {
    // Given - request without credentials
    String createRequest = """
      {
        "isbn": "9780134685991",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // When - try to create book without auth
    ResponseEntity<Void> response = restTemplate.exchange(
      "/api/books",
      HttpMethod.POST,
      new HttpEntity<>(createRequest, headers),
      Void.class
    );

    // Then - should be rejected
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldAllowAuthenticatedRequest() {
    // Given - request with credentials
    String createRequest = """
      {
        "isbn": "9780134685991",
        "title": "Effective Java",
        "author": "Joshua Bloch",
        "publishedDate": "2018-01-06"
      }
      """;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBasicAuth("admin", "admin"); // Add authentication

    // When - create book with auth
    ResponseEntity<Void> response = restTemplate.exchange(
      "/api/books",
      HttpMethod.POST,
      new HttpEntity<>(createRequest, headers),
      Void.class
    );

    // Then - should succeed
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void shouldEnforceRoleBasedAccess() {
    // Given - user with USER role (not ADMIN)
    TestRestTemplate userTemplate = restTemplate
      .withBasicAuth("user", "password");

    String deleteRequest = "";
    HttpHeaders headers = new HttpHeaders();

    // When - try to delete book (requires ADMIN role)
    ResponseEntity<Void> response = userTemplate.exchange(
      "/api/books/1",
      HttpMethod.DELETE,
      new HttpEntity<>(headers),
      Void.class
    );

    // Then - should be forbidden
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
```

These tests verify security in the **real runtime environment**:
- Filters are actually configured
- Security chains are executed
- Authentication is enforced
- Role-based access control works

## Creating a Base Integration Test Class

To optimize context caching (we'll explore this deeply in the next chapter), create a base class. Start with the class declaration and annotations:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
  // Configuration follows...
}
```

`@ActiveProfiles("test")` ensures consistent profile usage across all tests. This is crucial for context caching.

Add the Testcontainers PostgreSQL setup:

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres =
  new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");
```

The `static` container starts once and is shared by all test classes that extend this base.

Inject common dependencies:

```java
@Autowired
protected TestRestTemplate restTemplate;

@Autowired
protected BookRepository bookRepository;

@BeforeEach
void cleanDatabase() {
  bookRepository.deleteAll();
}
```

Using `protected` makes these available to subclasses. `@BeforeEach` ensures each test starts with a clean database.

Now all integration tests can extend this:

```java
class BookCreationIT extends BaseIntegrationTest {

  @Test
  void shouldCreateBook() {
    // Test implementation
    // All infrastructure already set up!
  }
}

class BookSearchIT extends BaseIntegrationTest {

  @Test
  void shouldSearchBooks() {
    // Test implementation
    // Same infrastructure reused!
  }
}
```

Benefits:
- **Single context**: Both tests share the same Spring context (fast!)
- **Consistent setup**: Same database, same configuration
- **Less duplication**: Common setup in one place
- **Better caching**: Spring reuses contexts more effectively

## When to Use Integration Tests

Integration tests are expensive (slow, memory-heavy). Use them strategically:

**Write Integration Tests For:**

✅ **Critical Business Workflows**
- User registration and login
- Payment processing
- Order fulfillment
- Data export/import

✅ **Complex Multi-Layer Interactions**
- Controller → Service → Repository → Database flows
- Features involving multiple services
- Transaction management verification

✅ **External API Integration**
- Verify HTTP client configuration
- Test retry and timeout logic
- Validate response mapping

✅ **Security Requirements**
- Authentication flows
- Authorization rules
- Token validation

**Don't Write Integration Tests For:**

❌ **Simple business logic** (use unit tests)
❌ **Individual repository methods** (use `@DataJpaTest`)
❌ **Controller request mapping** (use `@WebMvcTest`)
❌ **JSON serialization** (use `@JsonTest`)

The pyramid still applies:
```
       /\
      /  \ Integration Tests (Few)
     /----\
    /      \ Slice Tests (Some)
   /--------\
  /          \ Unit Tests (Many)
 /____________\
```

## Collecting Your Second Quest Item

By mastering `@SpringBootTest` and defeating the Integration Testing Final Boss, you've unlocked:

### 🔍 Quest Item Acquired: The Scroll of Truth

**Power**: Integration Confidence

The Scroll of Truth reveals whether your application truly works as a system. It answers:

- ✅ Do all layers integrate correctly?
- ✅ Does security protect the full request flow?
- ✅ Do external APIs behave as expected?
- ✅ Does data persist and retrieve correctly?
- ✅ Are transactions managed properly?

**The Scroll's Wisdom**:
- Unit tests tell you if pieces work
- Slice tests tell you if components work
- Integration tests tell you if the **system** works

You now have confidence in your complete application.

## Boss Defeated: Integration Testing Mastered

The Final Boss lies defeated. You've conquered:

**Complete Application Testing:**
- Using `@SpringBootTest` for full context loading
- Starting real servers with `RANDOM_PORT`
- Using `TestRestTemplate` for HTTP testing

**Real Infrastructure Integration:**
- Testcontainers for production-like databases
- Complete CRUD workflows
- Database state verification

**External Service Mocking:**
- WireMock for HTTP stubbing
- Predictable external dependency behavior
- Offline test execution

**End-to-End Security:**
- Authentication enforcement
- Role-based access control
- Complete security chain verification

**Quest Items**:
- ⚡ Lightning Shield (Fast Feedback from slices)
- 🔍 Scroll of Truth (Integration Confidence)

## The Path to the Exit

You've defeated all three bosses:
- ✅ Unit Testing Guardian
- ✅ Sliced Testing Hydra
- ✅ Integration Testing Final Boss

But your journey isn't complete. You have two quest items, but there's one more to collect: **The Caching Amulet**.

The next chamber doesn't contain a boss. Instead, it holds three powerful artifacts that will transform your testing practice:

**🧿 The Caching Amulet**: Master Spring's context caching to cut test time in half (the story of 26 minutes → 12 minutes)

**⚡ The Lightning Shield (Enhanced)**: Advanced parallelization techniques

**🔍 The Scroll of Truth (Enhanced)**: Mutation testing to verify your tests actually test something

These quest items address a critical challenge: **test performance**. You know how to write tests, but can you write tests that run fast enough for daily use?

**Next up: Chapter 4 - Collecting the Quest Items: Performance & Best Practices**
