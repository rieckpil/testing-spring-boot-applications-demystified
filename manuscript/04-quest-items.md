# Chapter 4: Collecting the Quest Items - Performance & Best Practices

![Quest Items Chamber](resources/quest-items-chamber.png)

## The Chamber of Artifacts

You've defeated all three bosses. The path ahead opens into a vast chamber filled with glowing artifacts. These are the quest items that will transform you from a capable tester into a testing master.

You already possess two items:
- ⚡ **Lightning Shield** (Fast feedback from test slices)
- 🔍 **Scroll of Truth** (Integration confidence)

But three more powerful artifacts await:

**🧿 The Caching Amulet**: Cut your test suite runtime in half through context caching mastery

**⚡ Lightning Shield (Enhanced)**: Harness parallelization for even faster execution

**🔍 Scroll of Truth (Enhanced)**: Use mutation testing to ensure your tests actually test something

Let's collect them all.

## Quest Item #1: 🧿 The Caching Amulet

### The Problem: The 26-Minute Build

Picture this scenario: A development team has 150 integration tests. Each test uses `@SpringBootTest`. The team followed best practices, wrote comprehensive tests, and achieved good coverage.

But there's a problem: **the test suite takes 26 minutes to run**.

Developers stop running tests locally. CI/CD pipelines slow down. Feedback loops stretch from minutes to hours. The team becomes frustrated.

Then they discover the Caching Amulet.

After applying context caching strategies: **12 minutes**.

**Same tests. Same coverage. Half the time.**

This is the power of the Caching Amulet.

### Understanding Spring Test Context Caching

Here's what most developers don't realize: **Spring can reuse application contexts between tests**.

When Spring runs your first integration test:
1. Loads the application context (5-15 seconds)
2. Runs the test (1 second)
3. Caches the context for reuse

When Spring runs your second integration test with **identical configuration**:
1. Reuses cached context (instant!)
2. Runs the test (1 second)

The second test skips the expensive context startup entirely.

**The Cache Key**

Spring determines if two tests can share a context by comparing:
- Configuration classes (`@SpringBootTest` classes)
- Active profiles (`@ActiveProfiles`)
- Properties (`@TestPropertySource`, inline properties)
- Mock beans (`@MockBean`, `@SpyBean`)
- Test property sources
- Context initializers
- Context customizers

If all of these match, Spring reuses the context. If any differ, Spring creates a new context.

### Example: Shared vs. Separate Contexts

**Tests that share a context (fast!):**

```java
@SpringBootTest
@ActiveProfiles("test")
class BookServiceIT {
  @Test
  void shouldCreateBook() { }
}

@SpringBootTest
@ActiveProfiles("test")
class BookRepositoryIT {
  @Test
  void shouldFindBook() { }
}
```

Both tests have identical configuration. Spring loads one context, runs both tests. Total context startups: **1**.

**Tests that create separate contexts (slow!):**

```java
@SpringBootTest
@ActiveProfiles("test")
class BookServiceSlowIT {
  @Test
  void shouldCreateBook() { }
}

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "logging.level.org.hibernate=DEBUG")
class BookRepositorySlowIT {
  @Test
  void shouldFindBook() { }
}
```

The second test has different properties. Spring creates two contexts. Total context startups: **2**.

The difference? **5-15 seconds per additional context**.

### The Friday Afternoon Story: From 26 to 12 Minutes

Here's what the team discovered in their test suite:

**Anti-Pattern #1: Different MockBeans Everywhere**

```java
@SpringBootTest
class Test1 {
  @MockBean private EmailService emailService;
}

@SpringBootTest
class Test2 {
  @MockBean private NotificationService notificationService;
}

@SpringBootTest
class Test3 {
  @MockBean private EmailService emailService;
  @MockBean private SmsService smsService;
}
```

Each test has different `@MockBean` combinations. Each creates its own context.

**Solution: Consolidate Mock Beans in Base Class**

Create a base class with all common mocks:

```java
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  @MockBean protected EmailService emailService;
  @MockBean protected NotificationService notificationService;
  @MockBean protected SmsService smsService;

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine");
}
```

