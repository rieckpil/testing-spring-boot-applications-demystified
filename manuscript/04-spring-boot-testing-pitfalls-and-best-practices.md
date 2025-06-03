# Chapter 4: Testing Pitfalls and Best Practices

Throughout our testing journey, we've explored the powerful tools and techniques Spring Boot offers.

However, even with the best tools, it's easy to fall into common traps that undermine test effectiveness.

In this chapter, we'll identify these pitfalls and establish best practices that lead to maintainable, reliable test suites.

## Common Testing Anti-Patterns

### Recognizing and Avoiding Testing Anti-Patterns

Testing anti-patterns are practices that seem reasonable at first but ultimately harm test quality and maintainability. Let's examine the most common ones and learn how to avoid them.

### Over-Mocking: The Isolation Trap

**The Problem**: Excessive mocking creates tests that pass but don't reflect real system behavior. When we mock everything, we're essentially testing our mocks, not our code.

#### Why Over-Mocking Hurts

1. **False Confidence**: Tests pass even when integration would fail
2. **Brittle Tests**: Any refactoring breaks tests, even if behavior is unchanged
3. **Maintenance Burden**: Updating mocks becomes more work than the actual code
4. **Lost Integration Issues**: Problems between components go undetected
5. **Unclear Intent**: It's hard to understand what the test actually verifies

```java
// Anti-pattern: Over-mocked test
@ExtendWith(MockitoExtension.class)
class BookshelfServiceOverMockedTest {
  @Mock private BookRepository bookRepository;
  @Mock private UserRepository userRepository;
  @Mock private BorrowingValidator validator;
  @Mock private NotificationService notificationService;
  // ... 4 more mocks!
```

This test class demonstrates a common mistake: creating too many mock objects. The `@Mock` annotation tells Mockito to create fake versions of these dependencies.

When we have this many mocks, it's a sign that our test is trying to control too much.

Each mock requires setup and verification, making the test complex and fragile.

The test setup becomes overwhelming:

```java
@Test
void shouldBorrowBook() {
  // Setup 10+ when() statements
  when(validator.canBorrow(any(), any())).thenReturn(true);
  when(bookRepository.findById(any()))
    .thenReturn(Optional.of(new Book()));
  // ... more stubbing
```

Here we see the problem escalate.

The `when()` method tells our mocks what to return when specific methods are called. The `any()` matcher means "accept any argument."

With many mocks, we need many of these setup statements. This setup code often becomes longer than the actual test, obscuring what we're trying to verify.

The actual test gets lost in the noise:

```java
BorrowResult result = bookshelfService.borrowBook(1L, 1L);

// Verify mocks were called - but did it actually work?
verify(validator).canBorrow(any(), any());
```

The actual business logic we're testing is just one line! The `verify()` method checks that our mock was called, but this doesn't tell us if the book borrowing actually worked correctly.

We're testing that methods were called, not that the system behaves correctly. This is like checking that a chef used a knife, but not tasting if the food is good.

**The Solution**: Use integration tests for complex workflows and mock only external boundaries.

#### Guidelines for Effective Mocking

1. **Mock External Dependencies Only**: Databases, web services, email servers
2. **Use Real Components When Possible**: Let Spring wire real beans
3. **Prefer Test Slices**: Use `@DataJpaTest`, `@WebMvcTest` for focused testing
4. **Mock at System Boundaries**: Not between your own components

```java
// Better: Integration test with selective mocking
@SpringBootTest
@Transactional
class BookshelfServiceIT {
  @Autowired private BookshelfService bookshelfService;
  @Autowired private BookRepository bookRepository;

  @MockBean private EmailService emailService; // Only mock external
```

This improved approach uses `@SpringBootTest` to load the full application context with real components. The `@Transactional` annotation ensures each test runs in a transaction that gets rolled back, keeping tests isolated.

We use `@Autowired` to inject real Spring beans, not mocks. The only mock is `@MockBean` for the email service - an external system we don't want to actually call during tests.

### Testing Implementation Details

**The Problem**: Tests that verify internal implementation make refactoring difficult and don't ensure correct behavior.

#### Signs You're Testing Implementation

1. **Using Reflection**: Accessing private fields or methods
2. **Verifying Method Calls**: Testing HOW instead of WHAT
3. **Spy Objects**: Spying on the class under test
4. **Exact Call Counts**: Counting internal method invocations
5. **White-Box Testing**: Tests that know too much about internals

