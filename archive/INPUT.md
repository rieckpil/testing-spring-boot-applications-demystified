---
marp: true
theme: pragmatech
---

![bg](./assets/barcelona-spring-io.jpg)

---

<!--

Question: Who is writing tests manually and who is enjoying it?

-->

![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Full-Day Workshop

_Spring I/O Conference Workshop 21.05.2025_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://twitter.com/rieckpil)


---

<!--

- Welcome, Introduction
- Overview of the workshop
- When to eat, toilet, WLAN

-->

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Spring I/O 21.05.2025' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

# Organization

- Hotel WiFi: `Spring I/O` Password: `bootifulBCN`
- Slides & Code will be shared: check PragmaTech GitHub
- Workshop lab requirements
  - Java 21
  - Docker
  - GitHub Account (preferably a personal)

---

### (Planned) Timeline

- 9:00 - 10:45: **Lab 1 - Introduction and Spring Boot Testing Basics**  (105 minutes)
- 10:45 - 11:05: **Coffee Break** (20 minutes)
- 11:05 - 13:00: **Lab 2 - Sliced Testing** (115 minutes)
- 13:00 - 14:00 **Lunch** (60 minutes)
- 14:00 - 15:30: **Lab 3 - Integration Testing** (90 minutes)
- 15:30 - 15:50 **Coffee Break** (20 minutes)
- 15:50 - 17:00: **Lab 4 - Pitfalls, Best Practices, AI & Outlook** (70 minutes)

---
![bg right:33%](assets/generated/hza.jpg)

## Workshop Instructor: Philip

- Self-employed IT consultant from Herzogenaurach, Germany (Bavaria) 🍻
- Blogging & content creation for more than five years. Since three years with a focus on testing Java and specifically Spring Boot applications 🍃
- Founder of PragmaTech GmbH - Enabling Developers to Frequently Deliver Software with More Confidence 🚤
- Enjoys writing tests 🧪
- @rieckpil on various platforms

---

## Getting to Know Each Other

- What's your name?
- Where are you from?
- What's your role?
- How is automated testing handled in your team/company?
- Do you have any specific testing challenges?
- What's your expectation for this workshop?

---

![bg left:33%](assets/generated/lab-1.jpg)

# Lab 1

## Introduction and Spring Boot Testing Basics

---
<!-- paginate: true -->

<!-- header: '' -->
<!-- footer: '' -->
<!--


Notes:

- Raise hands if you enjoy writing tests -> I do and hope I can change that for some of you today
- Why do we Test Code?
- Why is testing an afterthought?
- How to make testing more joyful?


-->

![bg left](assets/generated/starting.jpg)

# Getting Started with Testing

## How It Started

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Spring I/O 21.05.2025' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

<!--
- My story towards testing
- What I mean with testing: automated tests written by a developer
- Shifting left
- How much confidence do I have to deploy on a friday afternoon to prod on a dependabot update?
- Don't work towards 100% code coverage
- Fast feedback loops
- Quicker development, errors pop up more early, wouldn't say bugs, but sometimes we are overconfident only to realize after the deployment we missed a parameter or misspelled it. Avoid friction


-->

### Getting Used To Testing At Work

![](assets/generated/pr-reject.png)

---

# Goals of this Workshop

- Demystify the complexities of testing Spring Boot applications
- Provide clear explanations, practical insights, and actionable best practices
- Become more productive and confident in your development and testing efforts

---

<!-- _class: section -->

# Spring Boot Testing Basics
## Spring Boot Starter Test, Build Tools, Conventions, Unit Testing

---

## Why Do We Test Software

- Shift Left
- Catch Bugs Early
- Confidence in Code Changes
- Documentation
- Regression Prevention
- Become more Productive

---

<style>
img[alt~="center"] {
  display: block;
  margin: 0 auto;
}
</style>

### Naming Things Is Hard

![h:700 center](assets/cloud.svg)

---

### My Pragmatic Test Name Approach

1. **Unit Tests**: Tests that verify the functionality of a single, isolated component (like a method or class) by mocking or stubbing all external dependencies.
2. **Integration Tests**: Tests that verify interactions between two or more components work correctly together, with real implementations replacing some mocks.
3. **E2E**: Tests that validate the entire application workflow from start to finish, simulating real user scenarios across all components and external dependencies.

---

## Maven Build Lifecycle

![bg h:500 right:33%](assets/lifecycle.svg)

- **Maven Surfire Plugin** for unit tests: default postfix  `*Test` (e.g. `CustomerTest`)
- **Maven Failsafe Plugin** for integration tests: default postfix `*IT` (e.g. `CheckoutIT`)
- Reason for splitting: different **parallelization** options, better **organisation**

---

### Spring Boot Starter Test

<!--

Notes:

- Show the `spring-boot-starter-test` dependency and Maven dependency tree
- Show manual overriden


-->

![bg right:33%](assets/generated/swiss.jpg)

- aka. "Testing Swiss Army Knife"
- Batteries-included for testing
- Dependency management for:
  - JUnit Jupiter
  - Mockito
  - AssertJ
  - Awaitility
  - etc.
- We can manually override the dependency versions

---

<!--
Notes:
- Go to IDE to show the start
- Navigate to the parent pom to see the management
- Show the sample test to have seen the libraries at least once

Tips:
- Favor JUnit 5 over JUnit 4
- Pick one assertion library or at least not mix it within the same test class
-->

```shell
[INFO] +- org.springframework.boot:spring-boot-starter-test:jar:3.4.5:test
[INFO] |  +- org.springframework.boot:spring-boot-test:jar:3.4.5:test
[INFO] |  +- org.springframework.boot:spring-boot-test-autoconfigure:jar:3.4.5:test
[INFO] |  +- com.jayway.jsonpath:json-path:jar:2.9.0:test
[INFO] |  +- jakarta.xml.bind:jakarta.xml.bind-api:jar:4.0.2:test
[INFO] |  |  \- jakarta.activation:jakarta.activation-api:jar:2.1.3:test
[INFO] |  +- net.minidev:json-smart:jar:2.5.2:test
[INFO] |  |  \- net.minidev:accessors-smart:jar:2.5.2:test
[INFO] |  |     \- org.ow2.asm:asm:jar:9.7.1:test
[INFO] |  +- org.assertj:assertj-core:jar:3.26.3:test
[INFO] |  |  \- net.bytebuddy:byte-buddy:jar:1.15.11:test
[INFO] |  +- org.awaitility:awaitility:jar:4.3.0:test
[INFO] |  +- org.hamcrest:hamcrest:jar:2.2:test
[INFO] |  +- org.junit.jupiter:junit-jupiter:jar:5.11.4:test
[INFO] |  |  +- org.junit.jupiter:junit-jupiter-api:jar:5.11.4:test
[INFO] |  |  |  +- org.junit.platform:junit-platform-commons:jar:1.11.4:test
[INFO] |  |  |  \- org.apiguardian:apiguardian-api:jar:1.1.2:test
[INFO] |  |  +- org.junit.jupiter:junit-jupiter-params:jar:5.11.4:test
[INFO] |  |  \- org.junit.jupiter:junit-jupiter-engine:jar:5.11.4:test
[INFO] |  |     \- org.junit.platform:junit-platform-engine:jar:1.11.4:test
[INFO] |  +- org.mockito:mockito-core:jar:5.17.0:test
[INFO] |  |  +- net.bytebuddy:byte-buddy-agent:jar:1.15.11:test
[INFO] |  |  \- org.objenesis:objenesis:jar:3.3:test
[INFO] |  +- org.mockito:mockito-junit-jupiter:jar:5.17.0:test
[INFO] |  +- org.skyscreamer:jsonassert:jar:1.5.3:test
[INFO] |  |  \- com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test
[INFO] |  +- org.springframework:spring-core:jar:6.2.6:compile
[INFO] |  |  \- org.springframework:spring-jcl:jar:6.2.6:compile
[INFO] |  +- org.springframework:spring-test:jar:6.2.6:test
[INFO] |  \- org.xmlunit:xmlunit-core:jar:2.10.0:test
```

---

## Transitive Test Dependency: JUnit 5

- Modern testing framework for Java applications
- Rewrite of JUnit 4
- JUnit 5 = JUnit Jupiter + JUnit Vintage + JUnit Platform
- Key features: parameterized tests, nested tests, extensions, parallelization

```java
@Test
void shouldCreateNewBook() {
  Book book = new Book("1234", "Spring Boot Testing", "Test Author");

  assertEquals("1234", book.getIsbn());
}

```

---

## Transitive Test Dependency: Mockito

- Mocking framework for unit tests
- Used to isolate the class under test from its dependencies
- Allows verification of interactions between objects
- Golden Mockito Rules:
  - Don't mock what you don't own
  - Don't mock value objects
  - Don't mock everything
  - Show some love with your tests

---


```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookService bookService;

  @Test
  void shouldReturnBookWhenFound() {
    when(bookRepository.findByIsbn("1234")).thenReturn(Optional.of(expectedBook));

    Optional<Book> result = bookService.getBookByIsbn("1234");

    verify(bookRepository).findByIsbn("1234");
  }
}
```

---


## Transitive Test Dependency: AssertJ

- Fluent assertion library for Java tests
- Provides more readable, chain-based assertions
- Rich set of assertions for collections, exceptions, and more

```java
@Test
void shouldProvideFluentAssertions() {
  List<Book> books = List.of(
    new Book("1234", "Spring Boot Testing", "Test Author"),
    new Book("5678", "Advanced Spring", "Another Author")
  );

  assertThat(books)
    .hasSize(2)
    .extracting(Book::getTitle)
    .containsExactly("Spring Boot Testing", "Advanced Spring");
}
```

---

## Transitive Test Dependency: Hamcrest

- Fluent assertion library
- Occasionally used within Spring Test, e.g. MockMvc verifications
- Implementation for many other programming languages

```java
@Test
void shouldMatchWithHamcrest() {
  Book book = new Book("1234", "Spring Boot Testing", "Test Author");

  assertThat(book.getIsbn(), is("1234"));
  assertThat(book.getTitle(), allOf(
    startsWith("Spring"),
    containsString("Testing"),
    not(emptyString())
  ));
}
```
---

## Transitive Test Dependency: Awaitility

- Library for testing asynchronous code
- Provides a DSL for expressing expectations on async operations
- Great for testing concurrent code and background tasks


---

```java
@Test
void shouldEventuallyCompleteAsyncOperation() {
  CompletableFuture<Book> futureBook = CompletableFuture.supplyAsync(() -> {
    try {
      Thread.sleep(300);
      return new Book("1234", "Async Testing", "Author");
    } catch (InterruptedException e) {
      return null;
    }
  });

  await()
    .atMost(1, TimeUnit.SECONDS)
    .until(futureBook::isDone);
}
```

---

## Transitive Test Dependency: JsonPath

- Library for parsing and evaluating JSON documents
- Used for extracting and asserting on JSON structures
- Especially useful in REST API testing

```java
@Test
void shouldParseAndEvaluateJson() throws Exception {
  String json = """
    { "book": {"isbn": "1234", "title": "JSON Testing", "author": "Test Author"}}""";

  DocumentContext context = JsonPath.parse(json);

  assertThat(context.read("$.book.isbn", String.class)).isEqualTo("1234");
  assertThat(context.read("$.book.title", String.class)).isEqualTo("JSON Testing");
}
```

---

## Transitive Test Dependency: JSONAssert

- Assertion library for JSON data structures
- Provides powerful comparison of JSON structures
- Supports strict and lenient comparison modes

```java
@Test
void shouldAssertJsonEquality() throws Exception {
  String actual = """
    { "isbn": "1234", "title": "JSON Testing", "author": "Test Author"}""";

  String expected = """
    { "isbn": "1234", "title": "JSON Testing"}""";

  // Strict mode would fail as expected is missing the author field
  JSONAssert.assertEquals(expected, actual, false);
}
```

---

## Transitive Test Dependency: XMLUnit

- Library for testing XML documents
- Provides comparison and validation of XML
- Useful for testing SOAP services or XML outputs

```java
@Test
void shouldCompareXmlDocuments() {
  String control = "<book><isbn>1234</isbn><title>XML Testing</title></book>";
  String test = "<book><isbn>1234</isbn><title>XML Testing</title></book>";

  Diff diff = DiffBuilder.compare(Input.fromString(control))
    .withTest(Input.fromString(test))
    .build();

  assertFalse(diff.hasDifferences(), diff.toString());
}
```

---

## Design For (Unit) Testability with Spring Boot

- Provide collaborators from outside (dependency injection) -> no `new` inside your code
- Develop small, single responsibility classes
- Test only the public API of your class
- Verify behavior not implementation details
- TDD can help design (better) classes

---
### Avoid Static Method Access

```java
@Service
public class BirthdayService {

  public boolean isTodayBirthday(LocalDate birthday) {
    LocalDate today = LocalDate.now();

    return today.getMonth() == birthday.getMonth()
      && today.getDayOfMonth() == birthday.getDayOfMonth();
  }
}
```

---

### Better Alternative

```java
@Service
public class BirthdayServiceWithClock {

  private final Clock clock;

  public BirthdayServiceWithClock(Clock clock) {
    this.clock = clock;
  }

  public boolean isTodayBirthday(LocalDate birthday) {
    LocalDate today = LocalDate.now(clock);

    return today.getMonth() == birthday.getMonth()
      && today.getDayOfMonth() == birthday.getDayOfMonth();
  }
}
```

---

```java
@Test
void shouldReturnTrueWhenTodayIsBirthday() {
  // Arrange
  LocalDate fixedDate = LocalDate.of(2025, 5, 15);
  Clock fixedClock = Clock.fixed(
    fixedDate.atStartOfDay(ZONE_ID).toInstant(),
    ZONE_ID
  );

  BirthdayServiceWithClock cut = new BirthdayServiceWithClock(fixedClock);
  LocalDate birthday = LocalDate.of(1990, 5, 15); // Same month and day

  // Act
  boolean result = cut.isTodayBirthday(birthday);

  // Assert
  assertThat(result).isTrue();
}
```

---

### Check Your Imports

- Nothing Spring-related here
- Rely only on JUnit, Mockito and an assertion library

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
```

---

### Unify Test Structure

- Use a consistent test method naming: givenWhenThen, shouldWhen, etc.
- Structure test for the Arrange/Act/Assert test setup

```java
@Test
void should_When_() {

  // Arrange
  // ... setting up objects, data, collaborators, etc.

  // Act
  // ... performing the action to be tested on the class under test

  // Assert
  // ... verifying the expected outcome
}
```

---

<!-- _class: code -->

## A Standard Unit Test

```java
@Test
void testBookService() {
    // Given
    Book book = new Book("123", "Test Book", "Test Author");
    when(bookRepository.findById("123")).thenReturn(Optional.of(book));

    // When
    Optional<Book> result = bookService.getBookById("123");

    // Then
    assertTrue(result.isPresent());
    assertEquals("Test Book", result.get().getTitle());
    verify(bookRepository).findById("123");
}
```

---

## JUnit Jupiter Extension API

- Important concept to understand
- Makes JUnit Jupiter extensible
- `SpringExtension` provides Spring integration
- Successor of JUnit 4's `@RunWith`/`@Rule` API


```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

}
```

---

## JUnit Jupiter Extension Points

- Lifecycle Callbacks: `BeforeEachCallback`, `AfterAllCallback`, etc.
- Parameter Resolution: `ParameterResolver`
- Exception Handling: `TestExecutionExceptionHandler`
- Conditional Test Execution: `ExecutionCondition`
- Test Instance Factories: `TestInstanceFactory`, `TestInstancePostProcessor`

---

## Create a Custom Extension

```java
public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Logger logger = LoggerFactory.getLogger(TimingExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        getStore(context).put("start", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long start = getStore(context).remove("start", Long.class);
        long duration = System.currentTimeMillis() - start;
        logger.info("Test {} took {} ms", context.getDisplayName(), duration);
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
}
```

---

# Time For Some Exercises
## Lab 1

- Set up the [repository](https://github.com/PragmaTech-GmbH/testing-spring-boot-applications-demystified-workshop) locally - https://github.com/PragmaTech-GmbH/testing-spring-boot-applications-demystified-workshop
- Search "PragmaTech GitHub" and pick the first pinned repository
- Work locally or use GitHub Codespaces (120 hours/month free)
- Fore Codespaces, pick at least 4-Cores (16 GB RAM) and region `Europe West`
- Navigate to the `labs/lab-1` folder in the repository and complete the tasks as described in the `README` file of that folder
- Time boxed until the end of the coffee break (11:05 AM)

---
marp: true
theme: pragmatech
---

<style>
img[alt~="center"] {
  display: block;
  margin: 0 auto;
}
</style>

![bg](./assets/barcelona-spring-io.jpg)

---

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Full-Day Workshop

_Spring I/O Conference Workshop 21.05.2025_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://twitter.com/rieckpil)


---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Spring I/O 21.05.2025' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

![bg left:33%](assets/generated/lab-2.jpg)

# Lab 2

## Sliced Testing

---

## Discuss Exercises from Lab 1

---

## Unit Testing Has Limits

- **Request Mapping**: Does `/api/users/{id}` actually resolve to your desired method?
- **Validation**: Will incomplete request bodys result in a 400 bad request or return an accidental 200?
- **Serialization**: Are your JSON objects serialized and deserialized correctly?
- **Headers**: Are you setting Content-Type or custom headers correctly?
- **Security**: Are your Spring Security configuration and other authorization checks enforced?
- **Database**: Can we effectively map our complex JPA entity to a database table?
- etc.

---

## Unit Testing a Controller

```java
@ExtendWith(MockitoExtension.class)
class BookControllerUnitTest {

  @Mock
  private BookService bookService;

  @InjectMocks
  private BookController bookController;

  // ...
}
```

---

<!-- _class: section -->

# A Better Alternative
## Sliced Testing

![bg right:33%](assets/generated/slice.jpg)

---

## A Typical Spring Application Context

![w:600 center](assets/generated/spring-context.png)

---

![w:700 center](assets/generated/spring-sliced-context.png)

---

## Slicing Example: @WebMvcTest

- Testing your web layer in isolation and only load the beans you need
- `MockMvc`: Mocked servlet environment with HTTP semantics

```java
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

}
```

- See `WebMvcTypeExcludeFilter` for included Spring beans

---

![center](assets/generated/slicing-annotations.png)

---

## Common Test Slices

- `@WebMvcTest` - Controller layer
- `@DataJpaTest` - Repository layer
- `@JsonTest` - JSON serialization/deserialization
- `@RestClientTest` - RestTemplate testing
- `@WebFluxTest` - WebFlux controller testing
- `@JdbcTest` - JDBC testing

---

## Introducing: @DataJpaTest

```java
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;
}
```

- Tests JPA repositories
- Auto-configures in-memory database
- Provides `TestEntityManager`
- Verify JPA entity mapping, creation and native queries

---

## In-Memory vs. Real Database

- By default, Spring Boot tries to autoconfigure an in-memory relational database (H2 or Derby)
- In-memory database pros:
  - Easy to use & fast
  - Less overhead
- In-memory database cons:
  - Mismatch with the infrastructure setup in production
  - Despite having compatibility modes, we can't fully test proprietary database features

---

<!--

Notes:

- who is not using Testcontainers
- explain basics

-->

## Solution: Docker & Testcontainers

![bg right:33%](assets/generated/containers.jpg)

---

## Using a Real Database

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
  .withDatabaseName("testdb")
  .withUsername("test")
  .withPassword("test")
  .withInitScript("init-postgres.sql"); // Initialize PostgreSQL with required extensions
```

