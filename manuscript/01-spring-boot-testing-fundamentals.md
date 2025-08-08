# Chapter 1: Testing Fundamentals in Spring Boot Projects

Let's build a strong foundation for testing in Spring Boot projects.

We'll start with understanding how our build tools execute tests, explore testing best practices, and master unit testing without any Spring context.

This chapter focuses on pure Java testing fundamentals that form the backbone of any well-tested Spring Boot application.

## Defining a Testing Strategy

### The Importance of a Testing Strategy

Before diving into the technical details of testing Spring Boot applications, it's crucial to establish a clear testing strategy. A well-defined strategy provides a roadmap for writing effective tests, ensuring that we cover all critical aspects of our application without wasting effort.

### The Testing Pyramid: A Foundational Model

A widely recognized model for structuring a testing strategy is the Testing Pyramid. It provides a simple, visual guide for allocating testing efforts across different levels of granularity.

The pyramid is divided into three main layers:

1.  Unit Tests (Base of the Pyramid):
    *   Scope: These form the largest part of your test suite. They test individual components (e.g., a single class or method) in isolation from the rest of the system.
    *   Characteristics: They are fast, reliable, and cheap to write and maintain. They don't involve network calls, database connections, or file system access.
    *   Spring Boot Alignment: These are typically plain JUnit/Mockito tests that do *not* load a Spring `ApplicationContext`.

2.  Integration Tests (Middle of the Pyramid):
    *   Scope: These tests verify that different components work together as expected. This can range from testing the interaction between a service and a database repository to testing a REST endpoint with its underlying logic.
    *   Characteristics: They are slower than unit tests because they often require external infrastructure (like a database or a message queue) and may load parts of the Spring context.
    *   Spring Boot Alignment: Spring Boot provides excellent support for integration tests. Test Slices (e.g., `@WebMvcTest`, `@DataJpaTest`) are perfect for this layer, as they load only a relevant portion of the application context. Full context tests with `@SpringBootTest` also fall into this category.

3.  End-to-End (E2E) Tests (Top of the Pyramid):
    *   Scope: These are the fewest in number. They validate the entire application flow from start to finish, simulating real user scenarios. For a web application, an E2E test might involve using a browser automation tool to click through a user journey.
    *   Characteristics: They are the slowest, most brittle, and most expensive tests to write and maintain.
    *   Spring Boot Alignment: While Spring Boot can be used to set up the application for E2E tests, these tests are often run against a fully deployed application using tools like Selenium, Cypress, or Playwright.

The core idea of the pyramid is to write many fast, simple unit tests at the base and progressively fewer, more complex tests as you move up the layers. This approach provides a fast feedback loop and a stable test suite.

### Our Practical Approach

While the Testing Pyramid is an excellent mental model, we won't follow it dogmatically. Our central goal is to gain confidence in deploying changes to production and receive feedback about those changes as quickly as possible.

The ideal mix of tests depends on your application. For example:
-   A complex algorithmic library might rely almost entirely on unit tests.
-   A standard CRUD-based microservice will benefit greatly from a solid suite of integration tests.

Throughout this book, we will use the following terms, which align well with the pyramid's layers:

1.  Unit Tests: Fast, isolated tests with no Spring context.
2.  Integration Tests: Tests that involve the Spring framework, from focused test slices to full `@SpringBootTest` contexts.
3.  End-to-End Tests: The broadest tests that validate the entire application stack, often from a user's perspective.

By understanding these categories, you can build a balanced and effective testing strategy for any Spring Boot application.

## Build Tool Configuration

When building our Spring Boot project, someone has to take care to select, run and interpret the result of our tests. We only want to deploy a code change if all our tests have passed.

With Java, that's the job of the build tool. Maven and Gradle are the two most popular build tools for Java projects.

Understanding how these tools handle tests is crucial for effective testing. While Spring Boot provides excellent defaults, knowing the underlying mechanisms helps us optimize our test execution and troubleshoot issues.

Let's explore how both Maven and Gradle manage our tests, starting with Maven.

### Maven Testing Configuration

To understand how Maven manages our tests effectively, let's first examine how Maven organizes test structure.

Maven follows a strict convention for organizing tests:

- Test classes: `src/test/java` - All test classes must be placed here
- Test resources: `src/test/resources` - Configuration files, test data, and other resources
- Production code: `src/main/java` - Your actual application code
- Production resources: `src/main/resources` - Application configuration files

This separation ensures a clear boundary between production and test code.

With the directory structure in place, we need to configure dependencies properly using Maven's scoping mechanism.

The `<scope>test</scope>` declaration is critical for keeping our production artifacts lean:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

This Maven dependency declaration adds Spring Boot's testing starter to your project. The `<scope>test</scope>` is crucial - it tells Maven this dependency is only needed during testing, not in production. This keeps your deployed application smaller by excluding testing libraries like Mockito, AssertJ, and JUnit from the final JAR file.

This scoping mechanism ensures test dependencies never reach production, keeping our deployable artifacts smaller and more secure.

Maven's test execution relies on a sophisticated two-plugin strategy that separates different types of tests.

Maven employs two separate plugins for different test types:

1. Maven Surefire Plugin - Executes unit tests during the `test` phase
2. Maven Failsafe Plugin - Executes integration tests during the `integration-test` and `verify` phases

Why Two Plugins?

This separation provides several benefits:

- Fast feedback: Unit tests run first, failing fast if basic functionality is broken
- Build optimization: Skip slow integration tests during development with `mvn test`
- Parallel execution: Configure different parallelization strategies for each test type
- Resource management: Integration tests can have different JVM settings and timeouts

For this separation to work correctly, Maven follows specific test naming conventions that determine which plugin executes which tests.

Surefire Plugin (Unit Tests):

- `**/Test*.java`
- `**/*Test.java` -> Recommended
- `**/*Tests.java`
- `**/*TestCase.java`