```java
// Anti-pattern: Testing HOW it works, not WHAT it does
@Test
void shouldProcessBookReturnUsingSpecificMethods() {
  BookshelfService service = spy(bookshelfService);

  service.returnBook(1L);
```

This anti-pattern uses Mockito's `spy()` to create a partial mock of our service. A spy is a real object that we can track method calls on.

This is problematic because we're now testing the internal implementation rather than the external behavior. If we refactor the service to process returns differently, this test will break even if the functionality remains correct.

Verifying internal method calls:

```java
// Bad: Testing implementation details
verify(service).validateBookReturn(any());
verify(service).calculateLateFees(any());
```

These `verify()` calls check that specific internal methods were called. This couples our test to the implementation. If we later combine these methods or rename them, the test fails even though the book return still works correctly.

Tests should care about outcomes, not the steps taken to achieve them.

Using reflection to access private fields:

```java
// Worse: Breaking encapsulation
Field cache = BookshelfService.class
  .getDeclaredField("recentReturns");
cache.setAccessible(true);
// Don't do this!
```

This is the worst anti-pattern: using Java reflection to access private fields. The `getDeclaredField()` method retrieves a private field, and `setAccessible(true)` bypasses Java's access control.

This completely breaks encapsulation - one of the fundamental principles of object-oriented programming. Private fields are private for a reason!

**The Solution**: Test behavior and outcomes, not implementation.

#### How to Test Behavior Correctly

1. **Focus on Public API**: Only test through public methods
2. **Verify Outcomes**: Check return values and side effects
3. **Test State Changes**: Verify database or external changes
4. **Ignore Implementation**: Don't care HOW it's done
5. **Black-Box Testing**: Treat the component as a black box

```java
// Better: Testing WHAT it does, not HOW
@Test
void shouldCalculateLateFeeForOverdueBook() {
  // Given - Setup scenario
  Book book = createOverdueBook(10); // 10 days overdue
```

This better approach focuses on behavior. We set up a test scenario using a helper method `createOverdueBook()` that creates a book that was due 10 days ago.

We don't care how the service calculates late fees internally - we only care that it produces the correct result.

Call the public API:

```java
// When - Call public API
ReturnResult result = bookshelfService.returnBook(book.getId());
```

We interact with the service through its public API only. The `returnBook()` method is what external code would call, so that's what we test.

The method returns a result object that contains all the information about the return operation.

Verify the outcomes:

```java
// Then - Verify outcomes
assertThat(result.isOverdue()).isTrue();
assertThat(result.getLateFee()).isEqualTo("5.00");
```

We verify the behavior by checking the returned result. The assertions test what actually matters: Was the book marked as overdue?

Was the late fee calculated correctly? We don't care if the service used one method or ten methods internally - we only care about the correct outcome.

Check side effects in the database:

```java
// Verify side effects
Book returned = bookRepository.findById(book.getId())
  .orElseThrow();
assertThat(returned.isBorrowed()).isFalse();
```

Finally, we verify that the database was updated correctly. We fetch the book again and check that its borrowed status is now false.

This tests the complete behavior: the book is no longer marked as borrowed in our persistent storage. This is what matters to the business logic.

### The Fragile Test

**The Problem**: Tests that break with minor, unrelated changes indicate tight coupling to implementation.

#### What Makes Tests Fragile

1. **Exact String Matching**: Testing precise formatting
2. **Order Dependencies**: Assuming specific ordering
3. **Timing Assumptions**: Hard-coded delays or timeouts
4. **Environmental Dependencies**: Tests that only work on specific OS/locale
5. **Shared State**: Tests that depend on other tests

```java
// Anti-pattern: Breaks if formatting changes slightly
@Test
void shouldFormatBookDetailsInSpecificFormat() {
  Book book = new Book("Clean Code", "Robert Martin",
                       "978-0132350884");
```

This test creates a book object with specific details. The problem we're about to see is that the test will be too strict about how these details should be formatted, making it fragile and prone to breaking when non-functional changes occur.

Get the formatted result:

```java
String result = bookshelfService.formatBookDetails(book);
```

The service returns a formatted string representation of the book. How this string is formatted is an implementation detail that might change for aesthetic reasons without affecting functionality.

Exact string matching is fragile:

```java
// Bad: Will break if spacing or order changes
assertThat(result).isEqualTo(
  "Title: Clean Code\n" +
  "Author: Robert Martin\n" +
  "ISBN: 978-0132350884"
);
```

