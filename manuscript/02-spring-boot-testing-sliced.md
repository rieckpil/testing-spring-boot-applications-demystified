# Chapter 2: Testing with a Sliced Application Context

One of Spring Boot's most powerful testing features is the ability to test "slices" of our application.

Instead of loading the entire application context, we can focus on testing specific layers in isolation, resulting in faster tests and clearer test intentions.

## Understanding Test Slices

### The Concept of Context Slicing

When we write tests for our Spring Boot applications, we often don't need the entire application context.

Testing a REST controller doesn't require JPA repositories, and testing a repository doesn't need web controllers.

Spring Boot's test slices load only the relevant parts of the application context for specific testing scenarios.

Think of test slices as focused views into our application:

```java
// Full context - loads everything
@SpringBootTest
class FullContextTest {
  // Entire application context is loaded
}

// Sliced context - loads only web layer
@WebMvcTest
class WebLayerTest {
  // Only web-related beans are loaded
}
```

### Benefits of Focused Testing

Test slices provide several advantages:

1. Faster Test Execution: Loading fewer beans means quicker context startup
2. Clearer Test Intent: The test annotation immediately communicates what's being tested
3. Reduced Memory Usage: Smaller contexts consume less memory
4. Better Isolation: Fewer components mean fewer potential side effects
5. Easier Debugging: Less noise when troubleshooting test failures

Spring Boot provides numerous test slice annotations, each targeting specific layers:

| Annotation | Purpose | Key Auto-Configurations |
|------------|---------|------------------------|
| `@WebMvcTest` | Test Spring MVC controllers | Web layer, MockMvc |
| `@WebFluxTest` | Test Spring WebFlux controllers | Reactive web layer |
| `@DataJpaTest` | Test JPA repositories | JPA, Hibernate, DataSource |
| `@DataMongoTest` | Test MongoDB repositories | MongoDB components |
| `@JsonTest` | Test JSON serialization | Jackson ObjectMapper |
| `@RestClientTest` | Test REST clients | RestTemplate, WebClient |
| `@DataRedisTest` | Test Redis operations | Redis repositories |
| `@JdbcTest` | Test JDBC operations | JdbcTemplate, DataSource |