Failsafe Plugin (Integration Tests):

- `**/IT*.java`
- `**/*IT.java` -> Recommended
- `**/*ITCase.java`

Best Practice: Stick to one convention per test type:

- Unit tests: `BookServiceTest.java`
- Integration tests: `BookServiceIT.java`

This consistency makes it immediately clear what type of test you're looking at.

### Gradle Testing Configuration

Gradle follows many of the same conventions as Maven when it comes to test structure.

Gradle follows the same directory structure as Maven:

- `src/test/java` - Test classes
- `src/test/resources` - Test resources

Next, we need to properly configure test dependencies in Gradle.

```gradle
dependencies {
  testImplementation 'org.junit.jupiter:junit-jupiter'
  testImplementation 'org.mockito:mockito-core'
  testImplementation 'org.assertj:assertj-core'
}
```

The `testImplementation` configuration ensures dependencies are only available during testing.

To get the most out of Gradle's testing capabilities, we should configure test logging and create proper task separation.

Within our `build.gralde` file we can configure the logging events that Gradle should print to stdout when running the tests:

```gradle
test {
  testLogging {
    events "passed", "skipped", "failed"
    exceptionFormat "full"
    showStandardStreams = false
  }
}
```

Furthermore, with Gradle, we need to explicitly define a task to run the integration tests:

```groovy
tasks.register('integrationTest', Test) {
  description = 'Runs integration tests.'
  group = 'verification'

  useJUnitPlatform()
  include '**/*IT.class'

  testLogging {
    events "passed"
  }
}

tasks.named('check') {
  dependsOn integrationTest
}
```

In this configuration setup we follow the similar naming-scheme pattern of Maven where integration test classes have the postfix `*IT`.

As part of the [official Gradle documentation](https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests), we can also see an example of how one would configure Gradle to find integration test classes in a separate folder to split unit and integration tests stricter.

### Understanding Test Execution

Both Maven and Gradle delegate test execution to a test runner. With Spring Boot, this is JUnit 5's Jupiter engine by default.

Maven Lifecycle:

- `compile` → `test` (unit tests) → `package` → `verify` (integration tests)
- Running `mvn test` only executes unit tests
- Running `mvn verify` executes both unit and integration tests

Gradle Tasks:

- `gradle test` - Runs unit tests
- `gradle integrationTest` - Runs integration tests

## Unit Testing Fundamentals

Before diving into Spring-specific testing, let's master the fundamentals of unit testing in Java.

These principles apply to any Java project and form the foundation of a solid testing strategy.

### What Makes a Good Unit Test?

A unit test should be:

- Fast: Executes in milliseconds, not seconds
- Isolated: Tests a single unit of code without external dependencies
- Repeatable: Produces the same result every time
- Self-validating: Either passes or fails with no manual interpretation
- Timely: Written close to the production code

### The AAA Pattern

Every unit test should follow the Arrange-Act-Assert pattern:

```java
class CalculatorTest {
  @Test
  void shouldAddTwoNumbers() {
    // Arrange - Set up test data
    Calculator calculator = new Calculator();
    int a = 5;
    int b = 3;

    // Act - Execute the method under test
    int result = calculator.add(a, b);

    // Assert - Verify the result
    assertEquals(8, result);
  }
}
```

Alternatively, we can use the Given-When-Then pattern.

We'll use both patterns throughout this book, as it provides a clear structure for tests.

### Testing Without Spring Context

The best unit tests don't require any production framework (aka. Spring). They test pure Java code. Let's build a price calculator to demonstrate this principle.

First, we'll define our business constants:

```java
public class PriceCalculator {
  private static final double TAX_RATE = 0.08;
  private static final double DISCOUNT_THRESHOLD = 100.0;
  private static final double DISCOUNT_RATE = 0.10;
```

These constants define our business rules: 8% tax, 10% discount for purchases over $100.

Next, our main calculation method:

```java
public double calculateFinalPrice(double basePrice, int quantity) {
  if (basePrice <= 0 || quantity <= 0) {
    throw new IllegalArgumentException(
      "Price and quantity must be positive");
  }

  double subtotal = basePrice * quantity;
  double discountedPrice = applyDiscount(subtotal);
  return applyTax(discountedPrice);
}
```

This method validates inputs, calculates the subtotal, applies any discount, then adds tax. The order matters - we discount before taxing.

Our discount logic checks if the purchase qualifies:

```java
private double applyDiscount(double price) {
  if (price > DISCOUNT_THRESHOLD) {
    return price * (1 - DISCOUNT_RATE);
  }
  return price;
}
```

Only purchases over $100 get the 10% discount.

Finally, we apply tax to the discounted price:

```java
private double applyTax(double price) {
  return price * (1 + TAX_RATE);
}
```

Now let's write comprehensive tests. First, test the happy path without discount:

```java
class PriceCalculatorTest {
  private PriceCalculator calculator = new PriceCalculator();

  @Test
  void shouldCalculatePriceWithoutDiscount() {
    // Given a purchase under the discount threshold
    double basePrice = 20.0;
    int quantity = 4; // Total: $80

    // When calculating final price
    double finalPrice = calculator.calculateFinalPrice(
      basePrice, quantity);

    // Then tax is applied but no discount
    // Expected: 80 * 1.08 = 86.40
    assertEquals(86.40, finalPrice, 0.01);
  }
}
```

This test verifies that purchases under $100 don't receive a discount. The third parameter to `assertEquals` is the delta - we allow 1 cent difference for floating-point precision.

Next, test the discount scenario:

```java
@Test
void shouldApplyDiscountForLargePurchases() {
  // Given a purchase over the discount threshold
  double basePrice = 50.0;
  int quantity = 3; // Total: $150

  // When calculating final price
  double finalPrice = calculator.calculateFinalPrice(
    basePrice, quantity);

  // Then both discount and tax are applied
  // Expected: 150 * 0.9 * 1.08 = 145.80
  assertEquals(145.80, finalPrice, 0.01);
}
```

