# Chapter 4: Testing Pitfalls and Best Practices

Throughout our testing journey, we've explored the powerful tools and techniques Spring Boot offers.

However, even with the best tools, it's easy to fall into common traps that undermine test effectiveness.

In this chapter, we'll identify these pitfalls and establish best practices that lead to maintainable, reliable test suites.

## Common Testing Anti-Patterns

### Recognizing and Avoiding Testing Anti-Patterns

Testing anti-patterns are practices that seem reasonable at first but ultimately harm test quality and maintainability. Let's examine the most common ones and learn how to avoid them.

### Over-Mocking: The Isolation Trap

The Problem: Excessive mocking creates tests that pass but don't reflect real system behavior. When we mock everything, we're essentially testing our mocks, not our code.

Over-mocking hurts our testing efforts in several critical ways. First, it creates false confidence - tests pass even when integration would fail in production. Second, it leads to brittle tests where any refactoring breaks tests, even if behavior remains unchanged. The maintenance burden becomes overwhelming as updating mocks becomes more work than the actual code. We also lose the ability to catch integration issues, as problems between components go undetected. Finally, the test's intent becomes unclear - it's hard to understand what the test actually verifies when everything is mocked.

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

The Solution: Use integration tests for complex workflows and mock only external boundaries.

Effective mocking follows clear guidelines that help us avoid these pitfalls. We should mock external dependencies only - databases, web services, email servers - while using real components whenever possible and letting Spring wire real beans. We can prefer test slices like `@DataJpaTest` and `@WebMvcTest` for focused testing, and remember to mock at system boundaries, not between our own components.

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

The Problem: Tests that verify internal implementation make refactoring difficult and don't ensure correct behavior.

We can recognize when we're testing implementation by watching for several warning signs. Using reflection to access private fields or methods is a clear indicator, as is verifying method calls - essentially testing HOW something works instead of WHAT it does. Creating spy objects to monitor the class under test, counting exact method invocations, and writing tests that know too much about internal details all point to implementation-focused testing rather than behavior-focused testing.

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

The Solution: Test behavior and outcomes, not implementation.

To test behavior correctly, we need to shift our focus to what matters to the business. We should focus on the public API by only testing through public methods, verify outcomes by checking return values and side effects, and test state changes by verifying database or external changes. The key is to ignore implementation details - we don't care HOW something is done, only that it produces the correct result. This black-box testing approach treats components as sealed units where we only care about inputs and outputs.

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

The Problem: Tests that break with minor, unrelated changes indicate tight coupling to implementation.

Several factors make tests fragile and prone to breaking with unrelated changes. Exact string matching tests precise formatting that might change for aesthetic reasons.

Order dependencies assume specific ordering that might be optimized later. Timing assumptions rely on hard-coded delays or timeouts that work inconsistently across environments. Environmental dependencies create tests that only work on specific operating systems or locales.

Finally, shared state between tests creates dependencies where one test's behavior affects another, leading to unpredictable failures.

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

This assertion expects an exact string match, including specific spacing, line breaks, and field order. If someone adds an extra space, changes the order of fields, or adds a colon, this test breaks even though the information is still correct. This makes refactoring painful.

The Solution: Test essential characteristics, not exact formatting.

Making tests resilient requires focusing on what truly matters rather than superficial details. We should test content rather than format by verifying that information is present without caring about its exact presentation.

Using flexible matchers like "contains" or pattern matching makes tests more forgiving of minor changes.

Parameterizing tests makes them data-driven and less brittle. Creating domain-specific assertions abstracts away formatting concerns. Most importantly, we should test contracts - focusing on what must be true from a business perspective rather than implementation details.

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

### Identifying and Mitigating Flaky Tests

A flaky test is a test that can both pass and fail for the same code. These are among the most damaging types of tests because they erode trust in the entire test suite. When developers can't trust the test results, they start ignoring failures, and the value of the test suite diminishes.

Common Causes of Flakiness:

*   Asynchronous Operations: Tests that don't properly wait for asynchronous operations to complete are a primary source of flakiness. A test might pass if the operation finishes quickly but fail if there's a slight delay.
*   Shared State: If tests are not properly isolated, one test can leave the system in a state that causes a subsequent test to fail.
*   Concurrency Issues: In parallel test execution, tests that are not thread-safe can interfere with each other.
*   External Dependencies: Relying on external services that may be slow or unavailable can cause tests to fail intermittently.

Strategies for Mitigation:

1.  Use `Awaitility` for Asynchronous Code: Instead of using `Thread.sleep()`, which leads to both slow and flaky tests, use a library like [Awaitility](https://github.com/awaitility/awaitility). It provides a powerful DSL for waiting for asynchronous operations to complete.

    ```java
    // Don't do this:
    // Thread.sleep(2000);
    // assertThat(asyncService.isDone()).isTrue();

    // Do this instead:
    await().atMost(5, TimeUnit.SECONDS).until(() -> asyncService.isDone());
    ```

2.  Ensure Test Isolation: As discussed earlier, use `@Transactional` for database tests, clean up created files, and reset mocks between tests to ensure that each test runs in a clean, predictable environment.

3.  Use Resource Locks for Parallel Tests: When running tests in parallel, use JUnit 5's `@ResourceLock` annotation to prevent tests that use shared resources from running concurrently.

4.  Mock External Services: Use tools like WireMock or `@MockBean` to create stable, predictable responses from external dependencies.


### The Slow Test Suite

The Problem: Slow tests discourage frequent execution and delay feedback.

Several factors commonly contribute to slow test execution. Large data sets with thousands of records often provide no additional testing value compared to smaller, representative samples.

Real time delays using `Thread.sleep()` or actual timeouts waste precious development time. Full context loading through unnecessary use of `@SpringBootTest` adds overhead when lighter test slices would suffice.

Calling real external services like APIs or remote databases introduces network latency and reliability issues. Finally, inefficient queries and N+1 problems that might be acceptable in production can significantly slow down test execution.

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

The Solution: Use appropriate test sizes and avoid real delays.

Several strategies can dramatically improve test performance. Using representative data means choosing the minimum dataset that proves our point rather than massive collections.

Using test slices like `@DataJpaTest` instead of `@SpringBootTest` loads only necessary components.

Replacing `Thread.sleep()` with async utilities like Awaitility provides faster, more reliable waiting mechanisms. Mocking time through Clock abstractions makes time-based logic testable without real delays. Finally, running independent tests concurrently through parallel execution takes advantage of modern multi-core systems.

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

Spring's context caching can reduce test execution time by 50-90%. When Spring loads an application context for a test, it caches it and reuses it for other tests with identical configuration. This saves the expensive initialization process.

Several factors can break context caching and force Spring to create new contexts. Different `@MockBean` or `@SpyBean` configurations mean each unique mock creates a new context. Any property differences force a new context, as do different active profiles since they're part of the cache key.

The `@DirtiesContext` annotation explicitly marks a context as dirty, and different configuration classes through additional `@Import` or `@TestConfiguration` annotations also prevent reuse.

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

Context pollution occurs when tests modify shared state, forcing Spring to create new contexts. This dramatically slows down test execution.

Common pollution sources include modifying singleton beans, changing system properties, altering database schemas, and clearing caches.

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

### Visualizing Test Performance with Spring Test Profiler

While understanding context caching theory is important, seeing the actual impact on our test suite provides valuable insight. The [Spring Test Profiler](https://github.com/PragmaTech-GmbH/spring-test-profiler) is an open-source tool developed by [PragmaTech](https://pragmatech.digital/) that helps us visualize and optimize Spring Test execution by providing detailed reports on context caching behavior.

Key Benefits:

- Visual Reports: HTML reports showing context reuse statistics
- Cache Hit Analysis: Identify which tests share contexts and which create new ones
- Optimization Opportunities: Discover tests that could be grouped for better caching
- Performance Metrics: Track context creation times and memory usage

The tool is particularly valuable for large test suites where context caching optimization can dramatically reduce build times.

First, let's add the Spring Test Profiler dependency to our `pom.xml`:

```xml
<dependency>
  <groupId>digital.pragmatech.testing</groupId>
  <artifactId>spring-test-profiler</artifactId>
  <version>0.0.11</version> <!-- use the latest version -->
  <scope>test</scope>
</dependency>
```

The profiler requires Java 17+ and works with Spring Boot 3.x applications. Adding it to our Shelfie application is straightforward since we're already using Spring Boot > 3.X.

The profiler offers two activation methods.

The recommended approach uses Spring's auto-configuration mechanism.

Create a file `src/test/resources/META-INF/spring.factories`:

```properties
# Enable Spring Test Profiler automatically
org.springframework.test.context.TestExecutionListener=\
digital.pragmatech.testing.SpringTestProfilerListener
org.springframework.context.ApplicationContextInitializer=\
digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer
```

This registers the profiler as a `TestExecutionListener`, which automatically tracks all Spring test executions without requiring changes to individual test classes.

Alternative manual activation (useful for selective profiling):

```java
@SpringBootTest
@TestExecutionListeners(
  value = SpringTestProfilerListener.class,
  mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@ContextConfiguration(initializers = ContextDiagnosticApplicationInitializer.class)
class BookshelfServiceIT {
  // Test methods
}
```

After running tests, the profiler generates an HTML report at:

- Maven: `target/spring-test-profiler/latest.html`
- Gradle: `build/spring-test-profiler/latest.html`

The report includes several key sections:

- Context Summary: Shows total contexts created, reused, and cache hit ratio
- Test Execution Timeline: Visualizes when contexts are created vs. reused
- Context Details: Lists all unique contexts with their configuration hash
- Test Groupings: Shows which tests share the same context

Let's examine what different scenarios look like in our Shelfie application:

Poor Caching Example

```java
// Each test creates its own context
@SpringBootTest(properties = "debug=true")
class BookServiceSlowTest { }

@SpringBootTest(properties = "logging.level.org.hibernate=DEBUG")
class BookRepositorySlowTest { }

@SpringBootTest
@MockBean
class BookControllerSlowTest {
  @MockBean
  private BookService bookService;
}
```

In the profiler report, we'd see:

- Context Reuse: 0%
- Contexts Created: 3
- Each test waits for full context initialization

Optimized Caching Example:
```java
// Base configuration shared across tests
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine");
}

// These tests share the same context
class BookServiceOptimizedTest extends BaseIntegrationTest { }
class BookRepositoryOptimizedTest extends BaseIntegrationTest { }
class BookControllerOptimizedTest extends BaseIntegrationTest { }
```

The profiler report would show:

- Context Reuse: 67% (2 of 3 tests reuse context)
- Contexts Created: 1
- Significant time savings for subsequent tests

Let's add the profiler to our Shelfie application and run a real optimization cycle.

First, run our existing tests to see the baseline:

```bash
./mvnw verify
```

The profiler generates a report showing current context usage patterns.

A sample report [looks like this](https://github.com/PragmaTech-GmbH/spring-test-profiler?tab=readme-ov-file#features).

In our case, we might discover:

- Integration tests using different `@MockBean` configurations
- Property variations breaking context caching
- Opportunities to create shared base classes

After identifying issues, we can refactor for better caching:

```java
// Before: Multiple contexts due to different mocks
@SpringBootTest
class BookBorrowingIT {
  @MockBean private EmailService emailService;
}

@SpringBootTest
class BookReturnIT {
  @MockBean private NotificationService notificationService;
}

// After: Shared context with consolidated mocks
@SpringBootTest
public abstract class BaseLibraryIT {
  @MockBean protected EmailService emailService;
  @MockBean protected NotificationService notificationService;
}

class BookBorrowingIT extends BaseLibraryIT { }
class BookReturnIT extends BaseLibraryIT { }
```

Re-running tests with the profiler active shows the improvement in context reuse metrics.

When using the Spring Test Profiler effectively, we should run it regularly and include profiling in CI/CD to track performance trends. Focus on high-impact changes by prioritizing optimization of tests with the longest context creation times.

Balance context reuse against test isolation - don't sacrifice test independence for minor performance gains. Finally, document successful caching patterns and share them across your team.

The Spring Test Profiler transforms abstract context caching concepts into actionable insights, making test suite optimization much more approachable for development teams.

### Reducing Test Startup Time

Several techniques can significantly reduce test startup time. Lazy initialization creates beans only when needed, while disabling unused auto-configuration turns off unnecessary starters.

Specific component scanning limits package scanning to relevant areas, and skipping database migration in favor of pre-created schemas eliminates setup overhead. Finally, conditional beans using `@ConditionalOnProperty` allow us to exclude expensive components during testing.

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

There are two distinct approaches to parallelize test execution, each with different trade-offs. Build tool level parallelization forks multiple JVM processes, while test runner level parallelization uses multiple threads within a single JVM. Understanding both modes helps us choose the right strategy for our specific needs.

Build tools like Maven and Gradle can spawn multiple JVM processes to run tests in parallel. Each fork is a completely isolated Java process with its own memory space.

Maven Surefire Configuration:

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

Maven Failsafe Configuration (Integration Tests):

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

Gradle Configuration:

```groovy
test {
  maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
  forkEvery = 50  // Create new fork every 50 tests

  jvmArgs = ['-Xmx1024m', '-XX:+UseG1GC']
}
```

Gradle dynamically calculates the number of forks based on available CPU cores. The `forkEvery` setting creates fresh JVMs periodically to prevent memory leaks from accumulating.

Advantages of JVM Forking:

- Complete isolation between test groups
- No shared memory concerns
- Crashes in one fork don't affect others
- Can use different JVM settings per fork

Disadvantages:

- High memory overhead (each JVM needs its own heap)
- Slower startup time for each fork
- Context must be loaded in each fork

Test runners like JUnit 5 can execute tests using multiple threads within the same JVM. This approach shares memory and loaded contexts between threads.

JUnit 5 Parallel Configuration:

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

Fixed Thread Pool Configuration:

```properties
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

This creates exactly 4 threads regardless of available CPU cores. Useful for consistent behavior across different environments.

Controlling Parallel Execution in Code:

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

Resource Locks for Shared Resources:

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

Parallel testing offers significant benefits including faster test execution, better resource utilization, and faster CI/CD pipelines. However, it also introduces risks such as race conditions in tests, database conflicts, port conflicts, and shared file system issues.

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

## Managing Test Properties and Configurations

Effectively managing configuration properties is crucial for creating reliable and maintainable tests. Spring Boot provides a flexible and powerful system for handling test-specific properties, but it's important to understand the hierarchy and best practices.

### The Property Hierarchy

Spring Boot loads properties from various sources with a specific order of precedence. When it comes to testing, the hierarchy is as follows (from highest to lowest precedence):

1.  `@TestPropertySource` (inline properties): Properties defined directly in the annotation have the highest priority.
2.  `@TestPropertySource` (file-based properties): Properties loaded from files specified in the annotation.
3.  `@SpringBootTest` (properties attribute): Properties defined in the `properties` attribute of the `@SpringBootTest` annotation.
4.  Application-specific properties outside your packaged jar: (e.g., `application-test.properties` in the same directory as the jar).
5.  Application-specific properties inside your packaged jar: (e.g., `application-test.properties` in `src/test/resources`).

Understanding this hierarchy is key to avoiding confusion and ensuring that your tests are using the correct configuration.

### Best Practices for Test Properties

-   Use `application-test.properties` for common test configuration: Create an `application-test.properties` file in `src/test/resources` to define properties that should apply to *all* tests. This is the perfect place to configure your test database connection, disable production features, and set up logging for tests.

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:tc:postgresql:16-alpine:///testdb
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
spring.jpa.hibernate.ddl-auto=create-drop
```

- Use `@TestPropertySource` for test-specific overrides: When a specific test or test class needs to override a default test property, use the `@TestPropertySource` annotation. This makes the test's unique configuration explicit and self-contained.

```java
@SpringBootTest
@TestPropertySource(properties = "feature.toggle.new-algorithm.enabled=true")
class NewAlgorithmTest {
    // This test will run with the new algorithm feature toggle enabled.
}
```

- Avoid scattering properties: Keep your property definitions consolidated. Prefer using `application-test.properties` for shared settings and `@TestPropertySource` for specific overrides. Avoid mixing multiple ways of setting the same property, as it can lead to confusion.

## Test Data Management

### Creating Test Data Factories

Test data factories solve several common problems in our testing approach. They reduce duplication by centralizing test data creation, improve readability by expressing intent clearly, ensure validity by creating valid data consistently, and enable variations by making it easy to create specific scenarios.

The Object Mother pattern provides an excellent foundation for test data creation.

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

When using fixtures in our tests, we can create complete scenarios with minimal setup:

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

Several approaches work well for different test data setup scenarios. SQL scripts work best for static reference data, Object Mothers excel for unit tests, CSV/JSON files handle large datasets efficiently, Test Containers init scripts are ideal for schema setup, and programmatic setup works best for complex scenarios.

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

Different strategies work for different test isolation needs. `@Transactional` with `@Rollback` works best for most tests, `@DirtiesContext` serves as a nuclear option that recreates the context, manual cleanup handles non-transactional tests, and Test Containers provide a fresh database per test class.

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

Key security testing scenarios include verifying anonymous access limitations, ensuring role-based access controls work correctly, confirming service method protection, validating CSRF configuration, and testing session timeout behavior.

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

We can create reusable security contexts for common test scenarios through custom annotations:

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

Testing method-level security annotations like `@PreAuthorize` and `@PostAuthorize` ensures our business rules are properly enforced:

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

The FIRST principles guide effective test design: tests should be Fast and run quickly, Independent without dependencies on each other, Repeatable with the same result every time, Self-Validating to pass or fail without manual inspection, and Timely by being written just before production code.

The Three A's pattern structures tests with clear sections:

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

Consistent test naming conventions improve readability and maintainability:

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

We should avoid these anti-patterns: over-mocking by mocking only external dependencies, testing implementation by focusing on behavior rather than how, creating fragile tests by making them resilient to minor changes, writing slow tests by keeping feedback loops fast, and using shared state by ensuring test isolation.

We should embrace these practices: using test slices and the right tool for each job, implementing object mothers to centralize test data creation, optimizing context caching for reuse, leveraging parallel execution and modern hardware, and continuously refactoring since tests are code too.
