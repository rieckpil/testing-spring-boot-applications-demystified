# Chapter 2: Boss Fight #2 - The Sliced Testing Hydra

![The Sliced Testing Hydra](resources/sliced-testing-hydra.png)

## Entering the Second Chamber

You've mastered unit testing and defeated the first boss. But as you enter the second chamber, you realize the Unit Testing Guardian taught you only half the story.

You can test business logic in isolation, but what about:
- REST controllers that handle HTTP requests?
- JPA repositories with complex queries?
- JSON serialization and deserialization?
- Spring Security protecting your endpoints?

Try to unit test these, and you'll quickly hit walls. Let me show you why.

## Why Unit Tests Fall Short for Spring Components

Let's try unit testing a Spring MVC controller:

```java
@RestController
public class BookController {
  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @PostMapping("/api/books")
  public ResponseEntity<Void> createBook(
    @Valid @RequestBody BookCreationRequest request) {

    Long bookId = bookService.createBook(request);

    return ResponseEntity
      .created(URI.create("/api/books/" + bookId))
      .build();
  }
}
```

Here's what a unit test might look like:

```java
@Test
void shouldCreateBook() {
  BookService mockService = mock(BookService.class);
  BookController controller = new BookController(mockService);

  when(mockService.createBook(any()))
    .thenReturn(1L);

  BookCreationRequest request = new BookCreationRequest(
    "9780132350884", "Clean Code", "Robert Martin",
    LocalDate.of(2008, 8, 1)
  );

  ResponseEntity<Void> response = controller.createBook(request);

  assertEquals(HttpStatus.CREATED, response.getStatusCode());
  assertEquals("/api/books/1",
    response.getHeaders().getLocation().toString());
}
```

This test passes. But it gives us **false confidence**. Here's what it doesn't verify:

**1. Request Mapping**: Is the endpoint actually mapped to `POST /api/books`? A typo in `@PostMapping` won't be caught.

**2. Validation**: The `@Valid` annotation does nothing in this test. We're bypassing Spring's validation framework entirely. Invalid data would pass this test but fail in production.

**3. JSON Deserialization**: We're passing a Java object directly. In reality, the controller receives JSON that must be deserialized. What if the field names don't match?

**4. Security**: If this endpoint requires authentication, our test won't check it. An accidentally unsecured endpoint would pass all unit tests.

**5. Exception Handling**: Global exception handlers (`@ControllerAdvice`) aren't invoked. Error handling remains completely untested.

**6. HTTP Response**: We're testing the Java return type. But does Spring actually convert this to the correct HTTP status and headers?

This is why we need something beyond unit tests. We need to test Spring components **with Spring**, but without starting the entire application.

## Meet the Hydra: Spring Boot Test Slices

The Sliced Testing Hydra is a multi-headed beast. Each head represents a Spring Boot test slice annotation:

```
@WebMvcTest     → Test web layer (controllers, filters, security)
@DataJpaTest    → Test persistence layer (repositories, JPA)
@JsonTest       → Test JSON serialization/deserialization
@RestClientTest → Test REST clients (RestTemplate, WebClient)
@WebFluxTest    → Test reactive controllers
... and more
```

Each test slice:
- Loads only the beans relevant to that layer
- Configures minimal Spring context
- Provides specialized testing utilities
- Runs much faster than full `@SpringBootTest`

Think of test slices as **focused Spring contexts**. Instead of loading your entire application (web layer + service layer + persistence layer + security + everything else), you load just the slice you need.

## Understanding How Slices Work

When you use `@SpringBootTest`, Spring Boot loads everything:

```
Full Application Context:
┌─────────────────────────────────────┐
│  Controllers                        │
│  Services                           │
│  Repositories                       │
│  Security Config                    │
│  JPA Config                         │
│  Jackson Config                     │
│  Database Connection Pool           │
│  ... everything else ...            │
└─────────────────────────────────────┘
```

This takes time. For a medium-sized application: 5-15 seconds. For larger apps: 30+ seconds.

With a test slice like `@WebMvcTest`, Spring loads only web-related components:

```
Web Layer Slice:
┌─────────────────────────────────────┐
│  Controllers (marked @RestController)│
│  Filters (implementing Filter)      │
│  Security Config                     │
│  Jackson ObjectMapper                │
│  MockMvc (test utility)              │
└─────────────────────────────────────┘

NOT loaded:
  ✗ Services
  ✗ Repositories
  ✗ Database connections
  ✗ JPA configuration
```

Startup time: 1-3 seconds. Much faster!

How does Spring Boot know what to include? Each test slice has a "shopping list" of component types to scan for.

For `@WebMvcTest`, the shopping list includes:
- `@RestController`
- `@Controller`
- `@ControllerAdvice`
- `@JsonComponent`
- `Filter`
- `WebMvcConfigurer`
- `HandlerInterceptor`

When Spring scans your packages, it only picks beans matching this list.

## Hydra Head #1: Testing Web Controllers with @WebMvcTest

Let's properly test our `BookController` using `@WebMvcTest`.

### Setting Up the Web Layer Test

```java
@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookService bookService;
}
```

Let's break this down:

**`@WebMvcTest(BookController.class)`**
- Loads only the web layer context
- Scans for `BookController` specifically
- Configures MockMvc automatically
- Does NOT load services or repositories

**`@Autowired private MockMvc mockMvc`**
- MockMvc is a test utility provided by Spring
- Simulates HTTP requests without starting a real server
- Provides a fluent API for testing

**`@MockBean private BookService bookService`**
- Creates a Mockito mock of `BookService`
- Registers it as a Spring bean
- Our controller will get this mock injected

The key difference from unit testing: **MockMvc actually uses Spring's infrastructure**. It processes requests through filters, applies security, deserializes JSON, and invokes exception handlers.

### Testing a GET Endpoint

Let's test listing books:

```java
@Test
void shouldListAllBooks() throws Exception {
  // Given - prepare test data
  List<Book> books = List.of(
    new Book("Effective Java", "Joshua Bloch", "978-0134685991"),
    new Book("Clean Code", "Robert Martin", "978-0132350884")
  );

  when(bookService.findAllBooks()).thenReturn(books);

  // When & Then - perform request and verify response
  mockMvc.perform(get("/api/books"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$", hasSize(2)))
    .andExpect(jsonPath("$[0].title", is("Effective Java")))
    .andExpect(jsonPath("$[1].isbn", is("978-0132350884")));
}
```

Step by step:

**1. Given**: We configure our mock service to return test data.

**2. When**: `mockMvc.perform(get("/api/books"))` simulates a GET request to `/api/books`. This goes through the full Spring MVC pipeline.

**3. Then**: We verify multiple aspects:
- `status().isOk()`: HTTP 200 response
- `jsonPath("$", hasSize(2))`: Array has 2 elements
- `jsonPath("$[0].title", is("Effective Java"))`: First book's title
- `jsonPath("$[1].isbn", ...)`: Second book's ISBN

JSONPath is a query language for JSON, like XPath for XML:
- `$` = root element
- `$[0]` = first array element
- `$.title` = title field

### Testing a POST Endpoint with Validation

Now let's test creating a book:

```java
@Test
void shouldCreateBookWithValidData() throws Exception {
  // Given - prepare request JSON
  String validBookJson = """
    {
      "isbn": "9780134685991",
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "publishedDate": "2018-01-06"
    }
    """;

  when(bookService.createBook(any())).thenReturn(1L);

  // When & Then
  mockMvc.perform(post("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validBookJson))
    .andExpect(status().isCreated())
    .andExpect(header().exists("Location"))
    .andExpect(header().string("Location",
      containsString("/api/books/1")));
}
```

This test verifies:
- JSON is properly deserialized to `BookCreationRequest`
- The endpoint returns HTTP 201 Created
- The Location header points to the new resource

Now test validation:

```java
@Test
void shouldRejectInvalidBookData() throws Exception {
  // Given - invalid data (empty ISBN and title)
  String invalidBookJson = """
    {
      "isbn": "",
      "title": "",
      "author": "Test Author",
      "publishedDate": "2023-01-01"
    }
    """;

  // When & Then
  mockMvc.perform(post("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .content(invalidBookJson))
    .andExpect(status().isBadRequest());

  // Verify service was never called
  verify(bookService, never()).createBook(any());
}
```