This verifies the correct order of operations: discount first, then tax.

Finally, test error handling:

```java
@Test
void shouldThrowExceptionForInvalidPrice() {
  // Negative price should throw exception
  assertThrows(IllegalArgumentException.class,
    () -> calculator.calculateFinalPrice(-10, 5));

  // Zero quantity should also throw exception
  assertThrows(IllegalArgumentException.class,
    () -> calculator.calculateFinalPrice(10, 0));
}
```

These tests run instantly because they don't depend on any external resources or frameworks.

## The Testing Toolkit

While Spring Boot provides the `spring-boot-starter-test` dependency, let's understand the core testing libraries it includes and how to use them effectively for unit testing.

### What's Inside spring-boot-starter-test?

The `spring-boot-starter-test` is a comprehensive testing starter that brings together the most essential testing libraries for Spring Boot applications.

Here's what it includes transitively:

Core Testing Framework:

- JUnit 5 (JUnit Jupiter): The modern testing framework for Java

Assertion Libraries:

- AssertJ: Fluent assertion library with rich, readable assertions
- Hamcrest: Matcher library for building complex test conditions

Mocking Frameworks:

- Mockito: The most popular mocking framework for Java

Spring Test Support:

- Spring Test: Core Spring testing features including TestContext framework
- Spring Boot Test: Auto-configuration support for tests

Additional Utilities:

- JSONAssert: For testing JSON responses
- JsonPath: XPath-like syntax for JSON
- XMLUnit: For XML testing (if needed)

This means when we add just one dependency:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

We get a complete testing toolkit without managing individual library versions. Spring Boot manages version compatibility, ensuring all these libraries work well together.

### JUnit 5: The Modern Testing Framework

JUnit 5 (Jupiter) is the backbone of Java testing. Let's explore its key improvements, starting with basic assertions that form the foundation of any test.

```java
class StringUtilsTest {
  @Test
  void testStringManipulation() {
    // No more public methods required in JUnit 5
    String input = "  Hello World  ";
    String result = StringUtils.trimAndUpperCase(input);

    // Multiple assertions
    assertNotNull(result);
    assertEquals("HELLO WORLD", result);
    assertTrue(result.startsWith("HELLO"));
    assertFalse(result.contains(" "));
  }

  @Test
  void testMultipleAssertions() {
    String text = "Spring Boot Testing";

    // Group assertions - all are executed even if one fails
    assertAll("text properties",
      () -> assertEquals(19, text.length()),
      () -> assertTrue(text.contains("Boot")),
      () -> assertTrue(text.startsWith("Spring")),
      () -> assertFalse(text.isEmpty())
    );
  }
}
```

Beyond basic assertions, JUnit 5 provides excellent support for testing exceptions, which is crucial for validating error handling.

```java
class ValidationServiceTest {
  private ValidationService validator = new ValidationService();

  @Test
  void shouldThrowExceptionForInvalidEmail() {
    // Simple exception assertion
    assertThrows(ValidationException.class,
      () -> validator.validateEmail("invalid-email"));
  }

  @Test
  void shouldValidateExceptionDetails() {
    // Capture exception for detailed assertions
    ValidationException exception = assertThrows(
      ValidationException.class,
      () -> validator.validateEmail("@example.com")
    );

    assertEquals("INVALID_EMAIL", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("missing local part"));
  }
}
```

For developers transitioning from JUnit 4, understanding JUnit Jupiter's extension model is crucial, as it replaces the older runner system with something much more powerful.

If you're coming from JUnit 4, you might be familiar with `@RunWith` and custom runners.

JUnit Jupiter (JUnit 5) replaces this concept with a more flexible and powerful extension model. Instead of being limited to a single runner, you can compose multiple extensions together.

In JUnit 4, you were limited to one runner:

```java
@RunWith(MockitoJUnitRunner.class)
public class MyTest {
  // Can't add Spring runner here too!
}
```

In JUnit 5, you can combine multiple extensions:

```java
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class MyTest {
  // Both Mockito and Spring features available
}
```

This composability is crucial for real-world testing where you often need features from multiple frameworks.

To understand how frameworks integrate with JUnit 5, we need to explore the various extension points available to developers.

The extension model provides several callback interfaces that frameworks can implement:

Test Lifecycle Callbacks:

- `BeforeAllCallback` - Runs before all tests in a class
- `BeforeEachCallback` - Runs before each test method
- `BeforeTestExecutionCallback` - Runs immediately before test execution
- `AfterTestExecutionCallback` - Runs immediately after test execution
- `AfterEachCallback` - Runs after each test method
- `AfterAllCallback` - Runs after all tests in a class

Parameter Resolution:

- `ParameterResolver` - Provides parameters to test methods and constructors

Test Execution:

- `ExecutionCondition` - Controls whether tests should run
- `TestInstancePostProcessor` - Processes test instances after creation
- `TestInstancePreDestroyCallback` - Cleanup before test instance destruction

Exception Handling:

- `TestExecutionExceptionHandler` - Handles exceptions thrown during test execution
- `LifecycleMethodExecutionExceptionHandler` - Handles exceptions in lifecycle methods

Let's see how popular frameworks use these extension points:

Mockito Extension Example:

```java
public class MockitoExtension implements
    BeforeEachCallback, AfterEachCallback, ParameterResolver {

  @Override
  public void beforeEach(ExtensionContext context) {
    // Initialize @Mock fields
    MockitoAnnotations.openMocks(context.getTestInstance());
  }
}
```

The Mockito extension uses `BeforeEachCallback` to initialize mocks before each test and `ParameterResolver` to inject mocks as method parameters.