... and much more, consult the [documentation for a complete list](https://docs.spring.io/spring-boot/appendix/test-auto-configuration/slices.html).

### When to Use Sliced Testing vs. Full Context Testing

Use sliced tests when:
- Testing a specific layer in isolation
- We need fast feedback during development
- Testing component-specific behavior
- Validating configuration for a particular layer

Use full context tests (`@SpringBootTest`) when:

- Testing integration between multiple layers
- Validating the complete request-response flow
- Testing application startup and configuration
- Verifying production-like scenarios

## Testing Web Controllers with @WebMvcTest

The `@WebMvcTest` annotation is perhaps the most commonly used test slice, allowing us to test Spring MVC controllers efficiently.

Before we dive into using `@WebMvcTest`, it's important to understand why traditional unit testing of controllers falls short and why we need the Spring testing infrastructure.

When we unit test a controller in isolation, we miss critical aspects of how Spring Boot actually processes requests:

1. Request Mapping: Unit tests don't verify that our `@RequestMapping`, `@GetMapping`, or other mapping annotations are correctly configured. A typo in the path or incorrect HTTP method won't be caught.

2. Validation: Spring's validation framework (`@Valid`, `@Validated`) requires the full web infrastructure to work. Unit tests bypass this entirely, missing validation errors that would occur in production.

3. Type Conversion: Spring automatically converts request parameters and path variables to the appropriate types. Unit tests don't exercise these conversions, potentially missing conversion failures.

4. Security: Security configurations, authentication, and authorization are handled by Spring Security filters that don't exist in unit tests. We can't verify that endpoints are properly secured.

5. Exception Handling: Global exception handlers (`@ControllerAdvice`) and error mapping won't be invoked in unit tests, leaving error handling untested.

6. Content Negotiation: Spring's content negotiation (handling different media types) requires the web infrastructure to function properly.

Here's an example of what we miss with unit testing:

```java
// This unit test passes but misses many issues
@Test
void unitTestMissesImportantBehavior() {
  BookshelfController controller = new BookshelfController(mockService);

  // This test doesn't verify:
  // - Is the endpoint actually mapped to /books?
  // - Does validation work on the parameters?
  // - Is the endpoint secured?
  // - Does the JSON serialization work correctly?
  String result = controller.listBooks(new Model());

  assertEquals("listBooks", result);
}
```

This is why Spring Boot provides `@WebMvcTest` - to test controllers with the necessary web infrastructure while still keeping tests focused and fast.

### Setting Up Controller Tests

Let's start with a typical Spring MVC controller. First, we'll define the controller class with its dependency:

```java
@Controller
public class BookshelfController {
  private final BookshelfService bookshelfService;

  public BookshelfController(BookshelfService bookshelfService) {
    this.bookshelfService = bookshelfService;
  }
}
```

This controller depends on a `BookshelfService` which will handle the business logic. We inject it through the constructor for better testability.

Next, let's add an endpoint to list all books:

```java
@GetMapping("/books")
public String listBooks(Model model) {
  model.addAttribute("books", bookshelfService.findAllBooks());
  return "listBooks";
}
```

This method handles GET requests to `/books`, adds the book list to the model, and returns the view name. Spring will resolve this to a template file.

For adding new books, we need a form display endpoint:

```java
@GetMapping("/books/add")
public String showAddBookForm() {
  return "addBook";
}
```

This simply returns the view name for the add book form.

Finally, we handle form submission with a POST endpoint:

```java
@PostMapping("/books/add")
public String addBook(
 @RequestParam String title,
 @RequestParam String author,
 @RequestParam String isbn,
 @RequestParam(required = false) String genre,
 @RequestParam(required = false) String description) {

  bookshelfService.addBook(title, author, isbn, genre, description);
  return "redirect:/books";
}
```

This method accepts form parameters, calls the service to add the book, and redirects to the book list. The `required = false` parameters are optional.

Now let's test our controller with `@WebMvcTest`.

First, we set up the test class:

```java
@WebMvcTest(BookshelfController.class)
class BookshelfControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookshelfService bookshelfService;
}
```

The `@WebMvcTest` annotation loads only the web layer components needed to test our controller. `MockMvc` allows us to perform HTTP requests without starting a real server, while `@MockBean` creates a mock of our service.

Let's test the book listing endpoint:

```java
@Test
void shouldListAllBooks() throws Exception {
  // Given - prepare test data
  List<Book> books = List.of(
    new Book("Effective Java", "Joshua Bloch", "978-0134685991"),
    new Book("Clean Code", "Robert Martin", "978-0132350884")
  );
  when(bookshelfService.findAllBooks()).thenReturn(books);
}
```

We create test data and configure our mock service to return it when `findAllBooks()` is called.

Now we perform the request and verify the response:

```java
// When & Then
mockMvc.perform(get("/books"))
    .andExpect(status().isOk())
    .andExpect(view().name("listBooks"))
    .andExpect(model().attribute("books", books));
```

This performs a GET request to `/books` and verifies: the HTTP status is 200 OK, the correct view name is returned, and the model contains our book list.

Testing the form display is simpler:

```java
@Test
void shouldShowAddBookForm() throws Exception {
  // When & Then
  mockMvc.perform(get("/books/add"))
      .andExpect(status().isOk())
      .andExpect(view().name("addBook"));
}
```

This endpoint doesn't interact with the service, so we only verify the response status and view name.

### Testing Form Submissions

Form submissions require testing POST requests with parameters. Let's start by testing a successful book addition:

```java
@Test
void shouldAddNewBook() throws Exception {
  // Given - prepare mock response
  Book newBook = new Book("Clean Code", "Robert Martin", "978-0132350884");
  newBook.setId(1L);

  when(bookshelfService.addBook(anyString(), anyString(), anyString()))
    .thenReturn(newBook);
}
```

We configure the mock to return a book when `addBook` is called with any string parameters.

Now let's perform the POST request with form parameters:

```java
  // When & Then
  mockMvc.perform(post("/books/add")
      .param("title", "Clean Code")
      .param("author", "Robert Martin")
      .param("isbn", "978-0132350884"))
    .andExpect(status().is3xxRedirection())
    .andExpect(redirectedUrl("/books"));
```

The `param()` method simulates form fields. We expect a redirect status (3xx) and verify the redirect URL.

Finally, verify the service was called with correct parameters:

```java
verify(bookshelfService).addBook("Clean Code", "Robert Martin", "978-0132350884");
```

This ensures our controller passed the correct values to the service.

We should also test validation errors:

```java
@Test
void shouldShowValidationErrors() throws Exception {
  // When & Then - empty title should trigger validation
  mockMvc.perform(post("/books/add")
      .param("title", "")
      .param("author", "Robert Martin")
      .param("isbn", "978-0132350884"))
    .andExpect(status().isOk())
    .andExpect(view().name("addBook"))
    .andExpect(model().attributeHasFieldErrors("book", "title"));
```

With an empty title, we expect the form to be redisplayed (status 200 OK) with validation errors.

Verify the service was never called:

```java
verify(bookshelfService, never()).addBook(anyString(), anyString(), anyString());
```

The `never()` verification ensures that when validation fails, the service method isn't invoked.

### Testing REST API Endpoints

REST API testing requires handling JSON content and HTTP status codes.

Let's organize our tests using nested classes:

```java
@Nested
@DisplayName("POST /api/books endpoint tests")
class CreateBookTests {
```

Nested classes help organize related tests together, making test reports more readable.

First, let's test successful book creation:

```java
@Test
@DisplayName("Should return 201 Created when valid book data is provided")
void shouldReturnCreatedWhenValidBookData() throws Exception {
```

Prepare the JSON request body:

```
String validBookJson = """
  {
      "isbn": "9781234567890",
      "title": "Test Book",
      "author": "Test Author",
      "publishedDate": "2023-01-01"
  }
  """;
```

Java's text blocks (triple quotes) make JSON more readable in tests.

Configure the mock service:

```java
when(bookService.createBook(any())).thenReturn(1L);
```

The service returns the ID of the created book.

Perform the POST request and verify the response:

```java
mockMvc.perform(post("/api/books")
    .contentType(MediaType.APPLICATION_JSON)
    .content(validBookJson))
  .andExpect(status().isCreated())
  .andExpect(header().exists("Location"))
  .andExpect(header().string("Location",
    Matchers.containsString("/api/books/1")));
```

We verify: HTTP 201 Created status, presence of Location header, and that the Location contains the new book's URL.

Now let's test validation errors with invalid data:

```java
@Test
@DisplayName("Should return 400 Bad Request when invalid book data is provided")
void shouldReturnBadRequestWhenInvalidBookData() throws Exception {
  String invalidBookJson = """
    {
        "isbn": "",
        "title": "",
        "author": "Test Author",
        "publishedDate": "2025-01-01"
    }
    """;
```

Empty ISBN and title should trigger validation errors.

Perform the request and expect a bad request response:

```java
mockMvc.perform(post("/api/books")
    .contentType(MediaType.APPLICATION_JSON)
    .content(invalidBookJson))
  .andExpect(status().isBadRequest());
```

Validation should prevent the request from reaching the service layer.

Let's test a specific validation rule - future dates:

```java
@Test
@DisplayName("Should return 400 Bad Request when publishedDate is in the future")
void shouldReturnBadRequestWhenPublishedDateInFuture() throws Exception {
  LocalDate futureDate = LocalDate.now().plusDays(1);
```

Calculate tomorrow's date dynamically to ensure the test always uses a future date.

Build the JSON with the future date:

```
String futureDateJson = String.format("""
  {
      "isbn": "9781234567890",
      "title": "Test Book",
      "author": "Test Author",
      "publishedDate": "%s"
  }
  """, futureDate);
```

`String.format()` injects the calculated date into the JSON.

Test the validation:

```java
mockMvc.perform(post("/api/books")
    .contentType(MediaType.APPLICATION_JSON)
    .content(futureDateJson))
  .andExpect(status().isBadRequest());

verify(bookService, times(0)).createBook(any());
```

Books can't have future publication dates, so this should return 400 Bad Request.

### Testing Path Variables and Request Parameters

Controllers often use path variables and query parameters. Let's test a search endpoint:

```java
@Test
void shouldSearchBooksByTitle() throws Exception {
  // Given - prepare search results
  List<Book> searchResults = List.of(
    new Book("Clean Code", "Robert Martin", "978-0132350884"),
    new Book("Clean Architecture", "Robert Martin", "978-0134494166")
  );
}
```

We create test data representing books that match our search query.

Configure the mock service:

```java
when(bookshelfService.searchBooks("clean")).thenReturn(searchResults);
```

The service will return our test books when searching for "clean".

Perform the search request with query parameter:

```java
// When & Then
mockMvc.perform(get("/books/search")
    .param("query", "clean"))
  .andExpect(status().isOk())
  .andExpect(view().name("searchResults"));
```

The `.param()` method adds query parameters to the request.

Verify the model contains both results and the query:

```java
.andExpect(model().attribute("books", searchResults))
.andExpect(model().attribute("query", "clean"));
```

The controller should pass both the search results and the original query to the view.

Testing exception handling is crucial for robust applications:

```java
@Test
void shouldHandleBookNotFound() throws Exception {
  // Given - service throws exception
  when(bookshelfService.findById(99L))
    .thenThrow(new BookNotFoundException("Book with id 99 not found"));
}
```

We configure the mock to throw a custom exception when a non-existent book is requested.

Test the error handling:

```java
// When & Then
mockMvc.perform(get("/books/99"))
  .andExpect(status().isOk())
  .andExpect(view().name("error"))
  .andExpect(model().attribute("message", "Book with id 99 not found"));
```

The controller should catch the exception and display an error page with the message. Note the status is still 200 OK because we're returning an error view, not an error status.

Test invalid path variable types:

```java
@Test
void shouldHandleInvalidBookId() throws Exception {
  // When & Then
  mockMvc.perform(get("/books/invalid"))
    .andExpect(status().isBadRequest());
}
```

"invalid" can't be converted to a Long, so Spring returns 400 Bad Request automatically.

### Testing Model Attributes and View Rendering

MVC controllers often prepare complex models for view rendering. Let's test a book detail page:

```java
@Test
void shouldDisplayBookDetails() throws Exception {
  // Given - create a detailed book object
  Book book = new Book("Clean Code", "Robert Martin", "978-0132350884");
  book.setId(1L);
  book.setDescription("A handbook of agile software craftsmanship");
}
```

We create a book with all details that the view might display.

Configure the service mock:

```java
when(bookshelfService.findById(1L)).thenReturn(book);
```

Now test the controller:

```java
// When & Then
mockMvc.perform(get("/books/1"))
  .andExpect(status().isOk())
  .andExpect(view().name("bookDetail"));
```

The path variable {id} is replaced with 1.

Verify model attributes:

```java
.andExpect(model().attribute("book", book))
.andExpect(model().attributeExists("relatedBooks"));
```

We verify both that the book is in the model and that other expected attributes exist.

For testing pagination, first create test data:

```java
@Test
void shouldDisplayPaginatedBookList() throws Exception {
  // Given - create 25 test books
  List<Book> books = IntStream.range(1, 26)
    .mapToObj(i -> new Book("Book " + i, "Author " + i, "ISBN-" + i))
    .collect(Collectors.toList());
}
```

We use Java streams to generate 25 test books efficiently.

Configure pagination mock:

```java
when(bookshelfService.findAllBooks(0, 10))
  .thenReturn(books.subList(0, 10));
```

The service returns the first 10 books for page 0.

Test with pagination parameters:

```java
// When & Then
mockMvc.perform(get("/books")
    .param("page", "0")
    .param("size", "10"))
  .andExpect(status().isOk())
  .andExpect(view().name("listBooks"));
```

Pagination parameters are passed as query parameters.

Verify pagination model attributes:

```java
.andExpect(model().attributeExists("books"))
.andExpect(model().attributeExists("currentPage"))
.andExpect(model().attributeExists("totalPages"));
```

The controller should provide pagination metadata for the view to render page navigation.

### Testing Security Configuration

When controllers are secured, we need to include security in our tests. First, set up the test with security configuration:

```java
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

}
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookService bookService;
```

The `@Import(SecurityConfig.class)` is crucial - without it, `@WebMvcTest` won't load security configurations and all requests would pass through unsecured. This ensures our tests accurately reflect production security behavior.

Organize security tests with `@Nested`:

```java
@Nested
@DisplayName("DELETE /api/books/{id} endpoint tests")
class DeleteBookTests {
```

Nested classes group related tests, making test reports more readable. This inner class will contain all DELETE endpoint tests, clearly showing which security scenarios we're covering.

Test 1: No authentication

```java
@Test
@DisplayName("Should return 401 when no authentication")
void shouldReturnUnauthorizedWhenNoAuthentication()
    throws Exception {

  mockMvc.perform(delete("/api/books/1"))
    .andExpect(status().isUnauthorized());
```

This test has no `@WithMockUser`, simulating an anonymous request. Spring Security should immediately reject it with 401 (Unauthorized) before reaching the controller. This verifies our security configuration is active.

The `@WithMockUser` annotation provides authentication for secured endpoints and is coming from the `spring-security-test` dependency:

```xml
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

Verify the service wasn't called:

```java
verify(bookService, times(0)).deleteBook(any());
```

Crucial verification - the service should never be called when authentication fails. This ensures security is enforced at the framework level, not in our business logic.

Test 2: Insufficient privileges

```java
@Test
@WithMockUser(roles = "USER")
@DisplayName("Should return 403 with insufficient privileges")
void shouldReturnForbiddenWhenInsufficientPrivileges()
    throws Exception {
```

`@WithMockUser(roles = "USER")` creates an authenticated user with the USER role. This tests authorization - the user is logged in but lacks the ADMIN role required for deletion.

User role can't delete books:

```java
mockMvc.perform(delete("/api/books/1"))
  .andExpect(status().isForbidden());

verify(bookService, times(0)).deleteBook(any());
```

403 (Forbidden) differs from 401 - the user is authenticated but not authorized. Again, the service isn't called, confirming authorization is enforced before business logic.

Test 3: Admin can delete existing book

```java
@Test
@WithMockUser(roles = "ADMIN")
@DisplayName("Should return 204 when admin deletes book")
void shouldReturnNoContentWhenAdminAndBookExists()
    throws Exception {
```

Now we test the happy path - an admin user who should have access. The ADMIN role passes security checks.

Mock successful deletion:

```java
when(bookService.deleteBook(1L)).thenReturn(true);

mockMvc.perform(delete("/api/books/1"))
  .andExpect(status().isNoContent());
```

The service returns true indicating successful deletion. 204 (No Content) is the standard HTTP status for successful DELETE operations with no response body.

Verify service was called:

```java
verify(bookService, times(1)).deleteBook(1L);
```

Unlike the previous tests, this verifies the service WAS called exactly once. Security passed, so business logic executed.

Test 4: Admin tries to delete non-existent book

```java
@Test
@WithMockUser(roles = "ADMIN")
@DisplayName("Should return 404 when book doesn't exist")
void shouldReturnNotFoundWhenBookDoesNotExist()
    throws Exception {
```

Mock deletion failure:

```java
when(bookService.deleteBook(999L)).thenReturn(false);

mockMvc.perform(delete("/api/books/999"))
  .andExpect(status().isNotFound());

verify(bookService, times(1)).deleteBook(999L);
```

This comprehensive test demonstrates several important security testing patterns:

1. Testing unauthenticated access: Verifying that endpoints are properly secured
2. Testing insufficient privileges: Ensuring role-based access control works correctly
3. Testing authorized access: Confirming that users with proper roles can access endpoints
4. Verifying service interactions: Ensuring the service is only called when authorization succeeds

## Testing Data Access with @DataJpaTest

The `@DataJpaTest` annotation focuses on JPA components, providing a streamlined way to test repositories.

### Repository Testing Strategies

When testing repositories, we focus on:
- Custom query methods
- Complex JPQL or native queries
- Repository specifications
- Transaction behavior

Let's start with a typical Spring Data JPA repository. First, the basic structure:

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
  Optional<Book> findByIsbn(String isbn);

  List<Book> findByAuthorContainingIgnoreCase(String author);
}
```

These are derived query methods. Spring Data generates the SQL based on method names. `findByIsbn` creates an exact match query, while `ContainingIgnoreCase` performs a case-insensitive partial match.

For more complex queries, we use JPQL:

```java
  @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
         "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Book> searchBooks(@Param("search") String search, Pageable pageable);
```

This custom query searches both title and author fields. The `LOWER()` function ensures case-insensitive matching. The `Page` return type enables pagination support.

For data modification, we need `@Modifying`:

```java
@Modifying
@Query("UPDATE Book b SET b.borrowCount = b.borrowCount + 1 WHERE b.id = :id")
void incrementBorrowCount(@Param("id") Long id);
```

The `@Modifying` annotation tells Spring this query changes data. Without it, Spring assumes SELECT queries and will throw an exception. This query atomically increments a counter, avoiding race conditions.

### Working with Test Databases

`@DataJpaTest` provides a focused testing environment for JPA components.

An important feature of `@DataJpaTest` is that it includes the `@Transactional` annotation. This means each test method runs within a transaction that's automatically rolled back after the test completes. This rollback behavior ensures that all tests start with a clean database state, eliminating the need to manually delete test data between tests.

Let's set up a basic test:

```java
@DataJpaTest
class BookRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private BookRepository bookRepository;
}
```

`@DataJpaTest` automatically configures an in-memory H2 database, scans for `@Entity` classes, and configures Spring Data JPA repositories. The `TestEntityManager` is a test-specific wrapper around JPA's EntityManager.

Let's test our `findByIsbn` method:

```java
@Test
void shouldFindBookByIsbn() {
  // Given - create test data
  Book book = new Book();
  book.setTitle("Effective Java");
  book.setAuthor("Joshua Bloch");
  book.setIsbn("978-0134685991");
}
```

We create a book entity with all required fields. The ISBN follows the real format for books.

Persist the test data:

```java
entityManager.persistAndFlush(book);
```

The `persistAndFlush()` method saves the entity and immediately synchronizes with the database. This ensures the data is available for queries.

Test the repository method:

```java
// When
Optional<Book> found = bookRepository.findByIsbn("978-0134685991");

// Then
assertThat(found).isPresent();
assertThat(found.get().getTitle()).isEqualTo("Effective Java");
```

We verify both that a book was found and that it's the correct book. The test is automatically rolled back after completion, keeping tests isolated.

### Using TestEntityManager

The `TestEntityManager` provides test-specific methods for managing entities. Let's test author search:

```java
@Test
void shouldFindBooksByAuthor() {
  // Given - create books by different authors
  Book book1 = new Book("Clean Code", "Robert C. Martin", "978-0132350884");
  Book book2 = new Book("The Clean Coder", "Robert C. Martin", "978-0137081073");
  Book book3 = new Book("Effective Java", "Joshua Bloch", "978-0134685991");

}
```

We create three books - two by Martin, one by Bloch.

Persist all books:

```java
entityManager.persist(book1);
entityManager.persist(book2);
entityManager.persist(book3);
entityManager.flush();
```

The `persist()` method stages entities for insertion. The `flush()` forces Hibernate to execute the SQL immediately.

Test the case-insensitive search:

```java
// When
List<Book> martinBooks = bookRepository
  .findByAuthorContainingIgnoreCase("martin");
```

Searching for "martin" (lowercase) should find "Robert C. Martin" (mixed case).

Verify the results:

```java
// Then
assertThat(martinBooks).hasSize(2);
assertThat(martinBooks).extracting(Book::getTitle)
  .containsExactlyInAnyOrder("Clean Code", "The Clean Coder");
```

`containsExactlyInAnyOrder()` is perfect here - we care about which books are returned, not their order. Database queries without ORDER BY can return results in any sequence.

### Testing Custom Repository Methods

Let's test our custom JPQL search query. First, create test data:

```java
@Test
void shouldSearchBooksAcrossTitleAndAuthor() {
  // Given - books with "Spring" in title
  entityManager.persist(new Book("Spring in Action",
    "Craig Walls", "978-1617294945"));
  entityManager.persist(new Book("Spring Boot in Action",
    "Craig Walls", "978-1617292545"));
  entityManager.persist(new Book("Effective Java",
    "Joshua Bloch", "978-0134685991"));
  entityManager.flush();
}
```

Two books have "Spring" in the title, one doesn't. This tests our search functionality.

Perform the search with pagination:

```java
// When
Page<Book> results = bookRepository.searchBooks("spring",
  PageRequest.of(0, 10));
```

The search is case-insensitive, so "spring" should match "Spring".

Verify results and pagination:

```java
// Then
assertThat(results.getTotalElements()).isEqualTo(2);
assertThat(results.getContent()).extracting(Book::getTitle)
  .containsExactlyInAnyOrder("Spring in Action",
    "Spring Boot in Action");
```

The `Page` object contains both the results and metadata like total count.

Now let's test the update query:

```java
@Test
void shouldIncrementBorrowCount() {
  // Given - book with initial borrow count
  Book book = new Book("Test Book", "Test Author", "123-456");
  book.setBorrowCount(5);
  Book saved = entityManager.persistAndFlush(book);
}
```

We start with a borrow count of 5.

Execute the update:

```java
// When
bookRepository.incrementBorrowCount(saved.getId());
entityManager.flush();
entityManager.clear(); // Clear persistence context
```

The `clear()` is crucial - it evicts all entities from Hibernate's cache.

Verify the increment:

```java
// Then
Book updated = entityManager.find(Book.class, saved.getId());
assertThat(updated.getBorrowCount()).isEqualTo(6);
```

Without `clear()`, we'd get the cached entity with the old value. Now we force a fresh database read to verify the update worked.

### Testing with Real Databases using Testcontainers

For more realistic testing, we use Testcontainers - a Java library that provides lightweight, throwaway instances of databases, message brokers, web browsers, or anything else that can run in a Docker container. It revolutionizes integration testing by allowing us to test against real instances of external dependencies.

Testcontainers offers several compelling advantages: Production Parity means we test against the same database version used in production, No Setup Required since containers start automatically and are cleaned up after tests, Isolation allowing each test to have its own container instance, and Cross-Platform compatibility working on any system that supports Docker.

To get started, we need to add the core Testcontainers dependency to our project:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

This provides the base functionality for managing Docker containers.

Next, add the database-specific module:

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

The PostgreSQL module includes pre-configured containers and connection helpers for PostgreSQL databases.

Let's see Testcontainers in action by setting up a test class:

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace =
  AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
```

`@AutoConfigureTestDatabase(replace = NONE)` tells Spring Boot not to replace our database with H2. We want to use the real PostgreSQL provided by Testcontainers. This gives us database-specific behavior that H2 might not support.

Configure PostgreSQL container with `@ServiceConnection`:

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres =
  new PostgreSQLContainer<>("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");
```

`@ServiceConnection` (Spring Boot 3.1+) automatically configures the datasource to use this container. The container is static and starts once for all tests in the class. We use Alpine Linux for a smaller image and faster startup.

Inject the repository and ensure clean state:

```java
@Autowired
private BookRepository cut;
```

The variable name `cut` (Class Under Test) clearly identifies what we're testing.

Organize tests with `@Nested`:

```java
@Nested
@DisplayName("findByIsbn tests")
class FindByIsbnTests {
```

Test 1: Find existing book

Create test data:

```java
@Test
@DisplayName("Should find book by ISBN when it exists")
void shouldFindBookByIsbnWhenExists() {
  // Arrange
  String isbn = "9781234567890";
  Book book = new Book(isbn, "Test Book",
    "Test Author", LocalDate.now());
}
```

We use descriptive test names with `@DisplayName`. The ISBN follows the real format (13 digits starting with 978 or 979). Using `LocalDate.now()` for publication date is fine in tests where the exact date doesn't matter.

Set additional properties and save:

```java
book.setStatus(BookStatus.AVAILABLE);
book.setThumbnailUrl("https://example.com/cover.jpg");
cut.save(book);
```

We set up a complete book object with all properties to ensure our repository handles full entities correctly. The save() method returns the persisted entity with generated ID, though we don't need it here.

Find and verify:

```java
// Act
Optional<Book> result = cut.findByIsbn(isbn);

// Assert
assertThat(result).isPresent();
```

Use satisfies for multiple assertions:

```java
assertThat(result.get())
  .satisfies(foundBook -> {
    assertThat(foundBook.getIsbn()).isEqualTo(isbn);
    assertThat(foundBook.getTitle()).isEqualTo("Test Book");
    assertThat(foundBook.getStatus())
      .isEqualTo(BookStatus.AVAILABLE);
  });
```

The `satisfies()` method groups related assertions on the same object. If any assertion fails, you'll see exactly which property was wrong. This is cleaner than multiple separate assertions and provides better error messages than comparing the entire object.

Test 2: Book not found

```java
@Test
@DisplayName("Should return empty when book doesn't exist")
void shouldReturnEmptyWhenBookDoesNotExist() {
  // Arrange - Save a different book
  Book book = new Book("9781234567890", "Test Book",
                      "Test Author", LocalDate.now());
  cut.save(book);
```

We save one book to ensure the repository isn't just returning empty because the table is empty. This makes the test more robust - it verifies the query correctly filters by ISBN.

Search for non-existent ISBN:

```java
// Act
Optional<Book> result = cut.findByIsbn("9780987654321");

// Assert
assertThat(result).isEmpty();
```

Test isolation verification:

```java
@Test
@DisplayName("Should ensure test isolation")
void shouldEnsureTestIsolation() {
  // No books should exist at start
  assertThat(cut.count()).isZero();

  // Add a book
  Book book = new Book("9781234567890", "Test Book",
                      "Test Author", LocalDate.now());
  cut.save(book);

  // Verify it was added
  assertThat(cut.count()).isEqualTo(1);
}
```

This meta-test verifies our test setup works correctly. It confirms that `@Transactional` cleans the database and that each test starts fresh. If this test fails, it indicates a problem with test isolation that could cause flaky tests.

Key advantages of Testcontainers:

1. Real database behavior: Tests run against actual PostgreSQL
2. @ServiceConnection: Automatic configuration in Spring Boot 3.1+
3. Test isolation: Each test starts clean
4. Production parity: Catch database-specific issues early

### Best Practices for Custom Test Slices

1. Keep slices focused: Each slice should test one architectural concern
2. Document usage: Provide clear documentation on when to use each slice
3. Minimize overlap: Avoid creating slices that duplicate existing functionality
4. Consider performance: Only include necessary auto-configurations
5. Maintain consistency: Follow Spring Boot's patterns and conventions

By mastering test slices, we can write faster, more focused tests that clearly communicate their intent while maintaining the benefits of Spring Boot's auto-configuration.