This assertion expects an exact string match, including specific spacing, line breaks (\n), and field order. If someone adds an extra space, changes the order of fields, or adds a colon, this test breaks even though the information is still correct. This makes refactoring painful.

**The Solution**: Test essential characteristics, not exact formatting.

#### Making Tests Resilient

1. **Test Content, Not Format**: Verify information is present
2. **Use Flexible Matchers**: Contains, matches patterns
3. **Parameterize Tests**: Make tests data-driven
4. **Abstract Assertions**: Create domain-specific assertions
5. **Test Contracts**: Focus on what must be true

```java
// Better: Tests what matters, ignores formatting
@Test
void shouldIncludeRequiredBookInformation() {
  Book book = new Book("Clean Code", "Robert Martin",
                       "978-0132350884");

  String result = bookshelfService.formatBookDetails(book);
```

This improved test has the same setup but will use more flexible assertions. We still want to verify that all the important information is present, but we won't be strict about the exact formatting.

Verify content without caring about format:

```java
// Good: Flexible assertions
assertThat(result)
  .containsIgnoringCase("clean code")
  .contains("Robert Martin")
  .contains(book.getIsbn());
}
```

These assertions check that the required information is present without caring about formatting.

The `containsIgnoringCase()` method doesn't care about capitalization, and `contains()` just verifies the text appears somewhere in the result.

This test will survive formatting changes while still ensuring correctness.

### The Slow Test Suite

**The Problem**: Slow tests discourage frequent execution and delay feedback.

#### Common Causes of Slow Tests

1. **Large Data Sets**: Testing with thousands of records
2. **Real Time Delays**: Using `Thread.sleep()` or real timeouts
3. **Full Context Loading**: Using `@SpringBootTest` unnecessarily
4. **External Services**: Calling real APIs or databases
5. **Inefficient Queries**: N+1 problems in tests

```java
// Anti-pattern: Multiple problems making test slow
@SpringBootTest  // Loads entire context
class SlowBookImportTest {
  @Test
  void shouldImportLargeCatalog() {
```

This test class demonstrates several performance anti-patterns. First, `@SpringBootTest` loads the entire application context, including all beans, configurations, and dependencies.

This is often unnecessary and adds seconds to test startup time.

Using excessive test data:

```java
// Bad: Too much data
List<Book> books = generateBooks(10000);
```

Generating 10,000 books is excessive for most tests. If you're testing that bulk import works, you might only need 10-50 books to prove the concept.

Large datasets slow down test execution and often don't provide additional value.

Adding real time delays:

```java
// Bad: Real delay
Thread.sleep(5000);

assertThat(bookRepository.count()).isEqualTo(10000);
```

`Thread.sleep()` is one of the worst test anti-patterns. It makes the test wait for 5 real seconds, regardless of whether the operation completes faster.

Over many tests, these delays add up to minutes or hours of wasted time. There are better ways to handle asynchronous operations.

**The Solution**: Use appropriate test sizes and avoid real delays.

#### Strategies for Faster Tests

1. **Representative Data**: Use minimum data that proves the point
2. **Test Slices**: Use `@DataJpaTest` instead of `@SpringBootTest`
3. **Async Utilities**: Replace `sleep()` with Awaitility
4. **Mock Time**: Use Clock abstraction for time-based logic
5. **Parallel Execution**: Run independent tests concurrently

```java
// Better: Fast, focused test
@DataJpaTest  // Only loads JPA components
class FastBookImportTest {
  @Test
  void shouldImportMultipleGenres() {
```

This improved version uses `@DataJpaTest`, which only loads JPA-related components (repositories, EntityManager, DataSource). This is much faster than loading the entire application context.

The test name also indicates we're testing behavior (multiple genres) rather than volume.

Use minimal representative data:

```java
// Small dataset that tests the behavior
bookRepository.saveAll(List.of(
  new Book("Clean Code", "Technology"),
  new Book("1984", "Fiction")
));
```

We only need two books to test that different genres are handled correctly. The `saveAll()` method efficiently saves multiple entities in one operation.

This minimal dataset proves our functionality works without the overhead of thousands of records.

Verify immediately without delays:

```java
// Verify without delays
assertThat(bookRepository.countByGenre("Technology"))
  .isEqualTo(1);
assertThat(bookRepository.countByGenre("Fiction"))
  .isEqualTo(1);
```

We verify the results immediately after saving. Since we're using a real database transaction (thanks to `@DataJpaTest`), the data is available instantly. No delays needed!