Spring Extension Example:

```java
public class SpringExtension implements
    BeforeAllCallback, TestInstancePostProcessor {

  @Override
  public void postProcessTestInstance(Object testInstance,
      ExtensionContext context) {
    // Inject Spring beans into test instance
    ApplicationContext ctx = getApplicationContext(context);
    ctx.getAutowireCapableBeanFactory().autowireBean(testInstance);
  }
}
```

Spring uses `TestInstancePostProcessor` to perform dependency injection after JUnit creates the test instance.

Creating Your Own Extension:

Here's a practical example - a timing extension that warns about slow tests:

```java
public class TimingExtension implements
    BeforeTestExecutionCallback, AfterTestExecutionCallback {

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    context.getStore(NAMESPACE).put("start", System.currentTimeMillis());
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    long start = context.getStore(NAMESPACE).remove("start", Long.class);
    long duration = System.currentTimeMillis() - start;

    if (duration > 1000) {
      System.out.printf("Slow test: %s took %d ms%n",
        context.getDisplayName(), duration);
    }
  }
}
```

Use it with `@ExtendWith`:

```java
@ExtendWith(TimingExtension.class)
class PerformanceTests {
  @Test
  void potentiallySlowTest() {
    // Test code
  }
}
```

Extension Store for State Management:

Extensions can store state using the ExtensionContext's Store:

```java
Store store = context.getStore(
  ExtensionContext.Namespace.create(getClass(), context.getMethod())
);
store.put("key", value);
Object value = store.get("key");
```

This store is namespaced and hierarchical, allowing extensions to maintain state without conflicts.

Common Framework Extensions:

- MockitoExtension: Initializes mocks and injects them
- SpringExtension: Manages Spring context and dependency injection
- TempDirectory: Provides temporary directories for file testing
- TestContainersExtension: Manages Docker containers for integration tests
- WireMockExtension: Sets up mock HTTP servers

The extension model is what makes JUnit 5 so powerful for integration with modern frameworks and tools.

One of JUnit 5's most valuable features for reducing test duplication is parameterized tests, which allow us to run the same test logic with different inputs.

Parameterized tests let us run the same test logic with different inputs. This is one of JUnit 5's most powerful features for reducing test duplication.

First, let's see a simple example with `@ValueSource`:

```java
class EmailValidatorTest {
  private EmailValidator validator = new EmailValidator();

  @ParameterizedTest
  @ValueSource(strings = {
    "user@example.com",
    "john.doe@company.org",
    "admin+tag@domain.co.uk"
  })
  void shouldAcceptValidEmails(String email) {
    assertTrue(validator.isValid(email));
  }
}
```

This single test method runs three times, once for each email address. JUnit automatically injects each value as the `email` parameter.

We can also test invalid cases:

```java
@ParameterizedTest
@ValueSource(strings = {
  "invalid.email",
  "@no-local-part.com",
  "no-at-sign.com",
  "multiple@@at.com"
})
void shouldRejectInvalidEmails(String email) {
  assertFalse(validator.isValid(email));
}
```

For more complex scenarios, `@CsvSource` lets us provide multiple parameters:

```java
@ParameterizedTest
@CsvSource({
  "user@example.com, true",
  "invalid.email, false",
  "admin@company.org, true",
  "@missing.com, false"
})
void shouldValidateEmailsWithExpectedResults(
    String email, boolean expected) {
  assertEquals(expected, validator.isValid(email));
}
```

Each line in `@CsvSource` provides values for both parameters. This is much cleaner than writing four separate test methods.

To manage test setup and cleanup effectively, JUnit 5 provides several lifecycle annotations that control when initialization and cleanup code runs.

JUnit 5 provides lifecycle annotations that control when setup and teardown code runs. If you're coming from JUnit 4, here's the mapping:

- `@BeforeClass` -> `@BeforeAll`
- `@Before` -> `@BeforeEach`
- `@After` -> `@AfterEach`
- `@AfterClass` -> `@AfterAll`

Let's see how to use them effectively. First, we'll set up a resource that's expensive to create:

```java
class DatabaseConnectionTest {
  private static DatabasePool pool;
  private Connection connection;

  @BeforeAll
  static void initializePool() {
    // Runs once before all tests
    pool = new DatabasePool(5);
  }
}
```

The `@BeforeAll` method must be static because it runs before any test instance is created. Use this for expensive one-time setup.

Next, we'll get a connection before each test:

```java
@BeforeEach
void getConnection() {
  // Runs before each test
  connection = pool.getConnection();
}
```

`@BeforeEach` runs before every test method, ensuring each test has a fresh connection. This maintains test isolation.

Now our actual test can use the connection:

```java
@Test
void testDatabaseOperation() {
  // Use the connection
  assertNotNull(connection);
  assertTrue(connection.isValid());
}
```

After each test, we release the connection:

```java
@AfterEach
void releaseConnection() {
  // Runs after each test
  if (connection != null) {
    pool.release(connection);
  }
}
```

This cleanup runs even if the test fails, preventing resource leaks.

Finally, we shut down the pool after all tests:

```java
@AfterAll
static void closePool() {
  // Runs once after all tests
  if (pool != null) {
    pool.shutdown();
  }
}
```

Like `@BeforeAll`, this must be static and runs just once after all tests complete.

When testing complex classes with multiple behaviors, nested tests provide an excellent way to organize related test cases together.

Nested tests help organize related test cases together. This is particularly useful when testing different aspects of the same class.

First, let's create our outer test class:

```java
class StringCalculatorTest {
  private StringCalculator calculator = new StringCalculator();

  @Nested
  @DisplayName("When calculating sum of numbers")
  class SumCalculation {
    // Tests for valid calculations go here
  }
}
```

The `@Nested` annotation creates an inner test class. The `@DisplayName` provides a human-readable description that appears in test reports.