---

![](assets/hibernate-persistence-context.svg)

---

## Test Data Management

- Each test should start with a known state
- Tests should not interfere with each other
- Options:
  - Truncate tables between tests
  - Transaction rollback (`@Transactional`)
  - Separate schemas per test
  - Database resets

---

## Testing Native Queries

```java
/**
 * PostgreSQL-specific: Full text search on book titles with ranking.
 * Uses PostgreSQL's to_tsvector and to_tsquery for sophisticated text searching
 * with ranking based on relevance.
 *
 * @param searchTerms the search terms (e.g. "adventure dragons fantasy")
 * @return list of books matching the search terms, ordered by relevance
 */
@Query(value = """
  SELECT * FROM books
  WHERE to_tsvector('english', title) @@ plainto_tsquery('english', :searchTerms)
  ORDER BY ts_rank(to_tsvector('english', title), plainto_tsquery('english', :searchTerms)) DESC
  """,
  nativeQuery = true)
List<Book> searchBooksByTitleWithRanking(@Param("searchTerms") String searchTerms);
```

---

# Time For Some Exercises
## Lab 2

- Work with the same repository as in lab 1
- Navigate to the `labs/lab-2` folder in the repository and complete the tasks as described in the `README` file of that folder
- Time boxed until the end of the lunch break (14:00 AM)
---
marp: true
theme: pragmatech
---