All mocks are declared once in the base class. Every subclass gets the same mock configuration.

Now tests extend the base:

```java
class Test1 extends BaseIntegrationTest {
  @Test void test1() { }
}

class Test2 extends BaseIntegrationTest {
  @Test void test2() { }
}

class Test3 extends BaseIntegrationTest {
  @Test void test3() { }
}
```

All three tests share one base configuration. Spring creates **one context** for all three.

Result: **3 context startups → 1 context startup** = **30-45 seconds saved**.

**Anti-Pattern #2: Scattered Property Overrides**

```java
@SpringBootTest(properties = "spring.jpa.show-sql=true")
class DebugTest1 { }

@SpringBootTest(properties = "logging.level.root=DEBUG")
class DebugTest2 { }

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
class DebugTest3 { }
```

Each test has different properties. Each creates a new context.

**Solution: Shared Test Profile**

```properties
# src/test/resources/application-test.properties
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.springframework.web=INFO
```

```java
@SpringBootTest
@ActiveProfiles("test")
class Test1 { }

@SpringBootTest
@ActiveProfiles("test")
class Test2 { }

@SpringBootTest
@ActiveProfiles("test")
class Test3 { }
```

All tests use the `test` profile. Spring creates **one context**.

**Anti-Pattern #3: The @DirtiesContext Nuclear Option**

```java
@SpringBootTest
class BookServiceIT {

  @Test
  @DirtiesContext // Forces context recreation!
  void shouldClearCache() {
    cacheManager.getCache("books").clear();
    // Test logic
  }
}
```

`@DirtiesContext` tells Spring: "This test corrupted the context. Throw it away and create a new one for the next test."

Using this on 10 tests = 10 context recreations = **50-150 seconds wasted**.

**Solution: Mock the Cache Instead**

Mock the CacheManager in your base class:

```java
@SpringBootTest
public abstract class BaseIntegrationTest {

  @MockBean protected CacheManager cacheManager;

  @BeforeEach
  void setupCache() {
    when(cacheManager.getCache(any()))
      .thenReturn(new ConcurrentMapCache("test-cache"));
  }
}
```

Each `@BeforeEach` creates a fresh mock cache. No real cache to corrupt.

Tests use the mock without dirtying the context:

```java
class BookServiceIT extends BaseIntegrationTest {

  @Test
  void shouldClearCache() {
    // Each test gets a fresh mock cache
    // No context dirtying needed!
  }
}
```

No more `@DirtiesContext`. Contexts stay clean and reusable. This approach is **hundreds of times faster**.

### Visualizing Context Caching with Spring Test Profiler

Understanding context caching theoretically is one thing. Seeing it in action is another.