Inside our nested class, we can group related tests:

```java
@Test
@DisplayName("should return 0 for empty string")
void emptyStringReturnsZero() {
  assertEquals(0, calculator.add(""));
}

@Test
@DisplayName("should return number for single value")
void singleNumberReturnsSameValue() {
  assertEquals(5, calculator.add("5"));
}
```

Each test in the nested class has access to the outer class's fields. This reduces duplication while keeping tests organized.

We can add more tests to the same nested class:

```java
@Test
@DisplayName("should sum comma-separated numbers")
void sumsCommaSeparatedNumbers() {
  assertEquals(6, calculator.add("1,2,3"));
}
```

For error cases, we create a separate nested class:

```java
@Nested
@DisplayName("When handling invalid input")
class ErrorHandling {

  @Test
  @DisplayName("should throw exception for negative numbers")
  void throwsExceptionForNegativeNumbers() {
    Exception exception = assertThrows(
      IllegalArgumentException.class,
      () -> calculator.add("1,-2,3")
    );
    assertTrue(exception.getMessage()
      .contains("Negatives not allowed"));
  }
}
```

This organization makes test reports much clearer. Instead of a flat list of test methods, you see a hierarchy that reflects the behavior being tested.

### AssertJ: Fluent Assertions for Cleaner Tests

AssertJ provides more readable assertions than JUnit's built-in ones:

```java
class ProductServiceTest {
  private ProductService service = new ProductService();

  @Test
  void testBasicAssertions() {
    Product product = service.createProduct("Laptop", 999.99);

    // JUnit assertion
    assertEquals("Laptop", product.getName());

    // AssertJ - more fluent and readable
    assertThat(product.getName()).isEqualTo("Laptop");
    assertThat(product.getPrice()).isEqualTo(999.99);
    assertThat(product.getId()).isNotNull();
    assertThat(product.isActive()).isTrue();
  }
}
```

AssertJ really shines when working with complex data types. String assertions, for example, offer rich validation capabilities that make tests more expressive.

AssertJ provides rich string assertions that make tests more expressive. Let's start with basic checks:

```java
@Test
void testStringAssertions() {
  String result = "Spring Boot Testing Guide";

  assertThat(result)
    .isNotNull()
    .isNotEmpty()
    .hasSize(25);
}
```

These assertions verify the string exists, has content, and has the expected length. The method chaining makes multiple assertions readable.

We can also check string contents:

```java
assertThat(result)
  .startsWith("Spring")
  .endsWith("Guide")
  .contains("Boot", "Testing")
  .doesNotContain("JUnit");
```

The `contains` method accepts multiple values and checks that all are present. This is cleaner than multiple separate assertions.

For pattern matching and case-insensitive comparisons:

```java
assertThat(result)
  .matches(".*Boot.*")
  .isEqualToIgnoringCase("spring boot testing guide");
```

The `matches` method accepts regular expressions, while `isEqualToIgnoringCase` is perfect for user input validation.

When working with collections, AssertJ provides extensive assertion capabilities that go far beyond simple size checks.

AssertJ excels at collection assertions. Let's start with a simple list of numbers:

```java
@Test
void testCollectionAssertions() {
  List<Integer> numbers = List.of(1, 2, 3, 4, 5);

  assertThat(numbers)
    .hasSize(5)
    .contains(1, 3, 5)
    .containsExactly(1, 2, 3, 4, 5);
}
```

The difference between `contains` and `containsExactly` is important: `contains` checks that specified elements are present (in any order), while `containsExactly` verifies all elements in the exact order.

We can check for sequences and exclusions:

```java
assertThat(numbers)
  .containsSequence(2, 3, 4)
  .doesNotContain(0, 6);
```

`containsSequence` verifies that elements appear consecutively in the collection.

Predicate-based assertions are powerful for complex conditions:

```java
assertThat(numbers)
  .allMatch(n -> n > 0)
  .anyMatch(n -> n % 2 == 0)
  .noneMatch(n -> n > 10);
```

These read like English: all numbers are positive, at least one is even, and none exceed 10.

For collections of objects, `extracting` is invaluable:

```java
@Test
void testObjectCollectionAssertions() {
  List<Person> team = List.of(
    new Person("Alice", 30),
    new Person("Bob", 25),
    new Person("Charlie", 35)
  );

  assertThat(team)
    .extracting(Person::getName)
    .containsExactlyInAnyOrder("Bob", "Alice", "Charlie");
}
```

This extracts just the names for assertion, making the test more focused and readable.

We can extract multiple properties at once:

```java
assertThat(team)
  .extracting(Person::getName, Person::getAge)
  .containsExactly(
    tuple("Alice", 30),
    tuple("Bob", 25),
    tuple("Charlie", 35)
  );
```

The `tuple` method groups multiple values for comparison. This is cleaner than writing separate assertions for each property.

For testing map-based data structures, AssertJ provides specialized assertions that validate both keys and values efficiently.

```java
@Test
void testMapAssertions() {
  Map<String, Integer> inventory = Map.of(
    "apples", 10,
    "bananas", 5,
    "oranges", 8
  );

  assertThat(inventory)
    .hasSize(3)
    .containsKey("apples")
    .containsKeys("bananas", "oranges")
    .doesNotContainKey("grapes")
    .containsEntry("apples", 10)
    .containsValue(5);
}
```

When built-in assertions aren't sufficient for complex objects, AssertJ allows us to create custom validation logic using the `satisfies` method.

```java
@Test
void testCustomAssertions() {
  // Using satisfies for complex assertions
  Order order = new Order("ORD-123", 150.00);
  order.addItem("Laptop", 1, 100.00);
  order.addItem("Mouse", 2, 25.00);

  assertThat(order)
    .satisfies(o -> {
      assertThat(o.getId()).startsWith("ORD-");
      assertThat(o.getTotalAmount()).isEqualTo(150.00);
      assertThat(o.getItems()).hasSize(2);
      assertThat(o.getStatus()).isEqualTo(OrderStatus.PENDING);
    });
}
```