This test proves that Spring's validation framework (`@Valid`) actually works. The controller rejects invalid data before reaching our service.

### Testing with Path Variables and Query Parameters

Controllers often accept dynamic path segments:

```java
@GetMapping("/api/books/{id}")
public ResponseEntity<Book> getBook(@PathVariable Long id) {
  Book book = bookService.findById(id);
  return ResponseEntity.ok(book);
}
```

Test it like this:

```java
@Test
void shouldReturnBookById() throws Exception {
  // Given
  Book book = new Book("Clean Code", "Robert Martin",
                       "978-0132350884");
  book.setId(1L);

  when(bookService.findById(1L)).thenReturn(book);

  // When & Then
  mockMvc.perform(get("/api/books/{id}", 1L))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id", is(1)))
    .andExpect(jsonPath("$.title", is("Clean Code")));
}
```

The `{id}` placeholder is replaced with `1L` in the test.

For query parameters:

```java
@GetMapping("/api/books/search")
public List<Book> searchBooks(@RequestParam String query) {
  return bookService.searchByTitle(query);
}
```

Test with:

```java
@Test
void shouldSearchBooksByQuery() throws Exception {
  // Given
  List<Book> results = List.of(
    new Book("Clean Code", "Robert Martin", "978-0132350884"),
    new Book("Clean Architecture", "Robert Martin", "978-0134494166")
  );

  when(bookService.searchByTitle("clean")).thenReturn(results);

  // When & Then
  mockMvc.perform(get("/api/books/search")
      .param("query", "clean"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$", hasSize(2)));
}
```

The `.param("query", "clean")` adds `?query=clean` to the request.

### Testing Security with @WithMockUser

If your controller is secured:

```java
@PostMapping("/api/books")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> createBook(@Valid @RequestBody BookCreationRequest request) {
  // ...
}
```

You need to import the security configuration:

```java
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {
  // ...
}
```

Now test different security scenarios. Test #1: No authentication returns 401:

```java
@Test
void shouldReturn401WhenNotAuthenticated() throws Exception {
  mockMvc.perform(post("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}"))
    .andExpect(status().isUnauthorized());

  verify(bookService, never()).createBook(any());
}
```

Without authentication, Spring Security blocks the request with 401 Unauthorized. The service is never called.

Test #2: Wrong role returns 403:

```java
@Test
@WithMockUser(roles = "USER")
void shouldReturn403WhenNotAdmin() throws Exception {
  mockMvc.perform(post("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}"))
    .andExpect(status().isForbidden());

  verify(bookService, never()).createBook(any());
}
```

The `@WithMockUser` annotation (from `spring-security-test`) creates a mock authenticated user. A USER role exists but lacks ADMIN privileges, resulting in 403 Forbidden.

Test #3: Correct role succeeds:

```java
@Test
@WithMockUser(roles = "ADMIN")
void shouldCreateBookWhenAdmin() throws Exception {
  String validJson = """
    {
      "isbn": "9780134685991",
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "publishedDate": "2018-01-06"
    }
    """;

  when(bookService.createBook(any())).thenReturn(1L);

  mockMvc.perform(post("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .content(validJson))
    .andExpect(status().isCreated());

  verify(bookService, times(1)).createBook(any());
}
```

With ADMIN role, the request succeeds with 201 Created, and the service is called.

These tests verify our security progression:
- 401 (Unauthorized) → No authentication
- 403 (Forbidden) → Authenticated but wrong role
- 201 (Created) → Authenticated with correct role

## Hydra Head #2: Testing Repositories with @DataJpaTest

The persistence layer has different testing needs. Let's test our `BookRepository`:

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
  Optional<Book> findByIsbn(String isbn);

  List<Book> findByAuthorContainingIgnoreCase(String author);

  @Query("SELECT b FROM Book b WHERE " +
         "LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Book> searchBooks(@Param("search") String search,
                         Pageable pageable);
}
```

### Setting Up the Persistence Test

```java
@DataJpaTest
class BookRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldFindBookByIsbn() {
    // Given - create and persist test data
    Book book = new Book();
    book.setTitle("Effective Java");
    book.setAuthor("Joshua Bloch");
    book.setIsbn("978-0134685991");

    entityManager.persistAndFlush(book);