The custom repository method `countByGenre()` efficiently counts books without loading all entities.

## Test Performance Optimization

### Context Caching Strategies

#### Understanding Context Cache Impact

Spring's context caching can reduce test execution time by 50-90%. When Spring loads an application context for a test, it caches it and reuses it for other tests with identical configuration. This saves the expensive initialization process.

#### What Breaks Context Caching

1. **Different @MockBean/@SpyBean**: Each unique mock creates a new context
2. **Different Properties**: Any property difference forces new context
3. **Different Profiles**: Active profiles are part of the cache key
4. **@DirtiesContext**: Explicitly marks context as dirty
5. **Different Configuration Classes**: Additional @Import or @TestConfiguration

```java
// Good: Share base configuration for context reuse
@SpringBootTest
@ActiveProfiles("test")
abstract class BaseIntegrationTest {
  // Shared context configuration
}
```

This abstract base class defines a common configuration that all integration tests can inherit. By using the same annotations (`@SpringBootTest` and `@ActiveProfiles`), Spring recognizes these tests share the same context requirements and reuses the cached context. This is a crucial performance optimization.

Tests that extend the base share the same context:

```java
// These tests share the same context
class BookServiceIT extends BaseIntegrationTest { }
class UserServiceIT extends BaseIntegrationTest { }
```

Both test classes inherit the same configuration from `BaseIntegrationTest`. Spring loads the context once for the first test and reuses it for the second.

Without this pattern, each test class might create its own context, doubling the startup time.

#### Avoiding Context Pollution

Context pollution occurs when tests modify shared state, forcing Spring to create new contexts. This dramatically slows down test execution.

**Common Pollution Sources:**
- Modifying singleton beans
- Changing system properties
- Altering database schemas
- Clearing caches

```java
// Bad: Forces context recreation
@Test
@DirtiesContext // Expensive!
void shouldModifyCache() {
  cacheManager.getCache("books").clear();
}
```

The `@DirtiesContext` annotation tells Spring that this test modifies the application context in a way that could affect other tests.

Spring must discard the context and create a new one for subsequent tests. This is very expensive - often adding 5-30 seconds per occurrence!

Better approach using mocks:

```java
// Better: Mock the cache
@MockBean
private CacheManager cacheManager;

@BeforeEach
void setUp() {
  // Each test gets fresh cache
  when(cacheManager.getCache(any()))
    .thenReturn(new ConcurrentMapCache("test"));
}
```

Instead of modifying the real cache and dirtying the context, we mock the CacheManager. Each test gets a fresh, isolated cache instance. The `@BeforeEach` method runs before each test, ensuring clean state without the expensive context recreation. This approach is hundreds of times faster.

### Reducing Test Startup Time

#### Techniques for Faster Startup

1. **Lazy Initialization**: Beans created only when needed
2. **Disable Auto-Configuration**: Turn off unused starters
3. **Specific Component Scanning**: Limit package scanning
4. **Skip Database Migration**: Use pre-created schemas
5. **Conditional Beans**: Use `@ConditionalOnProperty`

```java
// Slow: Loads everything
@SpringBootTest
class SlowTest { }
```

Without any configuration, `@SpringBootTest` loads your entire application: all beans, all auto-configurations, all components. For a large application, this can take 10-30 seconds. Most tests don't need everything.

Faster alternative with specific classes:

```java
// Fast: Only what's needed
@SpringBootTest(classes = {
  BookshelfService.class,
  BookRepository.class
})
class FastTest { }
```

By specifying exactly which classes to load, Spring creates a minimal context with just these components and their dependencies.
### Parallel Test Execution

#### Two Modes of Test Parallelization

There are two distinct approaches to parallelize test execution, each with different trade-offs:

1. **Build Tool Level**: Forks multiple JVM processes
2. **Test Runner Level**: Uses multiple threads within a single JVM

Understanding both modes helps us choose the right strategy for our specific needs.

#### Build Tool Level Parallelization (JVM Forking)

Build tools like Maven and Gradle can spawn multiple JVM processes to run tests in parallel. Each fork is a completely isolated Java process with its own memory space.

**Maven Surefire Configuration:**

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <forkCount>4</forkCount>
    <reuseForks>true</reuseForks>
    <argLine>-Xmx1024m</argLine>
  </configuration>
</plugin>
```

This configuration creates 4 separate JVM processes. The `reuseForks=true` setting reuses processes between test classes for better performance. Each fork gets 1GB of heap memory.

**Maven Failsafe Configuration (Integration Tests):**

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <configuration>
    <forkCount>2</forkCount>
    <reuseForks>false</reuseForks>
  </configuration>
</plugin>
```