### Mockito: Isolating Units for True Unit Tests

Mockito is essential for creating test doubles to isolate the unit under test. Let's explore how to use it effectively, starting with basic mocking techniques that form the foundation of isolated unit testing.

First, let's create mocks manually:

```java
class OrderServiceTest {

  @Test
  void testWithManualMocks() {
    // Create mock objects
    PaymentGateway paymentGateway = mock(PaymentGateway.class);
    EmailService emailService = mock(EmailService.class);
```

The `mock()` method creates a fake implementation that we can control. These mocks have no real behavior until we define it.

Next, we define how our mocks should behave:

```java
// Define behavior
when(paymentGateway.processPayment(100.0, "VISA"))
  .thenReturn(new PaymentResult(true, "TXN-123"));
```

This tells the mock: "when processPayment is called with these specific arguments, return this result".

Now we can test our service:

```java
// Create service under test
OrderService orderService = new OrderService(
  paymentGateway, emailService);

// Execute test
Order order = orderService.placeOrder(100.0, "VISA");
```

The service uses our mocked dependencies, completely isolated from real implementations.

After execution, we verify the mocks were used correctly:

```java
// Verify interactions
verify(paymentGateway).processPayment(100.0, "VISA");
verify(emailService).sendOrderConfirmation(any());
```

The `verify()` method checks that methods were called with expected arguments. The `any()` matcher accepts any argument of the correct type.

Finally, we assert the result:

```java
// Assert result
assertThat(order.getTransactionId()).isEqualTo("TXN-123");
assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
```

While manual mock creation works well, Mockito annotations provide a cleaner approach that reduces boilerplate code.

Mockito annotations make tests cleaner. First, enable Mockito with the extension:

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceAnnotationTest {

  @Mock
  private PaymentGateway paymentGateway;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private OrderService orderService;
```

The `@Mock` annotation creates mocks, while `@InjectMocks` creates the service and automatically injects the mocks into it. This eliminates boilerplate code.

For our success scenario, we use argument matchers:

```java
@Test
void shouldProcessOrderSuccessfully() {
  // Given
  when(paymentGateway.processPayment(anyDouble(), anyString()))
    .thenReturn(new PaymentResult(true, "TXN-456"));
}
```

The `anyDouble()` and `anyString()` matchers accept any value of their type. This makes tests less brittle when exact values don't matter.

Execute and verify:

```java
// When
Order order = orderService.placeOrder(50.0, "MASTERCARD");

// Then
assertThat(order.isConfirmed()).isTrue();
verify(emailService).sendOrderConfirmation(order);
```

For failure scenarios, we can test exception handling:

```java
@Test
void shouldHandlePaymentFailure() {
  // Given - payment fails
  when(paymentGateway.processPayment(anyDouble(), anyString()))
    .thenReturn(new PaymentResult(false, null));
}
```

We expect an exception when payment fails:

```java
// When & Then
assertThrows(PaymentFailedException.class,
  () -> orderService.placeOrder(100.0, "VISA"));
```

And verify that no confirmation email was sent:

```java
verify(emailService, never()).sendOrderConfirmation(any());
```

The `never()` verification ensures a method wasn't called - crucial for testing error paths.

Beyond basic mocking, Mockito offers advanced techniques for handling complex testing scenarios.

Stubbing Consecutive Calls

Sometimes a method returns different values on successive calls. Mockito handles this elegantly:

```java
@Test
void testConsecutiveCalls() {
  when(random.nextInt(100))
    .thenReturn(42)      // First call
    .thenReturn(17)      // Second call
    .thenReturn(99);     // Third call
}
```

Each call returns the next value in sequence:

```java
assertEquals(42, random.nextInt(100));
assertEquals(17, random.nextInt(100));
assertEquals(99, random.nextInt(100));
```

This is useful for testing retry logic or state-dependent behavior.

Throwing Exceptions

To test error handling, mocks can throw exceptions:

```java
@Test
void testExceptionThrowing() {
  when(userRepository.findById(999L))
    .thenThrow(new UserNotFoundException("User not found"));
}
```

Now when the service calls this method, it receives an exception:

```java
assertThrows(UserNotFoundException.class,
  () -> userService.getUser(999L));
```

This verifies your service properly handles repository exceptions.

Argument Captors

Sometimes you need to inspect complex objects passed to mocks. First, create a captor:

```java
@Test
void testArgumentCapture() {
  ArgumentCaptor<Email> emailCaptor =
    ArgumentCaptor.forClass(Email.class);
}
```

Execute the code that should trigger the mock:

```java
// Execute the method
userService.registerUser("john@example.com", "John Doe");

// Capture the argument
verify(emailService).sendEmail(emailCaptor.capture());
```

The `capture()` method grabs the argument passed to `sendEmail()`. Now inspect it:

```java
// Verify the captured email
Email sentEmail = emailCaptor.getValue();
assertThat(sentEmail.getTo()).isEqualTo("john@example.com");
assertThat(sentEmail.getSubject()).contains("Welcome");
assertThat(sentEmail.getBody()).contains("John Doe");
```

This ensures the email was constructed correctly with user data.

Using Spies for Partial Mocking

Spies wrap real objects, allowing you to override specific methods while keeping others real:

```java
@Test
void testWithSpy() {
  // Create a real object
  List<String> list = new ArrayList<>();
  List<String> spyList = spy(list);
}
```

Real methods work normally:

```java
// Use real method
spyList.add("one");
spyList.add("two");
```

But you can stub specific methods:

```java
// Stub specific method
when(spyList.size()).thenReturn(100);
```

Now `get()` uses the real implementation, but `size()` returns our stubbed value:

```java
// Real method called
assertEquals("one", spyList.get(0));

// Stubbed method called
assertEquals(100, spyList.size());
```

Answer for Dynamic Stubbing

For complex stubbing logic, use `Answer` to calculate returns dynamically:

```java
@Test
void testWithAnswer() {
  when(calculator.add(anyInt(), anyInt()))
    .thenAnswer(invocation -> {
      int a = invocation.getArgument(0);
      int b = invocation.getArgument(1);
      return a + b;
    });
}
```

The lambda receives the method invocation and can access all arguments. This mock now behaves like a real calculator:

```java
assertEquals(7, calculator.add(3, 4));
assertEquals(15, calculator.add(10, 5));
```

This is useful when return values depend on input parameters.

## Writing Effective Unit Tests

Let's apply everything we've learned to write comprehensive unit tests for a realistic service:

### Example: Shopping Cart Service

Let's build a realistic shopping cart to demonstrate comprehensive unit testing. First, our domain model:

```java
import org.springframework.stereotype.Service;

@Service
public class ShoppingCart {
  private final Map<String, CartItem> items = new HashMap<>();
  private final PricingService pricingService;

  public ShoppingCart(PricingService pricingService) {
    this.pricingService = pricingService;
  }
}
```

The cart depends on a `PricingService` for product prices. This dependency will be mocked in tests.

Our add item method handles quantity validation:

```java
  public void addItem(String productId, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException(
        "Quantity must be positive");
    }

    items.merge(productId,
      new CartItem(productId, quantity),
      (existing, newItem) ->
        new CartItem(productId, existing.quantity + quantity)
    );
  }