    // When - query by ISBN
    Optional<Book> found = bookRepository.findByIsbn("978-0134685991");

    // Then - verify result
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("Effective Java");
  }
}
```

What `@DataJpaTest` provides:

**Loaded:**
- JPA Entity Manager
- Spring Data repositories
- Database connection (H2 in-memory by default)
- Transaction management

**NOT loaded:**
- Controllers
- Services
- Security configuration
- Web layer components

**Special Features:**
- Each test runs in a transaction that's rolled back after completion
- Provides `TestEntityManager` for test data setup
- Configures Hibernate to show SQL (helpful for debugging)

### Using TestEntityManager

The `TestEntityManager` is a test-friendly wrapper around JPA's `EntityManager`:

```java
@Test
void shouldFindBooksByAuthor() {
  // Given - create multiple books
  Book book1 = new Book("Clean Code", "Robert C. Martin",
                        "978-0132350884");
  Book book2 = new Book("The Clean Coder", "Robert C. Martin",
                        "978-0137081073");
  Book book3 = new Book("Effective Java", "Joshua Bloch",
                        "978-0134685991");

  entityManager.persist(book1);
  entityManager.persist(book2);
  entityManager.persist(book3);
  entityManager.flush(); // Force SQL execution

  // When - search by author (case-insensitive)
  List<Book> martinBooks =
    bookRepository.findByAuthorContainingIgnoreCase("martin");

  // Then - verify only Martin's books are returned
  assertThat(martinBooks).hasSize(2);
  assertThat(martinBooks)
    .extracting(Book::getTitle)
    .containsExactlyInAnyOrder("Clean Code", "The Clean Coder");
}
```

Key methods:

**`persist(entity)`**: Stages entity for insertion

**`persistAndFlush(entity)`**: Inserts immediately

**`flush()`**: Forces Hibernate to execute all pending SQL

**`clear()`**: Clears the persistence context (removes all cached entities)

### Testing Custom Queries

For our custom search query:

```java
@Test
void shouldSearchBooksAcrossTitleAndAuthor() {
  // Given - books with "Spring" in title or author
  entityManager.persist(
    new Book("Spring in Action", "Craig Walls", "978-1617294945")
  );
  entityManager.persist(
    new Book("Spring Boot in Action", "Craig Walls", "978-1617292545")
  );
  entityManager.persist(
    new Book("Java by Comparison", "Simon Harrer", "978-1680502879")
  );
  entityManager.flush();

  // When - search for "spring"
  Page<Book> results = bookRepository.searchBooks("spring",
    PageRequest.of(0, 10));

  // Then - verify results
  assertThat(results.getTotalElements()).isEqualTo(2);
  assertThat(results.getContent())
    .extracting(Book::getTitle)
    .containsExactlyInAnyOrder(
      "Spring in Action",
      "Spring Boot in Action"
    );
}
```

This test verifies:
- The custom `@Query` works correctly
- Case-insensitive search functions
- Both title and author fields are searched
- Pagination works as expected

### Testing with Real Databases using Testcontainers

H2 is great for quick tests, but it's not PostgreSQL. For database-specific features (like JSONB columns, full-text search, or specific SQL syntax), use Testcontainers.

Set up the test class with Testcontainers:

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryPostgresTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @Autowired
  private BookRepository bookRepository;
}
```

Breaking down the annotations: `@Testcontainers` activates the Testcontainers extension. `@AutoConfigureTestDatabase(replace = NONE)` tells Spring NOT to replace our database with H2. `@Container` manages the container lifecycle, and `@ServiceConnection` (Spring Boot 3.1+) automatically configures the DataSource.

Now write a test using the real PostgreSQL database:

```java
@Test
void shouldFindBookByIsbn() {
  // Given
  Book book = new Book("9780134685991", "Effective Java",
                       "Joshua Bloch", LocalDate.of(2018, 1, 6));
  book.setStatus(BookStatus.AVAILABLE);

  bookRepository.save(book);

  // When
  Optional<Book> found = bookRepository.findByIsbn("9780134685991");

  // Then
  assertThat(found).isPresent();
  assertThat(found.get())
    .satisfies(b -> {
      assertThat(b.getIsbn()).isEqualTo("9780134685991");
      assertThat(b.getTitle()).isEqualTo("Effective Java");
      assertThat(b.getStatus()).isEqualTo(BookStatus.AVAILABLE);
    });
}
```