For integration tests, we often disable fork reuse (`reuseForks=false`) to ensure complete isolation.

**Gradle Configuration:**

```groovy
test {
  maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
  forkEvery = 50  // Create new fork every 50 tests

  jvmArgs = ['-Xmx1024m', '-XX:+UseG1GC']
}
```

Gradle dynamically calculates the number of forks based on available CPU cores. The `forkEvery` setting creates fresh JVMs periodically to prevent memory leaks from accumulating.

**Advantages of JVM Forking:**
- Complete isolation between test groups
- No shared memory concerns
- Crashes in one fork don't affect others
- Can use different JVM settings per fork

**Disadvantages:**
- High memory overhead (each JVM needs its own heap)
- Slower startup time for each fork
- Context must be loaded in each fork

#### Test Runner Level Parallelization (Thread-Based)

Test runners like JUnit 5 can execute tests using multiple threads within the same JVM. This approach shares memory and loaded contexts between threads.

**JUnit 5 Parallel Configuration:**

Create a `junit-platform.properties` file in `src/test/resources`:

```properties
# Enable parallel execution
junit.jupiter.execution.parallel.enabled=true

# Execution mode (concurrent or same_thread)
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent

# Parallelism configuration
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=1.0
```

The `dynamic` strategy calculates thread count as: `availableProcessors * factor`. A factor of 1.0 means one thread per CPU core.

**Fixed Thread Pool Configuration:**

```properties
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

This creates exactly 4 threads regardless of available CPU cores. Useful for consistent behavior across different environments.

**Controlling Parallel Execution in Code:**

```java
@Execution(ExecutionMode.CONCURRENT)
class ParallelTest {
  // This class runs tests in parallel
}

@Execution(ExecutionMode.SAME_THREAD)
class SequentialTest {
  // This class runs tests sequentially
}
```

Use `@Execution` to override the default behavior for specific test classes. This is essential for tests that can't run in parallel due to shared resources.

**Resource Locks for Shared Resources:**

```java
class DatabaseTest {
  @Test
  @ResourceLock(value = "database", mode = ResourceAccessMode.READ)
  void readOnlyTest() {
    // Multiple tests can hold READ locks simultaneously
  }

  @Test
  @ResourceLock(value = "database", mode = ResourceAccessMode.READ_WRITE)
  void modifyingTest() {
    // Exclusive access - no other test can run
  }
}
```

JUnit 5's `@ResourceLock` prevents conflicts when tests share resources. READ locks allow concurrent access, while READ_WRITE locks ensure exclusive access.

#### Combining Both Approaches

We can use both parallelization modes together for maximum performance:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <!-- Build tool level: 2 JVM forks -->
    <forkCount>2</forkCount>
    <reuseForks>true</reuseForks>

    <!-- Test runner level: Enable JUnit 5 parallelism -->
    <properties>
      <configurationParameters>
        junit.jupiter.execution.parallel.enabled=true
        junit.jupiter.execution.parallel.mode.default=concurrent
      </configurationParameters>
    </properties>
  </configuration>
</plugin>
```

This configuration creates 2 JVM processes, each running tests in parallel using multiple threads. On an 8-core machine, this could utilize all cores effectively.

#### Benefits and Risks of Parallel Testing

**Benefits:**
- Faster test execution
- Better resource utilization
- Faster CI/CD pipelines

**Risks:**
- Race conditions in tests
- Database conflicts
- Port conflicts
- Shared file system issues

### Selective Testing for Faster Feedback

Organize tests by execution speed:

```java
// Tag fast tests
@Tag("fast")
@Test
void shouldValidateIsbn() {
  assertThat(BookValidator.isValidIsbn("978-0132350884")).isTrue();
  assertThat(BookValidator.isValidIsbn("invalid-isbn")).isFalse();
}

// Tag slow tests
@Tag("slow")
@Tag("integration")
@SpringBootTest
@Test
void shouldProcessCompleteBorrowingWorkflow() {
  // Long-running integration test for complete borrowing cycle
}
```

JUnit 5's `@Tag` annotation categorizes tests. Fast unit tests that run in milliseconds get tagged as "fast". Integration tests that load Spring contexts or use databases get tagged as "slow".

This allows developers to run fast tests frequently during development and save slow tests for pre-commit or CI runs.

Run specific test categories:

```bash
# Run only fast tests during development
mvn test -Dgroups="fast"

# Run all tests in CI
mvn test -Dgroups="fast,slow"
```

These Maven commands use the tag system to run specific test groups. During development, running only fast tests provides quick feedback (seconds instead of minutes).

The CI pipeline runs all tests to ensure complete coverage. This selective execution strategy keeps developers productive while maintaining quality.

## Test Data Management

### Creating Test Data Factories

#### Why Test Data Factories Matter

Test data factories solve common problems:
- **Reduce Duplication**: Centralize test data creation
- **Improve Readability**: Express intent clearly
- **Ensure Validity**: Create valid data consistently
- **Enable Variations**: Easy to create specific scenarios

#### Object Mother Pattern

```java
public class BookMother {
  private static final Faker faker = new Faker();
```

The Object Mother pattern creates a central place for generating test objects. The `Faker` library generates realistic test data (names, addresses, ISBNs) automatically.

This is better than hardcoding "Test Book 1" everywhere, as it creates more realistic test scenarios and catches edge cases.

Basic factory method:

```java
public static Book simple() {
  return Book.builder()
    .title(faker.book().title())
    .author(faker.book().author())
    .isbn(generateIsbn())
    .build();
}
```

Variation with specific title:

```java
public static Book withTitle(String title) {
  return simple().toBuilder()
    .title(title)
    .build();
}
```

Scenario-specific factory:

```java
public static Book overdue() {
  return simple().toBuilder()
    .borrowedDate(LocalDate.now().minusDays(30))
    .dueDate(LocalDate.now().minusDays(10))
    .borrowed(true)
    .build();
}
```

Bulk creation method:

```java
public static List<Book> withGenre(String genre, int count) {
  return Stream.generate(() -> simple().toBuilder()
      .genre(genre)
      .build())
    .limit(count)
    .toList();
}
```

### Using Test Fixtures Effectively

#### Builder Pattern for Test Scenarios

Test fixtures create complete scenarios with related data. They're especially useful for integration tests that need realistic data setups.

```java
@Component
@Profile("test")
public class TestScenarios {
  @Autowired
  private BookRepository bookRepository;
  @Autowired
  private UserRepository userRepository;
```

Scenario for overdue books:

```java
public Scenario libraryWithOverdueBooks() {
  User lateReader = userRepository.save(
    UserMother.withOverdueBooks("late@example.com")
  );
```

Create and associate overdue book:

```java
Book overdueBook = bookRepository.save(
  BookMother.overdue().toBuilder()
    .borrowedBy(lateReader)
    .build()
);

return new Scenario(lateReader, List.of(overdueBook));
```

Scenario for popular books:

```java
public Scenario popularBooksScenario() {
  List<Book> books = bookRepository.saveAll(List.of(
    BookMother.withTitle("Clean Code").toBuilder()
      .borrowCount(50).build(),
    BookMother.withTitle("Refactoring").toBuilder()
      .borrowCount(45).build()
  ));

  return new Scenario(null, books);
}

@Value
public static class Scenario {
  User user;
  List<Book> books;
}
```

#### Using Fixtures in Tests

```java
@SpringBootTest
class BookBorrowingTest {
  @Autowired
  private TestScenarios scenarios;
  @Autowired
  private BookshelfService bookshelfService;
```

Using test scenarios in action:

```java
@Test
void shouldPreventBorrowingWithOverdueBooks() {
  // Given - Complete scenario in one line
  Scenario scenario = scenarios.libraryWithOverdueBooks();
  Book availableBook = BookMother.simple();
```

Clear business rule test:

```java
// When/Then
assertThatThrownBy(() ->
  bookshelfService.borrowBook(
    availableBook.getId(),
    scenario.getUser().getId()
  )
).isInstanceOf(BorrowingDeniedException.class)
 .hasMessageContaining("overdue books");
```

### Database Seeding Strategies

#### Approaches to Test Data Setup

1. **SQL Scripts**: Best for static reference data
2. **Object Mothers**: Best for unit tests
3. **CSV/JSON Files**: Best for large datasets
4. **Test Containers Init Scripts**: Best for schema setup
5. **Programmatic Setup**: Best for complex scenarios

Simple SQL script approach:

```java
@Sql("/test-data/books.sql")
@Test
void testWithSqlData() { }
```

Programmatic approach for dynamic data:

```java
@Component
@Profile("test")
public class TestDataLoader {
  @EventListener(ApplicationReadyEvent.class)
  public void loadTestData() {
    if (bookRepository.count() == 0) {
      bookRepository.saveAll(
        BookMother.withGenre("Technology", 5)
      );
    }
  }
}
```

This component loads test data when the application starts in test mode. The `@EventListener` reacts to Spring's `ApplicationReadyEvent`, ensuring all beans are initialized first.

The count check prevents duplicate data if the context is reused. This approach is useful for shared test databases or when using test containers.

CSV for large datasets:

```java
@Value("classpath:test-data/books.csv")
private Resource booksCsv;

@PostConstruct
void loadFromCsv() {
  // Use Spring Batch or custom CSV reader
}
```

For large test datasets (thousands of records), CSV files are more maintainable than code. Spring's `@Value` annotation with `classpath:` prefix loads files from src/test/resources.

The `@PostConstruct` method runs after dependency injection. You'd typically use a CSV library or Spring Batch for the actual parsing.

### Cleanup Procedures

#### Strategies for Test Isolation

1. **@Transactional + @Rollback**: Best for most tests
2. **@DirtiesContext**: Nuclear option, recreates context
3. **Manual Cleanup**: For non-transactional tests
4. **Test Containers**: Fresh database per test class

Strategy 1: Transaction rollback (preferred):

```java
@SpringBootTest
@Transactional
class TransactionalTest {
  @Test
  void testWithAutoRollback() {
    // Changes automatically rolled back
  }
}
```

This is the cleanest approach for database tests. Spring wraps each test method in a transaction and rolls it back after completion.

Your test can insert, update, or delete data, and the database returns to its original state automatically. This is fast and ensures perfect isolation between tests.

Strategy 2: Manual cleanup for specific data:

```java
@Component
@Profile("test")
class TestDataCleaner {
  @Transactional
  public void cleanBorrowingData() {
    jdbcTemplate.update("DELETE FROM borrowing_history");
    jdbcTemplate.update("UPDATE books SET borrowed = false");
  }
}
```

Sometimes you need manual cleanup, especially for non-transactional operations or when using `@Commit`. This component provides methods to reset specific tables. The `jdbcTemplate` executes raw SQL for maximum control.

Call these methods in `@AfterEach` to ensure clean state.

Strategy 3: Fresh database per test:

```java
@Testcontainers
class FreshDatabaseTest {
  @Container
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>();
}
```

Testcontainers provides the ultimate isolation: a fresh database container for each test class. The `@Container` annotation manages the container lifecycle. This approach is slower but guarantees complete isolation.

Perfect for tests that modify database structure or need specific PostgreSQL versions.

## Testing Security

### Authentication and Authorization Testing

#### Key Security Testing Scenarios