![bg](./assets/barcelona-spring-io.jpg)

---

<style>
img[alt~="center"] {
  display: block;
  margin: 0 auto;
}
</style>

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Full-Day Workshop

_Spring I/O Conference Workshop 21.05.2025_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://twitter.com/rieckpil)

---

## Discuss Exercises from Lab 2

---


![bg left:33%](assets/generated/lab-3.jpg)

# Lab 3

## Integration Testing

---

<!-- _class: section -->

# Starting Everything
## Writing Tests Against a Complete Application Context

![bg right:33%](assets/generated/full.jpg)

---

<!--

Notes:

-->

## The Default Integration Test

![](assets/generated/spring-boot-test-setup.png)

---

## Starting the Entire Context

- Provide external infrastructure with [Testcontainers](https://testcontainers.com/)
- Start Tomcat with: `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`
- Consider WireMock/MockServer for stubbing external HTTP services
- Test controller endpoints via: `MockMvc`, `WebTestClient`, `TestRestTemplate`

---

## Introducing: Microservice HTTP Communication

```java
public BookMetadataResponse getBookByIsbn(String isbn) {
  return webClient.get()
    .uri("/isbn/{isbn}", isbn)
    .retrieve()
    .bodyToMono(BookMetadataResponse.class)
    .block();
}
```

---

## HTTP Communication During Tests

- Unreliable when performing real HTTP responses during tests
- Sample data?
- Authentication?
- Cleanup?
- No airplane-mode testing possible anymore
- Solution: Stub the HTTP responses for remote system

---

![w:1200 h:500](assets/wiremock-usage.svg)

---

## Introducing WireMock

- In-memory (or container) Jetty to stub HTTP responses
- Simulate failures, slow responses, etc.
- Stateful setups possible (scenarios): first request fails, then succeeds
- Alternatives: MockServer, MockWebServer, etc.

```java
wireMockServer.stubFor(
  get("/isbn/" + isbn)
    .willReturn(aResponse()
      .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .withBodyFile(isbn + "-success.json"))
);
```

---

## Making Our Application Context Start

- Stubbing HTTP responses during the launch of our Spring Context
- Introducing a new concept: `ContextInitializer`

```java
WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

wireMockServer.start();

// Register a shutdown hook to stop WireMock when the context is closed
applicationContext.addApplicationListener(event -> {
  if (event instanceof ContextClosedEvent) {
    logger.info("Stopping WireMock server");
    wireMockServer.stop();
  }
});

TestPropertyValues.of(
  "book.metadata.api.url=http://localhost:" + wireMockServer.port()
).applyTo(applicationContext);
```

---
<!--

- Go to `DefaultContextCache` to show the cache

-->

## Spring Test `TestContext` Caching

- Part of Spring Test (automatically part of every Spring Boot project via `spring-boot-starter-test`)
- Spring Test caches an already started Spring `ApplicationContext` for later reuse
- Cache retrieval is usually faster than a cold context start
- Configurable cache size (default is 32) with LRU (least recently used) strategy

Speed up your build:

![](assets/generated/context-cache-improvements.png)

---

## Caching is King

![center](assets/cache.svg)

---

## How the Cache Key is Built

This goes into the cache key (`MergedContextConfiguration`):

- activeProfiles (`@ActiveProfiles`)
- contextInitializersClasses (`@ContextConfiguration`)
- propertySourceLocations (`@TestPropertySource`)
- propertySourceProperties (`@TestPropertySource`)
- contextCustomizer (`@MockitoBean`, `@MockBean`, `@DynamicPropertySource`, ...)

---
## Identify Context Restarts

![](assets/context-caching-hints.png)


---

## Investigate the Logs

![](assets/context-caching-logs.png)

---

## Spot the Issues for Context Caching

![](assets/context-caching-bad.png)

---

## Context Caching Issues

Common problems that break caching:

1. Different context configurations
2. `@DirtiesContext` usage
3. Modifying beans in tests
4. Different property settings
5. Different active profiles

---

## Make the Most of the Caching Feature


- Avoid `@DirtiesContext` when possible, especially at `AbstractIntegrationTest` classes
- Understand how the cache key is built
- Monitor and investigate the context restarts
- Align the number of unique context configurations for your test suite

---

# Time For Some Exercises
## Lab 3

- Work with the same repository as in lab 1/lab 2
- Navigate to the `labs/lab-3` folder in the repository and complete the tasks as described in the `README` file of that folder
- Time boxed until the end of the coffee break (15:50 AM)

---
marp: true
theme: pragmatech
---

![bg](./assets/barcelona-spring-io.jpg)

---

<style>
img[alt~="center"] {
  display: block;
  margin: 0 auto;
}
</style>

<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## Full-Day Workshop

_Spring I/O Conference Workshop 21.05.2025_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://twitter.com/rieckpil)

---

<!-- header: 'Testing Spring Boot Applications Demystified Workshop @ Spring I/O 21.05.2025' -->
<!-- footer: '![w:32 h:32](assets/generated/logo.webp)' -->

![bg left:33%](assets/generated/lab-4.jpg)

# Lab 4

## Best Practices, Pitfalls, AI & Outlook

---

## Discuss Exercises from Lab 3

---

# Last Lab
## Spring Boot Testing Best Practices & Pitfalls

![bg right:33%](assets/generated/best-practices.jpg)

---


### Best Practice 1: Test Parallelization

**Goal**: Reduce build time and get faster feedback

Requirements:
- No shared state
- No dependency between tests and their execution order
- No mutation of global state

Two ways to achieve this:
- Fork a new JVM with Surefire/Failsafe and let it run in parallel -> more resources but isolated execution
- Use JUnit Jupiter's parallelization mode and let it run in the same JVM with multiple threads

---

![bg w:800 h:900 center](assets/parallel-testing.svg)

---

<!--

Notes:
- Useful to get started
- Boilerplate and skeleton help
- LLM very usueful for boilerplate setup, test data, test migration (e.g. Kotlin -> Java)
- ChatBots might not produce compilable/working test code, agents are better
-->

### Best Practice 2: Get Help from AI

- [Diffblue Cover](https://www.diffblue.com/): #1 AI Agent for unit testing complex Java code at scale
- Agent vs. Assistant
- LLMs: ChatGPT, Claude, Gemini, etc.
- Claude Code
- TDD with an LLM?
- (Not AI but still useful) OpenRewrite for migrations
- Clearly define your requirements in e.g. `claude.md` or cursor rule files

---

### Best Practice 3: Try Mutation Testing

- Having high code coverage might give you a **false sense of security**
- Mutation Testing with [PIT](https://pitest.org/quickstart/)
- Beyond Line Coverage: Traditional tools like JaCoCo show which code runs during tests, but PIT verifies if your tests actually detect when code behaves incorrectly by introducing "**mutations**" to your source code.
- Quality Guarantee: PIT automatically modifies your code (changing conditionals, return values, etc.) to ensure your tests fail when they should, **revealing blind spots** in seemingly comprehensive test suites.
- Considerations for bigger projects: only run on the new code diffs, not on the whole codebase

---

![center w:800 h:600](assets/mutation.svg)

---

# Common Spring Boot Testing Pitfalls to Avoid

![bg right:33%](assets/generated/pitfalls.jpg)

---

## Testing Pitfall 1: `@SpringBootTest` Obsession

- The name could apply it's a one size fits all solution, but it isn't
- It comes with costs: starting the (entire) application context
- Useful for integration tests that verify the whole application but not for testing a single service in isolation
- Start with unit tests, see if sliced tests are applicable and only then use `@SpringBootTest`

---

## @SpringBootTest Obsession Visualized

![](assets/generated/spring-boot-test-obsession.png)

---

## Testing Pitfall 2: @MockitoBean vs. @MockBean vs. @Mock

- `@MockBean` is a Spring Boot specific annotation that replaces a bean in the application context with a Mockito mock
- `@MockBean` is deprecated in favor of the new `@MockitoBean` annotation
- `@Mock` is a Mockito annotation, only for unit tests

---

## Testing Pitfall 3: JUnit 4 vs. JUnit 5

![bg right:33%](assets/generated/car-comparison.jpg)

- You can mix both versions in the same project but not in the same test class
- Browsing through the internet (aka. StackOverflow/blogs/LLMs) for solutions, you might find test setups that are still for JUnit 4
- Easily import the wrong `@Test` and you end up wasting one hour because the Spring context does not work as expected

---

<center>

| JUnit 4              | JUnit 5                            |
|----------------------|------------------------------------|
| @Test from org.junit | @Test from org.junit.jupiter.api   |
| @RunWith             | @ExtendWith/@RegisterExtension     |
| @ClassRule/@Rule     | -                                  |
| @Before              | @BeforeEach                        |
| @Ignore              | @Disabled                          |
| @Category            | @Tag                               |

</center>

---

<!--

Notes:

- Rich ecosystem: LocalStack, Contract testing (Pact), GreenMail, Selenide, Performance Testing

-->

## Summary Lab 1

- Spring Boot applications come with batteries-included for testing
- Testing Swiss-Army Knife pulls many test libraries
- Master JUnit, Mockito and AssertJ first
- Maven Failsafe and Maven Surefire Plugin run our tests (Gradle equivalent `test` task)
- Explore the JUnit Jupiter Extension Model for cross-cutting test concerns

---

## Summary Lab 2

- Sliced testing helps to verify parts of your application in isolation
- `@WebMvcTest`: Verify our controller when it comes to validation, authentication, authorization, serialization, exception mapping, etc.
- `@DataJpaTest`: Test our JPA-related code with a real database
- Testcontainers: Seamlessly start external infrastructure components locally

---

## Summary Lab 3

- Things might get complicated when trying to launch the entire application context
- WireMock helps to stub remote HTTP services
- The context caching feature can drastically reduce build times
- Consider the caching key structure when writing your integration tests for maximum reuse

---

## Summary Lab 4

- Test parallelization can help reduce build times even further
- Don't use `@SpringBootTest` everytime
- `@MockitoBean` vs. `@MockBean` vs. `@Mock`
- Consider the JUnit 4 vs. 5 pitfall
- AI can help you write your tests
- Give mutation testing a try

---

## What We Couldn't Touch Today

- E2E
- Tests involving the UI
- TDD (Test-Driven Development)
- BDD (Behaviour-Driven Development)
- Contract Testing
- The entire rich & mature Java testing ecosystem
- Testing reactive Spring Boot Applications

---

## Further Resources on Testing

![bg h:1200 right:33%](assets/generated/offers-w.png)

- Online Course: [Testing Spring Boot Applications Masterclass](https://rieckpil.de/testing-spring-boot-applications-masterclass/) (on-demand, 12 hours, 130+ modules)
- eBook: [30 Testing Tools and Libraries Every Java Developer Must Know](https://leanpub.com/java-testing-toolbox)
- eBook: [Stratospheric - From Zero to Production with AWS](https://leanpub.com/stratospheric)
- Spring Boot [testing workshops](https://pragmatech.digital/workshops/) (in-house/remote/hybrid)
- [Consulting offerings](https://pragmatech.digital/consulting/), e.g. the Test Maturity Assessment

---

![bg right:33%](assets/generated/end.jpg)

## Next Steps

- Request your certificate of completion via mail/LinkedIn
- Share your feedback, e.g. you could share three highlights and three areas for improvement
- Enjoy the next two days at Spring I/O
- Joyful testing!

**LinkedIn**: Philip Riecks
**X**: @rieckpil
**Mail**: philip@pragmatech.digital