This test runs against real PostgreSQL, giving us confidence that our queries work in production.

Breaking down the annotations:

**`@Testcontainers`**: Activates Testcontainers extension

**`@AutoConfigureTestDatabase(replace = NONE)`**: Tells Spring NOT to replace our database with H2

**`@Container`**: Manages container lifecycle

**`@ServiceConnection`**: (Spring Boot 3.1+) Automatically configures the DataSource to use this container

**`static PostgreSQLContainer<?>`**: Container starts once for all tests in the class

This gives us:
- Real PostgreSQL behavior
- Database-specific features
- Confidence that queries work in production
- Automatic cleanup after tests

## Collecting Your First Quest Item

You've learned to slice your tests, defeating two heads of the Hydra (web and persistence). This achievement unlocks your first quest item:

### ⚡ Quest Item Acquired: The Lightning Shield

**Power**: Fast Feedback Loops

By using test slices instead of full `@SpringBootTest`, you've dramatically reduced test execution time:

```
Full Context Test:    15 seconds to start
Web Slice Test:        3 seconds to start
Persistence Test:      2 seconds to start

Speed improvement: 5-7x faster!
```

The Lightning Shield grants you:
- Faster development iterations
- More frequent test runs
- Reduced context switching
- Earlier bug detection

## Hydra Weakness: When NOT to Use Slices

Test slices have limitations:

**Don't use test slices when:**
1. Testing interactions between layers (controller → service → repository)
2. Verifying complete request-to-response flows
3. Testing application startup and configuration
4. Validating security across multiple layers

**Use full integration tests (`@SpringBootTest`) when:**
- Testing critical business workflows end-to-end
- Verifying external API integrations
- Testing features that span multiple layers
- Validating production-like scenarios

Think of it this way:
- **Test slices**: Test components in isolation with Spring support
- **Integration tests**: Test the complete assembled system

## Hydra Strategy Guide: Choosing the Right Slice

Here's your decision matrix:

```
Testing a Controller?
  ↓
  Use @WebMvcTest
  - Loads web layer only
  - Provides MockMvc
  - Fast startup
  - Mock the service layer

Testing a Repository?
  ↓
  Use @DataJpaTest
  - Loads JPA components only
  - Provides TestEntityManager
  - In-memory database (or Testcontainers)
  - Automatic transaction rollback

Testing JSON Serialization?
  ↓
  Use @JsonTest
  - Loads Jackson configuration only
  - Provides JacksonTester utility
  - No database, no web layer

Testing a REST Client?
  ↓
  Use @RestClientTest
  - Loads HTTP client configuration
  - Provides MockRestServiceServer
  - Stub external HTTP calls
```

## Boss Defeated: Sliced Testing Mastered

The Sliced Testing Hydra lies defeated. You've mastered:

**Head #1: @WebMvcTest**
- Testing controllers with MockMvc
- Verifying HTTP request/response handling
- Testing validation and error handling
- Security testing with @WithMockUser

**Head #2: @DataJpaTest**
- Testing repositories with real databases
- Using TestEntityManager for test data
- Testing custom queries and JPQL
- Integration with Testcontainers

**Quest Item**: ⚡ Lightning Shield (Fast Feedback)

**Skills Unlocked:**
- Understanding test slice architecture
- Choosing the right slice for each scenario
- Mocking at layer boundaries
- Writing focused, fast integration tests

## The Path Forward

You've defeated two bosses and collected one quest item. But the deepest chamber of the maze remains.

The third boss is the most challenging: **The Integration Testing Final Boss**.

This boss guards the ultimate testing challenge: testing your entire Spring Boot application as a cohesive system. You'll face:

- Full application context startup
- Real database integration with Testcontainers
- External HTTP service mocking with WireMock
- End-to-end security testing
- Complex multi-layer workflows

But you're ready. You have:
- Unit testing fundamentals
- Test slicing expertise
- The Lightning Shield for fast feedback

The final boss awaits.

**Next up: Chapter 3 - Boss Fight #3: The Integration Testing Final Boss**