1. **Anonymous Access**: What can unauthenticated users do?
2. **Role-Based Access**: Do roles work correctly?
3. **Method Security**: Are service methods protected?
4. **CSRF Protection**: Is CSRF properly configured?
5. **Session Management**: Do sessions timeout correctly?

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {
  @Autowired
  private MockMvc mockMvc;
```

Security tests require `@AutoConfigureMockMvc` to enable MockMvc with security filters. This loads the complete security configuration including authentication, authorization, CSRF protection, and session management.

MockMvc simulates HTTP requests without starting a real server.

Test anonymous access:

```java
@Test
void anonymousUserCannotBorrow() throws Exception {
  mockMvc.perform(post("/books/1/borrow"))
    .andExpect(status().is3xxRedirection())
    .andExpect(redirectedUrlPattern("**/login"));
}
```

This test verifies that unauthenticated users cannot access protected endpoints. The `perform()` method simulates a POST request.

Spring Security intercepts it and, finding no authentication, redirects to the login page (3xx status). The `redirectedUrlPattern()` uses a wildcard to match any host/port.

Test authenticated user access:

```java
@Test
@WithMockUser(roles = "USER")
void userCanBorrowBooks() throws Exception {
  mockMvc.perform(post("/books/1/borrow"))
    .andExpect(status().is3xxRedirection());
}
```

`@WithMockUser` creates a mock authenticated user with the USER role. This simulates a logged-in user without needing real authentication.

The test verifies that authenticated users can access the borrowing endpoint. The 3xx status here indicates successful processing with a redirect (common in web applications).

Test authorization boundaries:

```java
@Test
@WithMockUser(roles = "USER")
void userCannotAccessAdmin() throws Exception {
  mockMvc.perform(get("/admin/users"))
    .andExpect(status().isForbidden());
}
```

This test ensures role-based authorization works correctly. A user with the USER role attempts to access an admin endpoint. The expected 403 Forbidden status confirms that Spring Security blocks the request based on insufficient privileges.

This verifies your security configuration is properly enforcing access rules.

### Testing Secured Endpoints

#### Custom Security Test Annotations

Create reusable security contexts for common test scenarios:

Simple role-based annotation:

```java
@WithMockUser(roles = "LIBRARIAN")
public @interface WithMockLibrarian { }
```

Custom annotations make tests more readable and reduce duplication. This meta-annotation combines `@WithMockUser` with a specific role.

Now tests can use `@WithMockLibrarian` instead of repeating the role configuration. This follows the DRY principle and makes role changes easier.

Complex custom security context:

```java
@WithSecurityContext(factory = ReaderSecurityFactory.class)
public @interface WithMockReader {
  String email() default "reader@example.com";
  boolean hasOverdueBooks() default false;
}
```

For complex security scenarios, `@WithSecurityContext` allows custom security context creation. The factory class can create a principal with specific attributes like email and business state (overdue books).

This enables testing business rules that depend on user attributes beyond simple roles.

Usage in tests:

```java
@Test
@WithMockLibrarian
void librarianCanManageBooks() {
  // Test with librarian role
}

@Test
@WithMockReader(hasOverdueBooks = true)
void readerWithOverdueBooksCannotBorrow() {
  // Test with custom principal
}
```

These annotations make security tests expressive. The first test runs with librarian privileges. The second creates a reader with overdue books, testing a business rule that combines authentication (who you are) with authorization (what you can do based on your state).

### Testing Method-Level Security

#### @PreAuthorize and @PostAuthorize Testing

Service with method-level security:

```java
@Service
public class BookManagementService {
  @PreAuthorize("hasRole('LIBRARIAN')")
  public Book addBook(String title, String author) {
    // Implementation
  }
```

Method with complex security expression:

```java
@PreAuthorize("hasRole('USER') and !#user.hasOverdueBooks")
public BorrowResult borrowBook(Long bookId, User user) {
  // Implementation
}
```

Test class setup:

```java
@SpringBootTest
class MethodSecurityTest {
  @Autowired
  private BookManagementService service;
```

Test unauthorized access:

```java
@Test
@WithMockUser(roles = "USER")
void userCannotAddBooks() {
  assertThrows(AccessDeniedException.class,
    () -> service.addBook("Title", "Author")
  );
}
```

Test authorized access:

```java
@Test
@WithMockUser(roles = "LIBRARIAN")
void librarianCanAddBooks() {
  assertDoesNotThrow(
    () -> service.addBook("Title", "Author")
  );
}
```

## Best Practices Summary

### Test Design Principles

#### FIRST Principles

**F**ast - Tests should run quickly
**I**ndependent - Tests don't depend on each other
**R**epeatable - Same result every time
**S**elf-Validating - Pass or fail, no manual inspection
**T**imely - Written just before production code

#### The Three A's Pattern

Structured test with clear sections:

```java
@Test
void shouldCalculateLateFee() {
  // Arrange (Given)
  BorrowingRecord record = createOverdueRecord(7);

  // Act (When)
  BigDecimal fee = calculator.calculateLateFee(record);

  // Assert (Then)
  assertThat(fee).isEqualTo("3.50"); // $0.50 × 7 days
}
```

#### Test Naming Conventions

Behavior-driven naming pattern:

```java
// Pattern: should_ExpectedBehavior_When_StateUnderTest
void should_ThrowException_When_BookNotFound()
void should_ReturnBook_When_UserIsAuthorized()
```

Method-focused naming pattern:

```java
// Pattern: methodName_StateUnderTest_ExpectedBehavior
void borrowBook_WithOverdueBooks_ThrowsException()
void calculateFee_SevenDaysLate_Returns3Dollars50()
```

### Key Takeaways

#### Avoid These Anti-Patterns
1. **Over-mocking**: Mock only external dependencies
2. **Testing implementation**: Focus on behavior, not how
3. **Fragile tests**: Make tests resilient to minor changes
4. **Slow tests**: Keep feedback loops fast
5. **Shared state**: Ensure test isolation

#### Embrace These Practices
1. **Test slices**: Use the right tool for the job
2. **Object mothers**: Centralize test data creation
3. **Context caching**: Optimize for reuse
4. **Parallel execution**: Leverage modern hardware
5. **Continuous refactoring**: Tests are code too