```

The `merge` method elegantly handles both new items and quantity updates. If the product exists, quantities are combined.

Removal is straightforward:

```java
public void removeItem(String productId) {
  items.remove(productId);
}
```

Calculating the total requires the pricing service:

```java
public double calculateTotal() {
  return items.values().stream()
    .mapToDouble(item -> {
      double price = pricingService.getPrice(item.productId);
      return price * item.quantity;
    })
    .sum();
}
```

This is where mocking becomes essential - we don't want real pricing lookups in unit tests.

A helper method counts all items:

```java
public int getItemCount() {
  return items.values().stream()
    .mapToInt(item -> item.quantity)
    .sum();
}

private record CartItem(String productId, int quantity) {}
```

Using a record for `CartItem` keeps our code concise.

Now let's write comprehensive tests. First, set up the test class:

```java
@ExtendWith(MockitoExtension.class)
class ShoppingCartTest {

  @Mock
  private PricingService pricingService;

  @InjectMocks
  private ShoppingCart cart;
```

We mock the `PricingService` and inject the mock into our `ShoppingCart` instance. This allows us to isolate the cart's logic from external dependencies.

Now let's test adding items using nested classes for organization:

```java
@Nested
@DisplayName("Adding items to cart")
class AddingItems {

  @Test
  @DisplayName("should add single item successfully")
  void addSingleItem() {
    // When
    cart.addItem("PROD-001", 2);

    // Then
    assertThat(cart.getItemCount()).isEqualTo(2);
  }
}
```

This simple test verifies basic functionality. No mocking needed since we're not calculating prices.

Test that quantities accumulate correctly:

```java
@Test
@DisplayName("should accumulate quantities for same product")
void accumulateQuantities() {
  // When
  cart.addItem("PROD-001", 2);
  cart.addItem("PROD-001", 3);

  // Then
  assertThat(cart.getItemCount()).isEqualTo(5);
}
```

This verifies our `merge` logic combines quantities for the same product.

Test error handling with a simple case:

```java
@Test
@DisplayName("should reject negative quantities")
void rejectNegativeQuantity() {
  // When & Then
  assertThrows(IllegalArgumentException.class,
    () -> cart.addItem("PROD-001", -1));
}
```

Use parameterized tests for multiple invalid inputs:

```java
@ParameterizedTest
@ValueSource(ints = {0, -1, -10})
@DisplayName("should reject non-positive quantities")
void rejectInvalidQuantities(int quantity) {
  assertThrows(IllegalArgumentException.class,
    () -> cart.addItem("PROD-001", quantity));
}
```

Now test price calculations where mocking becomes essential:

```java
@Nested
@DisplayName("Calculating totals")
class CalculatingTotals {