The [Spring Test Profiler](https://github.com/PragmaTech-GmbH/spring-test-profiler) generates HTML reports showing exactly how your contexts are cached.

**Installation:**

```xml
<dependency>
  <groupId>digital.pragmatech.testing</groupId>
  <artifactId>spring-test-profiler</artifactId>
  <version>0.0.11</version>
  <scope>test</scope>
</dependency>
```

**Activation (Automatic):**

Create `src/test/resources/META-INF/spring.factories`:

```properties
org.springframework.test.context.TestExecutionListener=\
digital.pragmatech.testing.SpringTestProfilerListener
org.springframework.context.ApplicationContextInitializer=\
digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer
```

**Run your tests:**

```bash
./mvnw verify
```

**View the report:**

Open `target/spring-test-profiler/latest.html`

The report shows:
- **Context Reuse Percentage**: How many tests shared contexts
- **Unique Contexts**: How many different contexts were created
- **Timeline**: Visual representation of context creation vs. reuse
- **Cache Misses**: Which tests broke caching and why

**Example Report Interpretation:**

```
Context Summary:
- Total Tests: 50
- Contexts Created: 5
- Context Reuse: 90%
- Time Saved: ~225 seconds
```

This means 45 out of 50 tests reused existing contexts. If each context takes 5 seconds to start, that's `45 * 5 = 225 seconds` saved!

**Finding Optimization Opportunities:**

The profiler highlights tests with unique contexts:

```
Context #4 (Used by 1 test):
- BookServiceSlowIT
  Difference from Context #1:
    + Additional property: spring.jpa.show-sql=true
```

This reveals that `BookServiceSlowIT` creates its own context just for SQL logging. Remove that property override, and it will share Context #1 with 40 other tests.

### Spring Boot 4 Feature: Context Pausing

Spring Framework 7 (part of Spring Boot 4) introduces a game-changing feature: **context pausing**.

**The Old Problem:**

When contexts are cached, all beans remain active:
- Background threads keep running
- Scheduled tasks keep executing
- Message listeners keep polling queues

If you have 3 cached contexts with message listeners, all 3 are fighting over the same messages!

**The New Solution:**

Paused contexts freeze these active components:

```java
@SpringBootTest
class BookServiceIT {

  @Test
  void test1() {
    // Context starts, test runs, context pauses
  }
}

@SpringBootTest
class BookRepositoryIT {

  @Test
  void test2() {
    // Cached context resumes, test runs, context pauses again
  }
}
```

Between tests, the cached context is **paused**:
- `@Scheduled` tasks don't execute
- Message listeners don't consume messages
- Background threads are dormant

When the context is needed again, it **resumes** instantly.

This makes context caching more reliable without `@DirtiesContext` workarounds.

### The Caching Amulet Strategy Guide

**Do:**
- ✅ Create a `BaseIntegrationTest` with shared configuration
- ✅ Use `@ActiveProfiles("test")` consistently
- ✅ Consolidate `@MockBean` declarations in base classes
- ✅ Put shared properties in `application-test.properties`
- ✅ Use the Spring Test Profiler to find cache misses

**Don't:**
- ❌ Use `@MockBean` in individual test classes
- ❌ Override properties with `@TestPropertySource` unnecessarily
- ❌ Use `@DirtiesContext` without understanding the cost
- ❌ Mix different profiles across similar tests
- ❌ Create unique configurations for each test class

**Monitor:**

Enable Spring Test cache logging:

```properties
# src/test/resources/logback-test.xml
<logger name="org.springframework.test.context.cache" level="DEBUG"/>
```

This shows cache hits/misses in your test output.

## Quest Item #2: ⚡ Lightning Shield (Enhanced)

You already have the Lightning Shield from test slicing. Now let's enhance it with **parallelization**.

### Two Parallelization Strategies

**Strategy #1: JVM Forking (Build Tool Level)**

Your build tool (Maven/Gradle) creates multiple JVM processes, each running tests.

**Maven Surefire (Unit Tests):**

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

This creates 4 JVM processes. Each process:
- Gets its own heap memory (1GB here)
- Runs 1/4 of the tests
- Loads its own Spring contexts

**Maven Failsafe (Integration Tests):**

```xml
<plugin>
  <artifactId>maven-failsafe-plugin</artifactId>
  <configuration>
    <forkCount>2</forkCount>
    <reuseForks>false</reuseForks>
  </configuration>
</plugin>
```

For integration tests, we use fewer forks (expensive to start) and `reuseForks=false` for complete isolation.

**Gradle Configuration:**

```groovy
test {
  maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
  forkEvery = 50  // New fork every 50 tests
}
```

Gradle calculates forks based on CPU cores.

**Pros:**
- Complete isolation (separate JVMs)
- No shared memory issues
- Crashes don't affect other forks

**Cons:**
- High memory usage (each fork needs heap)
- Slower startup (each fork loads classes)
- Contexts can't be shared across forks

**Strategy #2: Thread-Based Parallelization (JUnit 5)**

JUnit 5 runs tests using multiple threads **within the same JVM**.

**Configuration (`src/test/resources/junit-platform.properties`):**

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent

# Dynamic: threads = CPU cores * factor
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=1.0
```

**Fixed thread pool:**

```properties
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

**Controlling Execution Per Test:**

```java
@Execution(ExecutionMode.CONCURRENT)
class ParallelTest {
  // Tests in this class run in parallel
}

@Execution(ExecutionMode.SAME_THREAD)
class SequentialTest {
  // Tests in this class run sequentially
}
```

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
    // Exclusive access - no other test runs
  }
}
```

`@ResourceLock` prevents conflicts. READ locks allow concurrent access. READ_WRITE locks ensure exclusive access.

**Pros:**
- Lower memory overhead (one JVM)
- Can share cached Spring contexts
- Faster overall (no JVM startup)

**Cons:**
- Shared memory (potential conflicts)
- Need careful synchronization
- Spring context caching more complex

### Combining Both Strategies

You can use both:

```xml
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <!-- Build tool: 2 JVM forks -->
    <forkCount>2</forkCount>

    <!-- JUnit 5: Enable thread parallelization -->
    <properties>
      <configurationParameters>
        junit.jupiter.execution.parallel.enabled=true
        junit.jupiter.execution.parallel.mode.default=concurrent
      </configurationParameters>
    </properties>
  </configuration>
</plugin>
```

On an 8-core machine:
- 2 JVM processes
- Each runs 4 threads
- Total: 8 concurrent tests

### Selective Test Execution with Tags

Not all tests need to run all the time:

```java
@Tag("fast")
@Test
void shouldValidateIsbn() {
  // Fast unit test (milliseconds)
}

@Tag("slow")
@Tag("integration")
@SpringBootTest
@Test
void shouldProcessCompleteBorrowingWorkflow() {
  // Slow integration test (seconds)
}
```

**Run only fast tests during development:**

```bash
mvn test -Dgroups="fast"
```

**Run all tests in CI:**

```bash
mvn test -Dgroups="fast,slow"
```

This gives developers instant feedback (fast tests) while CI gets comprehensive coverage (all tests).

## Quest Item #3: 🔍 Scroll of Truth (Enhanced)

The Scroll of Truth gave you integration confidence. Now enhance it with **mutation testing** to ensure your tests truly test.

### The Coverage Metric Lie

You have 100% code coverage. All lines executed. All branches taken. Green checkmarks everywhere.

**But your tests might be useless.**

Here's why:

```java
public class PriceCalculator {
  public double calculateDiscount(double price) {
    if (price > 100) {
      return price * 0.9; // 10% discount
    }
    return price;
  }
}
```

**Test with 100% coverage:**

```java
@Test
void testCalculateDiscount() {
  PriceCalculator calc = new PriceCalculator();
  double result = calc.calculateDiscount(150);
  // No assertion! But 100% coverage achieved.
}
```

This test executes all code but verifies **nothing**. If you change the discount to 50%, the test still passes.

### Enter Mutation Testing

Mutation testing is like having an evil twin modify your code:

**Original code:**

```java
if (price > 100) {
  return price * 0.9;
}
```

**Mutant #1:**

```java
if (price >= 100) { // Changed > to >=
  return price * 0.9;
}
```

**Mutant #2:**

```java
if (price < 100) { // Changed > to <
  return price * 0.9;
}
```

**Mutant #3:**

```java
if (price > 100) {
  return price * 0.5; // Changed 0.9 to 0.5
}
```

Mutation testing runs your tests against each mutant. If your tests still pass with mutated code, it means your tests didn't actually verify that behavior.

**Strong test that kills mutants:**

```java
@Test
void shouldApplyDiscountAbove100() {
  PriceCalculator calc = new PriceCalculator();

  // Test boundary: 100 should NOT get discount
  assertEquals(100.0, calc.calculateDiscount(100), 0.01);

  // Test above threshold: should get 10% discount
  assertEquals(135.0, calc.calculateDiscount(150), 0.01); // 150 * 0.9

  // Test below threshold: should get no discount
  assertEquals(50.0, calc.calculateDiscount(50), 0.01);
}
```

This test would **kill all three mutants**:
- Mutant #1: Fails (100.0 != 90.0)
- Mutant #2: Fails (inverted logic)
- Mutant #3: Fails (135.0 != 75.0)

### Using PIT for Mutation Testing

[PIT](https://pitest.org/) is the leading mutation testing tool for Java.

**Maven Configuration:**

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.15.3</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <targetClasses>
      <param>de.rieckpil.blog.*</param>
    </targetClasses>
    <targetTests>
      <param>de.rieckpil.blog.*</param>
    </targetTests>
    <outputFormats>
      <format>HTML</format>
    </outputFormats>
  </configuration>
</plugin>
```