  @Test
  @DisplayName("should calculate total for single item")
  void calculateSingleItemTotal() {
    // Given
    when(pricingService.getPrice("PROD-001"))
      .thenReturn(10.0);
  }
}
```

We stub the pricing service to return a known price. This isolates our test from external dependencies.

```java
// When
cart.addItem("PROD-001", 3);
double total = cart.calculateTotal();

// Then
assertThat(total).isEqualTo(30.0);
verify(pricingService).getPrice("PROD-001");
```

The verification ensures our service was called correctly. The math is simple: 3 items × $10 = $30.

For multiple items, we need multiple stubs:

```java
@Test
@DisplayName("should calculate total for multiple items")
void calculateMultipleItemsTotal() {
  // Given
  when(pricingService.getPrice("PROD-001"))
    .thenReturn(10.0);
  when(pricingService.getPrice("PROD-002"))
    .thenReturn(25.0);
}
```

Each product needs its own price stub.

```java
// When
cart.addItem("PROD-001", 2);
cart.addItem("PROD-002", 1);
double total = cart.calculateTotal();

// Then
assertThat(total).isEqualTo(45.0);
```

The calculation: `(2 × $10) + (1 × $25) = $45`.

Test the edge case of an empty cart:

```java
@Test
@DisplayName("should return zero for empty cart")
void emptyCartTotal() {
  // When
  double total = cart.calculateTotal();

  // Then
  assertThat(total).isEqualTo(0.0);
  verifyNoInteractions(pricingService);
}
```

`verifyNoInteractions` ensures the pricing service isn't called for an empty cart - an important optimization check.
Finally, test item removal at the class level:

```java
@Test
@DisplayName("should remove items from cart")
void removeItems() {
  // Given
  cart.addItem("PROD-001", 5);
  cart.addItem("PROD-002", 3);
```

First, we add two products with different quantities.

```java
// When
cart.removeItem("PROD-001");

// Then
assertThat(cart.getItemCount()).isEqualTo(3);
```

After removing the first product (5 items), only the second product's 3 items remain. This verifies complete removal, not just quantity reduction.

## Best Practices for Unit Testing

### 1. Test Naming Conventions

Use descriptive names that explain what the test does:

```java
// Bad
@Test
void test1() { }

// Good
@Test
void shouldThrowExceptionWhenQuantityIsNegative() { }

// Better - using DisplayName
@Test
@DisplayName("should throw IllegalArgumentException when quantity is negative")
void negativeQuantityValidation() { }
```

### 2. Keep Tests Independent

Tests should not depend on each other:

```java
// Bad - depends on test execution order
class BadTestExample {
  private static List<String> sharedList = new ArrayList<>();

  @Test
  void firstTest() {
    sharedList.add("item");
  }

  @Test
  void secondTest() {
    // This fails if run before firstTest
    assertEquals(1, sharedList.size());
  }
}

// Good - each test is independent
class GoodTestExample {
  private List<String> list;

  @BeforeEach
  void setUp() {
    list = new ArrayList<>();
  }

  @Test
  void shouldAddItemToList() {
    list.add("item");
    assertEquals(1, list.size());
  }
}
```

### 3. Test One Thing at a Time

Each test should verify a single behavior. Here's what not to do:

```java
// Bad - testing multiple behaviors
@Test
void testUserService() {
  User user = userService.createUser("John", "john@example.com");
  assertNotNull(user.getId());
  assertEquals("John", user.getName());

  userService.updateEmail(user.getId(), "newemail@example.com");
  assertEquals("newemail@example.com", user.getEmail());

  userService.deleteUser(user.getId());
  assertNull(userService.findById(user.getId()));
}
```

This test does too much: creates, updates, and deletes a user. If it fails, which operation caused the problem?

Instead, write focused tests:

```java
// Good - test user creation
@Test
void shouldCreateUserWithValidData() {
  User user = userService.createUser("John", "john@example.com");
  assertNotNull(user.getId());
  assertEquals("John", user.getName());
}
```

This tests only user creation. Clear and focused.

```java
// Good - test email update separately
@Test
void shouldUpdateUserEmail() {
  User user = userService.createUser("John", "john@example.com");
  userService.updateEmail(user.getId(), "newemail@example.com");
  User updated = userService.findById(user.getId());
  assertEquals("newemail@example.com", updated.getEmail());
}
```

Each test has a single reason to fail, making debugging much easier.

### 4. Use Test Data Builders

Test data builders reduce duplication and make tests more readable. First, create the builder:

```java
class UserTestDataBuilder {
  private String name = "Default Name";
  private String email = "default@example.com";
  private int age = 25;
```

Provide sensible defaults so tests only specify what matters to them.

Add fluent methods for each property:

```java
public UserTestDataBuilder withName(String name) {
  this.name = name;
  return this;
}

public UserTestDataBuilder withEmail(String email) {
  this.email = email;
  return this;
}

public UserTestDataBuilder withAge(int age) {
  this.age = age;
  return this;
}
```

Each method returns `this` for chaining.

The build method creates the actual object:

```java
public User build() {
  return new User(name, email, age);
}
```

Now tests can create users easily:

```java
// Usage in tests
@Test
void testWithBuilder() {
  User youngUser = new UserTestDataBuilder()
    .withAge(18)
    .build();
}
```

Only age is specified; other fields use defaults.

```java
User seniorUser = new UserTestDataBuilder()
  .withAge(65)
  .withName("Senior User")
  .build();
```

This approach makes tests concise and highlights what's important for each test case.

### 5. Don't Test Framework Code

Avoid testing trivial code that's unlikely to break:

```java
// Bad - testing getter/setter
@Test
void testGetterSetter() {
  User user = new User();
  user.setName("John");
  assertEquals("John", user.getName());
}
```

This tests Java's basic functionality, not your logic. It adds no value.

Instead, test business logic:

```java
// Good - test business logic
@Test
void shouldCapitalizeUserName() {
  User user = new User("john doe");
  user.formatName();
  assertEquals("John Doe", user.getName());
}
```

This tests your `formatName()` method's capitalization logic - actual business value that could break if implemented incorrectly.

## Summary

In this chapter, we've covered the fundamentals of testing in Spring Boot projects:

Build Tool Configuration:

- Maven uses Surefire for unit tests and Failsafe for integration tests
- Gradle provides similar separation with custom test tasks
- Following naming conventions (`*Test.java` and `*IT.java`) ensures proper test detection

Testing Toolkit:

- JUnit 5 provides the foundation with improved assertions and lifecycle management
- AssertJ offers fluent, readable assertions for all data types
- Mockito enables true unit testing by isolating dependencies
- All these tools work together seamlessly in Spring Boot projects

Unit Testing Best Practices:

- Write fast, isolated, repeatable tests
- Follow the AAA (Arrange-Act-Assert) or Given/When/Then pattern
- Test one behavior per test method
- Use descriptive test names
- Keep tests independent of each other

These fundamentals apply to all testing in Spring Boot, whether you're writing pure unit tests or integration tests.

In the next chapter, we'll explore Spring Boot's powerful test slicing annotations that let us test specific layers of our application in isolation.

Remember: the best tests are those that give us confidence in our code without being brittle or slow.

Start with pure unit tests wherever possible, and only add complexity when truly needed.