**Run mutation testing:**

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

**View the report:**

Open `target/pit-reports/index.html`

**Reading the Report:**

```
Class: PriceCalculator
Line Coverage: 100%
Mutation Coverage: 67%

Mutations:
- Changed conditional boundary: KILLED ✓
- Replaced return value: KILLED ✓
- Negated conditional: SURVIVED ✗
```

"SURVIVED" means your tests didn't detect that mutation. This reveals weaknesses in your test assertions.

### Mutation Testing Best Practices

**Do:**
- ✅ Run mutation testing on critical business logic
- ✅ Focus on algorithms and calculations
- ✅ Use as a code review tool
- ✅ Set mutation coverage goals (e.g., 80%)

**Don't:**
- ❌ Run on every build (too slow)
- ❌ Aim for 100% mutation coverage (diminishing returns)
- ❌ Run on generated code
- ❌ Run on simple getters/setters

**When to Run:**
- Before merging critical features
- During code review
- On CI for main branches
- Monthly as a quality audit

## Anti-Patterns to Avoid

While collecting quest items, avoid these common traps:

### Anti-Pattern #1: Over-Mocking

```java
// Bad: Mocking everything
@SpringBootTest
class OverMockedTest {
  @MockBean private BookRepository bookRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private EmailService emailService;
  @MockBean private NotificationService notificationService;
  @MockBean private AuditService auditService;
  // ... 5 more mocks
}
```

**Solution:** Mock only external boundaries (email, external APIs). Use real components for internal layers.

### Anti-Pattern #2: Testing Implementation Details

```java
// Bad: Testing HOW
@Test
void shouldUseSpecificSortAlgorithm() {
  BookService spy = spy(bookService);
  spy.processBooks();
  verify(spy).quickSort(); // Testing implementation
}

// Good: Testing WHAT
@Test
void shouldReturnBooksSortedByTitle() {
  List<Book> books = bookService.processBooks();
  assertThat(books).extracting(Book::getTitle).isSorted();
}
```

### Anti-Pattern #3: Slow Test Suites

```java
// Bad: Unnecessary waits
@Test
void shouldProcessAsync() {
  service.triggerAsyncOperation();
  Thread.sleep(5000); // Fixed delay
  assertTrue(repository.findProcessedItem().isPresent());
}

// Good: Smart waiting
@Test
void shouldProcessAsync() {
  service.triggerAsyncOperation();

  await().atMost(5, SECONDS)
    .until(() -> repository.findProcessedItem().isPresent());
}
```

## All Quest Items Collected

You now possess all three enhanced quest items:

**🧿 The Caching Amulet**
- Context caching mastery
- BaseIntegrationTest pattern
- Spring Test Profiler utilization
- From 26 minutes to 12 minutes

**⚡ The Lightning Shield (Enhanced)**
- JVM-level parallelization
- Thread-based parallelization
- Tag-based selective execution
- Optimal test organization

**🔍 The Scroll of Truth (Enhanced)**
- Mutation testing with PIT
- True quality verification
- Detecting weak assertions
- Critical logic validation

## The Power of Combined Artifacts

Used together, these quest items transform your testing practice:

```
Before Quest Items:
- Test suite: 26 minutes
- Runs locally: Rarely (too slow)
- Coverage: 80% (but weak assertions)
- CI feedback: Hours
- Developer confidence: Medium

After Quest Items:
- Test suite: 6 minutes (context caching + parallelization)
- Runs locally: Every commit
- Coverage: 80% with 75% mutation coverage
- CI feedback: Minutes
- Developer confidence: High
```

## The Exit Awaits

You've collected all quest items and defeated all bosses. The exit to the maze is visible ahead.

But before you leave, let's consolidate everything you've learned into a comprehensive testing strategy that you can apply immediately.

The final chapter awaits: **Exiting the Maze**.

**Next up: Chapter 5 - Exiting the Maze: Your Complete Testing Strategy**
