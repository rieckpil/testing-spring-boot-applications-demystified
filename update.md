# Demystifying Testing Spring Boot Applications

## Introduction

Welcome to the journey of understanding and mastering Spring Boot testing. This guide aims to demystify the often complex world of testing Spring Boot applications, presenting concepts in a progressive, approachable manner that helps you build confidence in your testing practices.

Testing isn't just a checkbox to tick off in your development process—it's an essential practice that ensures your applications work correctly, remain maintainable, and can evolve with changing requirements. Yet, many developers find testing challenging, time-consuming, or sometimes even intimidating.

This book takes a different approach. Rather than prescribing rigid testing methodologies, we'll explore the "why" behind testing practices, helping you understand the reasoning so you can make informed decisions for your unique scenarios. Through practical examples, real-world patterns, and step-by-step guidance, we'll transform testing from a necessary burden into a powerful tool in your development arsenal.

Whether you're new to Spring Boot testing or looking to refine your existing practices, this guide will meet you where you are and help elevate your testing expertise to the next level.

## Chapter 1: The Foundation of Spring Boot Testing

### Understanding the Testing Pyramid

Before diving into Spring Boot-specific testing practices, it's important to understand the conceptual framework that guides modern testing strategies. The Testing Pyramid, popularized by Mike Cohn, provides a visual representation of how different types of tests should be distributed in a healthy test suite.

The pyramid consists of three main layers:

1. **Unit Tests** (Base): These form the foundation of your testing strategy. Unit tests are numerous, fast, and focused on testing individual components in isolation. They verify that each piece of your code works correctly on its own.

2. **Integration Tests** (Middle): These tests verify that different components work together correctly. They're fewer in number than unit tests but provide confidence that your units collaborate as expected.

3. **End-to-End Tests** (Top): These tests verify that the entire application works as expected from a user's perspective. They're the fewest in number but provide the highest level of confidence that your application works in a real-world scenario.

The shape of the pyramid suggests that you should have many unit tests, fewer integration tests, and even fewer end-to-end tests. This distribution helps ensure that your test suite is fast, reliable, and maintainable.

```
    /\
   /  \
  /    \  End-to-End Tests
 /      \
/        \
----------
|        |
|        |  Integration Tests
|        |
----------
|        |
|        |
|        |  Unit Tests
|        |
|        |
----------
```

### The Value of Different Test Types

Each layer of the testing pyramid serves a unique purpose and provides different kinds of value:

**Unit Tests:**
- Fast feedback loop (milliseconds)
- Precise failure localization
- Comprehensive coverage of edge cases
- Documentation of component behavior
- Enables confident refactoring

**Integration Tests:**
- Verifies component interactions
- Tests configuration and wiring
- Catches interface mismatches
- Validates data flows
- Tests application subsets

**End-to-End Tests:**
- Validates user workflows
- Tests the full application stack
- Catches integration issues
- Verifies business requirements
- Provides confidence for deployment

While the pyramid provides a useful guideline, the exact distribution of tests will depend on your application's specific needs. A complex domain with intricate business rules might benefit from more unit tests, while an application with many external dependencies might need more integration tests.

### Spring Boot's Testing Support

Spring Boot provides comprehensive support for testing at all levels of the pyramid. The `spring-boot-starter-test` dependency includes the most commonly used testing libraries:

- **JUnit 5**: The foundation for writing and running tests
- **Spring Test & Spring Boot Test**: For testing Spring applications
- **AssertJ**: For fluent assertions
- **Hamcrest**: For matcher assertions
- **Mockito**: For mocking dependencies
- **JSONassert**: For JSON assertions
- **JsonPath**: For JSON parsing

This integrated toolset makes it easier to write effective tests for Spring Boot applications at all levels of the testing pyramid.

## Chapter 2: Unit Testing Spring Components

### The Building Blocks: JUnit 5 and Mockito

At the heart of Spring Boot testing lies JUnit 5 (also known as JUnit Jupiter), the latest version of the most popular testing framework for Java applications. JUnit 5 introduced significant improvements over its predecessors, including:

- A modular architecture
- Improved extension model
- Parameterized tests
- Conditional test execution
- Nested tests for better organization

Let's explore a simple JUnit 5 test for a Spring Boot component:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookTest {
    @Test
    void bookShouldBeAvailableByDefault() {
        Book book = new Book("123", "Test Title", "Test Author");
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }
}
```

This test verifies that a newly created `Book` has the status `AVAILABLE` by default. It's simple, focused, and tests a single aspect of the `Book` class.

### Mocking Dependencies with Mockito

When testing components that have dependencies, we often want to isolate the component under test by replacing its dependencies with mock objects. This allows us to test the component's behavior without worrying about the behavior of its dependencies.

Mockito is the most popular mocking framework for Java and is included in Spring Boot's testing starter. Here's an example of using Mockito to test a service that depends on a repository:

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void findByIsbn_shouldReturnBook_whenBookExists() {
        // Given
        Book expectedBook = new Book("123", "Test Title", "Test Author");
        when(bookRepository.findById("123")).thenReturn(Optional.of(expectedBook));

        // When
        Optional<Book> result = bookService.findByIsbn("123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedBook, result.get());
        verify(bookRepository).findById("123");
    }
}
```

In this test:
1. We use `@ExtendWith(MockitoExtension.class)` to enable Mockito's JUnit 5 integration
2. We create a mock `BookRepository` using `@Mock`
3. We inject this mock into the `BookService` using `@InjectMocks`
4. We configure the mock to return a specific book when its `findById` method is called with the ISBN "123"
5. We call the `findByIsbn` method on the service
6. We verify that the result matches our expectations
7. We verify that the repository's `findById` method was called exactly once with the correct argument

This pattern of "arrange, act, assert" (also known as "given, when, then") helps structure tests in a clear and consistent way.

### Testing Time-Dependent Code

One common challenge in testing is dealing with code that depends on the current time. For example, code that checks if a book loan is overdue based on the current date can be difficult to test because the current date changes with each test run.

Here's an example of code that's difficult to test:

```java
public boolean isOverdue(LocalDate dueDate) {
    return LocalDate.now().isAfter(dueDate);
}
```

This method directly uses `LocalDate.now()`, which makes it impossible to control the "current" date during tests.

A better approach is to inject a `Clock` or a `TimeProvider` that can be controlled during tests:

```java
public boolean isOverdue(LocalDate dueDate, Clock clock) {
    return LocalDate.now(clock).isAfter(dueDate);
}
```

Now we can test this method with a fixed clock:

```java
@Test
void isOverdue_shouldReturnTrue_whenDueDateIsInPast() {
    // Given
    LocalDate dueDate = LocalDate.of(2023, 1, 1);
    Clock fixedClock = Clock.fixed(
        Instant.parse("2023-02-01T10:15:30Z"),
        ZoneId.systemDefault()
    );

    // When
    boolean result = overdueChecker.isOverdue(dueDate, fixedClock);

    // Then
    assertTrue(result);
}

@Test
void isOverdue_shouldReturnFalse_whenDueDateIsInFuture() {
    // Given
    LocalDate dueDate = LocalDate.of(2023, 3, 1);
    Clock fixedClock = Clock.fixed(
        Instant.parse("2023-02-01T10:15:30Z"),
        ZoneId.systemDefault()
    );

    // When
    boolean result = overdueChecker.isOverdue(dueDate, fixedClock);

    // Then
    assertFalse(result);
}
```

By injecting a `Clock`, we can control the "current" date during tests, making our tests deterministic and reliable.

### Creating Custom JUnit Extensions

JUnit 5's extension model allows us to create custom extensions that can add functionality to our tests. These extensions can be used to reduce boilerplate code, enforce testing patterns, or provide common utilities.

Let's create a simple extension that times the execution of each test:

```java
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        getStore(context).put("start", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long start = getStore(context).remove("start", Long.class);
        long duration = System.currentTimeMillis() - start;
        System.out.printf("Test '%s' took %d ms%n", context.getDisplayName(), duration);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
}
```

To use this extension, we add it to our test class:

```java
@ExtendWith(TimingExtension.class)
class SlowTestsIdentificationTest {
    @Test
    void fastTest() {
        // Test code
    }

    @Test
    void slowTest() throws InterruptedException {
        Thread.sleep(1000); // Simulate slow operation
        // Test code
    }
}
```

When these tests run, the extension will print the execution time of each test, helping us identify slow tests that might need optimization.

### Testing Best Practices for Units

As we wrap up this chapter on unit testing, let's review some best practices:

1. **Test One Thing at a Time**: Each test should verify one specific behavior or scenario. This makes tests easier to understand, maintain, and debug.

2. **Use Descriptive Test Names**: Test names should clearly describe what the test is verifying. Consider using a naming pattern like `methodName_expectedBehavior_whenCondition`.

3. **Follow the AAA Pattern**: Structure your tests with Arrange, Act, Assert (or Given, When, Then) sections to make them clear and consistent.

4. **Keep Tests Independent**: Tests should not depend on each other or on a specific execution order. Each test should set up its own test data and clean up after itself.

5. **Test Edge Cases**: Don't just test the happy path. Test boundary conditions, error cases, and edge cases to ensure your code handles all situations correctly.

6. **Make Failed Tests Obvious**: When a test fails, it should be clear what went wrong. Use specific assertions and include meaningful error messages.

7. **Keep Tests Fast**: Unit tests should run quickly to provide fast feedback. If a test is slow, consider whether it's actually an integration test in disguise.

By following these practices, you'll create a robust suite of unit tests that provides confidence in your code's behavior and serves as living documentation of your system.

## Chapter 3: Sliced Testing with Spring Boot

### Introduction to Test Slices

Spring Boot applications typically consist of multiple layers: controllers handling HTTP requests, services implementing business logic, repositories accessing databases, and so on. When testing these different layers, we often want to focus on a specific "slice" of the application while mocking or ignoring the rest.

Spring Boot provides specialized test annotations that configure the test context to include only the components needed for testing a particular slice of the application. These "test slices" help keep tests focused, fast, and reliable by limiting the components that are initialized.

### Testing Web Controllers with @WebMvcTest

The `@WebMvcTest` annotation is used to test Spring MVC controllers. It disables full auto-configuration and only applies configuration relevant to MVC tests, including setting up a `MockMvc` instance for you to use.

Here's an example of a controller test using `@WebMvcTest`:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    void getBookById_shouldReturnBook_whenBookExists() throws Exception {
        // Given
        Book book = new Book("123", "Test Title", "Test Author");
        when(bookService.findByIsbn("123")).thenReturn(Optional.of(book));

        // When & Then
        mockMvc.perform(get("/api/books/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isbn").value("123"))
            .andExpect(jsonPath("$.title").value("Test Title"))
            .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void getBookById_shouldReturn404_whenBookDoesNotExist() throws Exception {
        // Given
        when(bookService.findByIsbn("999")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/books/999"))
            .andExpect(status().isNotFound());
    }
}
```

In this test:
1. We use `@WebMvcTest(BookController.class)` to specify that we're testing the `BookController` class
2. Spring Boot auto-configures a `MockMvc` instance that we can inject
3. We use `@MockBean` to provide a mock of the `BookService` that our controller depends on
4. We configure the mock service to return specific results for different method calls
5. We use `MockMvc` to simulate HTTP requests to our controller
6. We use assertions to verify that the responses match our expectations

This approach allows us to test the controller's request handling, input validation, response formatting, and error handling without starting a full web server or involving the real service implementation.

### Testing Security with @WebMvcTest

If your application uses Spring Security, you can also test security rules using `@WebMvcTest`. The `spring-security-test` module provides utilities like `MockMvc` security setup and test authentication annotations.

Here's an example of testing secured endpoints:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class SecuredBookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    void getAllBooks_shouldBeAccessibleToAnyone() throws Exception {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk());
    }

    @Test
    void createBook_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType("application/json")
                .content("{\"isbn\":\"123\",\"title\":\"Test\",\"author\":\"Test\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_shouldBeAccessibleToAdmins() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType("application/json")
                .content("{\"isbn\":\"123\",\"title\":\"Test\",\"author\":\"Test\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBook_shouldBeForbiddenForRegularUsers() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType("application/json")
                .content("{\"isbn\":\"123\",\"title\":\"Test\",\"author\":\"Test\"}"))
            .andExpect(status().isForbidden());
    }
}
```

In these tests, we're verifying that:
1. The endpoint to get all books is accessible without authentication
2. The endpoint to create a book requires authentication
3. Users with the ADMIN role can create books
4. Users with only the USER role cannot create books

The `@WithMockUser` annotation is particularly useful as it allows us to simulate authenticated users with specific roles for testing secured endpoints.

### Testing JPA Repositories with @DataJpaTest

The `@DataJpaTest` annotation is used to test JPA repositories. It configures an in-memory database, scans for `@Entity` classes and Spring Data JPA repositories, and provides a `TestEntityManager` for testing.

Here's an example of a repository test using `@DataJpaTest`:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByAuthor_shouldReturnBooks_whenAuthorMatches() {
        // Given
        Book book1 = new Book("123", "Title 1", "Author A");
        Book book2 = new Book("456", "Title 2", "Author B");
        Book book3 = new Book("789", "Title 3", "Author A");

        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.flush();

        // When
        List<Book> foundBooks = bookRepository.findByAuthorContainingIgnoreCase("author a");

        // Then
        assertThat(foundBooks).hasSize(2);
        assertThat(foundBooks).extracting(Book::getIsbn).containsExactlyInAnyOrder("123", "789");
    }

    @Test
    void findByPublishedDateAfter_shouldReturnBooks_whenPublishedAfterDate() {
        // Given
        Book book1 = new Book("123", "Title 1", "Author");
        book1.setPublishedDate(LocalDate.of(2020, 1, 1));

        Book book2 = new Book("456", "Title 2", "Author");
        book2.setPublishedDate(LocalDate.of(2021, 1, 1));

        Book book3 = new Book("789", "Title 3", "Author");
        book3.setPublishedDate(LocalDate.of(2022, 1, 1));

        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.flush();

        // When
        List<Book> foundBooks = bookRepository.findByPublishedDateAfter(LocalDate.of(2020, 6, 1));

        // Then
        assertThat(foundBooks).hasSize(2);
        assertThat(foundBooks).extracting(Book::getIsbn).containsExactlyInAnyOrder("456", "789");
    }
}
```

In these tests:
1. We use `@DataJpaTest` to configure an in-memory database and scan for repositories
2. We inject a `TestEntityManager` which provides lower-level JPA operations for test setup
3. We inject the repository we want to test
4. We use the entity manager to set up test data
5. We call the repository methods we want to test
6. We use assertions to verify that the results match our expectations

This approach allows us to test our repository methods in isolation from the rest of the application, using a real database but without the overhead of starting the entire application context.

### Other Test Slices

Spring Boot provides several other test slice annotations for testing different parts of your application:

- `@JsonTest`: For testing JSON serialization and deserialization
- `@RestClientTest`: For testing REST clients
- `@DataMongoTest`: For testing MongoDB repositories
- `@JdbcTest`: For testing JDBC components
- `@JooqTest`: For testing jOOQ-based database access
- `@DataRedisTest`: For testing Redis operations
- `@WebFluxTest`: For testing WebFlux controllers

Each of these annotations configures a minimal application context with only the components needed for testing that particular slice of the application.

## Chapter 4: Integration Testing with @SpringBootTest

### Beyond Slices: Testing the Whole Context

While sliced tests are great for testing individual components, integration tests are needed to verify that these components work together correctly. Spring Boot's `@SpringBootTest` annotation creates a full application context, allowing you to test the interactions between all your application's components.

There are multiple ways to use `@SpringBootTest`, depending on how much of the application you want to start:

1. **Mock Web Environment**: Default mode where the web environment is mocked
2. **Random Port**: Starts the server with a random port
3. **Defined Port**: Starts the server with a defined port
4. **None**: Does not start the web environment at all

Here's an example of an integration test using `@SpringBootTest`:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllBooks_shouldReturnBooks() {
        // When
        ResponseEntity<Book[]> response = restTemplate.getForEntity("/api/books", Book[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // More assertions about the response body
    }

    @Test
    void createBook_shouldReturnCreatedBook() {
        // Given
        Book newBook = new Book("123", "Test Title", "Test Author");

        // When
        ResponseEntity<Book> response = restTemplate.postForEntity("/api/books", newBook, Book.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsbn()).isEqualTo("123");
        // More assertions about the response body
    }
}
```

In these tests:
1. We use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` to start the application with a random port
2. We inject a `TestRestTemplate` which we can use to make HTTP requests to our application
3. We make HTTP requests to our application's endpoints
4. We verify that the responses match our expectations

This approach allows us to test our application from an external client's perspective, verifying that all layers of the application work together correctly.

### The Challenge of Test Data

One of the challenges with integration tests is managing test data. Unlike unit tests where we can mock dependencies, integration tests need actual data in the database. There are several strategies for managing test data in integration tests:

1. **In-memory Database**: Using an in-memory database like H2 for tests
2. **Database Migration Tools**: Using tools like Flyway or Liquibase to set up the schema
3. **Test Data Builders**: Using builder patterns to create test data
4. **SQL Scripts**: Using SQL scripts to set up test data
5. **Programmatic Setup**: Setting up data programmatically in tests

Let's look at an example using programmatic setup with JPA repositories:

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Clear existing data
        bookRepository.deleteAll();

        // Set up test data
        bookRepository.saveAll(List.of(
            new Book("123", "Title 1", "Author 1"),
            new Book("456", "Title 2", "Author 2"),
            new Book("789", "Title 3", "Author 3")
        ));
    }

    @Test
    void getAllBooks_shouldReturnAllBooks() {
        // When
        ResponseEntity<Book[]> response = restTemplate.getForEntity("/api/books", Book[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        // More assertions
    }

    // More tests
}
```

In this test, we're using `@BeforeEach` to set up test data before each test. This ensures that each test starts with a clean, known state.

### Testing with Real Databases using TestContainers

While in-memory databases are convenient for testing, they might not fully replicate the behavior of your production database. This can lead to tests that pass in development but fail in production due to database-specific behavior.

TestContainers is a Java library that provides lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container. Using TestContainers, we can test with real databases that match our production environment.

Here's an example of using TestContainers with Spring Boot for integration testing:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookTestContainersTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    // Tests using real PostgreSQL database
}
```

In this setup:
1. We use `@Testcontainers` to enable TestContainers JUnit 5 support
2. We define a PostgreSQL container using `@Container`
3. We use `@DynamicPropertySource` to configure Spring Boot to use the database properties from the container
4. Our tests now run against a real PostgreSQL database in a container

This approach provides more confidence that our application will work correctly in production, as we're testing with a database that closely matches our production environment.

### Optimizing Test Context Caching

The Spring Test Context Framework caches application contexts between tests to avoid the overhead of starting a new context for each test class. However, if tests modify the context (for example, by changing bean definitions or profiles), the context will need to be reloaded.

Here are some strategies to optimize test context caching:

1. **Group Similar Tests**: Keep tests that use the same context configuration in the same class or package
2. **Avoid @DirtiesContext When Possible**: This annotation tells Spring to reload the context, which can be slow
3. **Use Transactions for Data Cleanup**: Instead of clearing the database, use transactions that are rolled back after each test
4. **Be Careful with @MockBean and @SpyBean**: These annotations modify the context, so use them consistently across test classes

Here's an example of using transactions for test isolation:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TransactionalBookServiceTest {
    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void createBook_shouldPersistBook() {
        // Given
        Book book = new Book("123", "Test Title", "Test Author");

        // When
        Book createdBook = bookService.createBook(book);

        // Then
        assertThat(bookRepository.findById("123")).isPresent();
    }

    // More tests that modify the database
    // Each test's changes are rolled back after the test completes
}
```

By using `@Transactional`, we ensure that any changes made during the test are rolled back after the test completes. This keeps tests isolated without the need to manually clean up the database or reload the context.

## Chapter 5: Testing Web Clients and External Services

### Modern API Communication: WebClient

In modern Spring Boot applications, `WebClient` from the WebFlux module has largely replaced the older `RestTemplate` for making HTTP requests to external services. `WebClient` provides a more modern API with support for reactive programming, which can be more efficient and flexible.

Here's an example of a service that uses `WebClient` to communicate with an external book information service:

```java
@Service
public class BookInfoService {
    private final WebClient webClient;

    public BookInfoService(WebClient.Builder webClientBuilder,
                          @Value("${book.info.service.url}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<BookInfo> getBookInfo(String isbn) {
        return webClient.get()
                .uri("/api/books/{isbn}", isbn)
                .retrieve()
                .bodyToMono(BookInfo.class);
    }
}
```

Testing services that use `WebClient` can be challenging because they make real HTTP requests to external services. We don't want our tests to depend on the availability or state of these external services.

### Testing WebClient with WireMock

WireMock is a library for stubbing and mocking web services. It can be used to create a mock server that responds to HTTP requests in a way that we control, making it perfect for testing WebClient code without relying on external services.

Here's an example of testing a service that uses WebClient with WireMock:

```java
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WireMockTest(httpPort = 8080)
@TestPropertySource(properties = "book.info.service.url=http://localhost:8080")
class BookInfoServiceTest {
    @Autowired
    private BookInfoService bookInfoService;

    @Test
    void getBookInfo_shouldReturnBookInfo_whenServiceReturns200() {
        // Given
        stubFor(get(urlEqualTo("/api/books/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"isbn\":\"123\",\"title\":\"Test Title\",\"author\":\"Test Author\"}")));

        // When
        BookInfo bookInfo = bookInfoService.getBookInfo("123").block();

        // Then
        assertThat(bookInfo).isNotNull();
        assertThat(bookInfo.getIsbn()).isEqualTo("123");
        assertThat(bookInfo.getTitle()).isEqualTo("Test Title");
        assertThat(bookInfo.getAuthor()).isEqualTo("Test Author");

        // Verify the request was made as expected
        verify(getRequestedFor(urlEqualTo("/api/books/123")));
    }

    @Test
    void getBookInfo_shouldHandleErrorResponse_whenServiceReturns500() {
        // Given
        stubFor(get(urlEqualTo("/api/books/error"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        // When & Then
        assertThrows(WebClientResponseException.class, () -> {
            bookInfoService.getBookInfo("error").block();
        });
    }
}
```

In these tests:
1. We use `@WireMockTest` to start a WireMock server on port 8080
2. We configure our application to use the WireMock server URL instead of the real service URL
3. We stub HTTP requests to the mock server to return specific responses
4. We call our service methods that use WebClient
5. We verify that our service correctly processes the mock responses
6. We also verify that the expected HTTP requests were made to the mock server

This approach allows us to test our WebClient code in isolation from external services, making our tests faster, more reliable, and more focused.

### Testing Reactive APIs with WebTestClient

When testing reactive APIs built with Spring WebFlux, we can use `WebTestClient` which is specifically designed for testing reactive web applications. It provides a fluent API for making requests and asserting on responses, with support for both server-side and client-side testing.

Here's an example of testing a reactive controller with `WebTestClient`:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@WebFluxTest(BookController.class)
class BookControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @Test
    void getAllBooks_shouldReturnBooks() {
        // Given
        Book book1 = new Book("123", "Title 1", "Author 1");
        Book book2 = new Book("456", "Title 2", "Author 2");
        when(bookService.findAllBooks()).thenReturn(Flux.just(book1, book2));

        // When & Then
        webTestClient.get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class)
                .hasSize(2)
                .contains(book1, book2);
    }

    @Test
    void getBookById_shouldReturnBook_whenBookExists() {
        // Given
        Book book = new Book("123", "Test Title", "Test Author");
        when(bookService.findByIsbn("123")).thenReturn(Mono.just(book));

        // When & Then
        webTestClient.get().uri("/api/books/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class)
                .isEqualTo(book);
    }

    @Test
    void getBookById_shouldReturn404_whenBookDoesNotExist() {
        // Given
        when(bookService.findByIsbn("999")).thenReturn(Mono.empty());

        // When & Then
        webTestClient.get().uri("/api/books/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
```

In these tests:
1. We use `@WebFluxTest` to set up a minimal context for testing our reactive controller
2. We inject a `WebTestClient` that we can use to make requests to our controller
3. We mock the service that our controller depends on
4. We make requests to our controller's endpoints
5. We use the fluent API to assert on the responses

This approach allows us to test our reactive controllers in isolation from the service layer, focusing on the controller's responsibility of handling HTTP requests and returning appropriate responses.

## Chapter 6: Common Pitfalls and Best Practices

### Common Testing Pitfalls

Even with Spring Boot's excellent testing support, there are several common pitfalls that developers can fall into when testing Spring Boot applications. Being aware of these pitfalls can help you write more effective tests and avoid frustrating debugging sessions.

#### Pitfall 1: Overusing @SpringBootTest

One common pitfall is using `@SpringBootTest` for all tests, even those that only need to test a small part of the application. This leads to slow tests, as the entire application context needs to be loaded for each test class.

Instead, try to use the most focused test slice annotation that meets your needs:
- For testing controllers, use `@WebMvcTest`
- For testing repositories, use `@DataJpaTest`
- For testing JSON serialization/deserialization, use `@JsonTest`
- Only use `@SpringBootTest` when you need to test the interaction between multiple components

#### Pitfall 2: Ignoring Test Context Caching

The Spring Test Context Framework caches application contexts between tests to avoid the overhead of starting a new context for each test class. However, if you're not careful, you can inadvertently prevent this caching from being effective:

- Inconsistent use of `@MockBean` and `@SpyBean` across test classes
- Unnecessarily using `@DirtiesContext`
- Using different context configurations for tests that could share a context

To optimize test context caching:
- Group tests with similar context configurations together
- Be consistent with your use of `@MockBean` and `@SpyBean`
- Avoid `@DirtiesContext` when possible
- Use transactions for test isolation instead of clearing the database

#### Pitfall 3: Poor Test Data Management

Managing test data is a challenge in integration tests. Common pitfalls include:

- Leaving test data in the database after tests
- Relying on specific database state that might be changed by other tests
- Using production data for tests
- Hardcoding database credentials in tests

To avoid these pitfalls:
- Use `@Transactional` for test isolation
- Set up test data programmatically or with scripts
- Use in-memory databases or TestContainers for isolation
- Externalize database configuration

#### Pitfall 4: Ignoring Exception Handling

It's easy to focus on the happy path and forget to test how your application handles exceptions. Make sure to test:

- Validation errors
- Not found scenarios
- Permission errors
- External service failures
- Timeout scenarios

For each of these, verify that your application returns appropriate error responses and handles the exceptions gracefully.

#### Pitfall 5: Testing Implementation Details

Another common pitfall is testing implementation details rather than behavior. This leads to fragile tests that break when the implementation changes, even if the behavior remains the same.

Instead, focus on testing the observable behavior of your components:
- For controllers, test the HTTP requests and responses
- For services, test the business logic outcomes
- For repositories, test the query results

This approach makes your tests more robust and allows your implementation to evolve without breaking tests.

### Best Practices for Spring Boot Testing

Now that we've covered the common pitfalls, let's look at some best practices for testing Spring Boot applications.

#### Practice 1: Follow the Testing Pyramid

Structure your tests according to the testing pyramid:
- Many unit tests for business logic and individual components
- Fewer integration tests for component interactions
- Even fewer end-to-end tests for critical user flows

This approach gives you a balance of speed, reliability, and coverage.

#### Practice 2: Use Appropriate Test Slice Annotations

Choose the right test slice annotation for each test:
- `@WebMvcTest` for controllers
- `@DataJpaTest` for repositories
- `@JsonTest` for JSON serialization/deserialization
- `@RestClientTest` for REST clients
- `@WebFluxTest` for reactive controllers
- `@SpringBootTest` for integration tests

Using the appropriate annotation keeps your tests focused and fast.

#### Practice 3: Organize Tests Clearly

Organize your tests for clarity and maintainability:
- Use descriptive test names that explain what the test is verifying
- Group related tests in nested classes using JUnit 5's `@Nested`
- Follow a consistent structure within test methods (e.g., Given-When-Then)
- Comment complex test setup or assertions

#### Practice 4: Use AssertJ for Readable Assertions

AssertJ provides a fluent API for assertions that makes your tests more readable:

```java
// Using JUnit assertions
assertEquals(3, books.size());
assertEquals("123", books.get(0).getIsbn());
assertTrue(books.get(0).getTitle().contains("Test"));

// Using AssertJ
assertThat(books)
    .hasSize(3)
    .extracting(Book::getIsbn)
    .containsExactly("123", "456", "789");

assertThat(books.get(0).getTitle())
    .contains("Test");
```

The AssertJ version is more readable and provides better error messages when assertions fail.

#### Practice 5: Write Self-Contained Tests

Each test should be self-contained and independent of other tests:
- Set up the necessary test data in each test or `@BeforeEach` method
- Use `@Transactional` for test isolation
- Don't assume a specific database state
- Avoid shared mutable state between tests

This approach makes your tests more reliable and easier to debug when they fail.

#### Practice 6: Mock External Dependencies

Mock external dependencies to make your tests more reliable and focused:
- Use `@MockBean` for Spring components
- Use Mockito's `@Mock` for regular classes
- Use WireMock for external HTTP services
- Use TestContainers for external databases

This approach isolates your tests from external factors that might cause flakiness.

#### Practice 7: Test Edge Cases and Error Scenarios

Don't just test the happy path. Make sure to test:
- Boundary conditions
- Invalid inputs
- Error responses from external services
- Resource constraints (e.g., timeouts, memory limits)

These tests ensure that your application handles exceptional situations gracefully.

## Chapter 7: Advanced Testing Techniques

### Testing Asynchronous and Reactive Code

Modern Spring Boot applications often use asynchronous or reactive programming to improve performance and scalability. Testing asynchronous and reactive code requires special techniques.

#### Testing CompletableFuture

For asynchronous methods that return `CompletableFuture`:

```java
@Test
void processAsync_shouldCompleteSuccessfully() throws Exception {
    // When
    CompletableFuture<String> future = service.processAsync("test");

    // Then
    String result = future.get(1, TimeUnit.SECONDS); // Wait for completion with timeout
    assertEquals("processed:test", result);
}
```

Key points for testing `CompletableFuture`:
- Use `future.get()` with a timeout to avoid hanging tests
- Catch and assert on specific exceptions that might be completed exceptionally
- Consider using `CompletableFuture.allOf()` for testing multiple futures

#### Testing Reactive Types (Mono/Flux)

For reactive methods that return `Mono` or `Flux`:

```java
@Test
void findByIsbn_shouldReturnBook_whenBookExists() {
    // Given
    Book book = new Book("123", "Test Title", "Test Author");
    when(bookRepository.findById("123")).thenReturn(Mono.just(book));

    // When
    Mono<Book> result = bookService.findByIsbn("123");

    // Then
    StepVerifier.create(result)
        .expectNext(book)
        .verifyComplete();
}

@Test
void findAllBooks_shouldReturnBooks() {
    // Given
    Book book1 = new Book("123", "Title 1", "Author 1");
    Book book2 = new Book("456", "Title 2", "Author 2");
    when(bookRepository.findAll()).thenReturn(Flux.just(book1, book2));

    // When
    Flux<Book> result = bookService.findAllBooks();

    // Then
    StepVerifier.create(result)
        .expectNext(book1)
        .expectNext(book2)
        .verifyComplete();
}
```

Key points for testing reactive types:
- Use `StepVerifier` from the `reactor-test` library
- Verify both successful completion and error scenarios
- Test backpressure behavior if relevant
- Use `StepVerifier.withVirtualTime()` for testing time-based operations

### Advanced Mockito Techniques

Mockito provides several advanced features that can be useful for testing complex scenarios.

#### Argument Matchers and Captures

For testing methods where the exact arguments are not known in advance:

```java
@Test
void saveBook_shouldSetCreatedDate() {
    // Given
    Book book = new Book("123", "Test Title", "Test Author");
    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);

    // When
    bookService.saveBook(book);

    // Then
    verify(bookRepository).save(bookCaptor.capture());
    Book savedBook = bookCaptor.getValue();
    assertThat(savedBook.getCreatedDate()).isNotNull();
}
```

Key points for argument captures:
- Use `ArgumentCaptor` to capture arguments passed to mock methods
- Verify properties of the captured arguments
- Use `ArgumentMatchers` (`any()`, `eq()`, etc.) for flexible matching

#### Stubbing Consecutive Calls

For testing methods that are called multiple times with different results:

```java
@Test
void processBooks_shouldHandleSuccess_thenFailure() {
    // Given
    when(bookRepository.findById("123"))
        .thenReturn(Optional.of(new Book("123", "Title", "Author")))
        .thenReturn(Optional.empty());

    // When & Then
    assertTrue(bookService.processBook("123")); // First call succeeds
    assertFalse(bookService.processBook("123")); // Second call fails
}
```

#### Stubbing Exceptions

For testing error handling:

```java
@Test
void findByIsbn_shouldHandleRepositoryException() {
    // Given
    when(bookRepository.findById("error"))
        .thenThrow(new DataAccessException("Database error") {});

    // When & Then
    assertThrows(ServiceException.class, () -> {
        bookService.findByIsbn("error");
    });
}
```

#### Partial Mocks with Spy

For testing methods while preserving some real behavior:

```java
@Test
void calculateLateFee_shouldUseRealCalculation() {
    // Given
    BookService serviceSpy = spy(new BookService(bookRepository));
    Book book = new Book("123", "Test Title", "Test Author");
    when(serviceSpy.findByIsbn("123")).thenReturn(Optional.of(book));

    // When
    double fee = serviceSpy.calculateLateFeeByIsbn("123", 5);

    // Then
    assertEquals(2.5, fee); // 5 days * 0.5 per day
}
```

Key points for using spies:
- Use spies when you want to test some real methods while mocking others
- Be careful with spies as they can lead to unexpected behavior
- Prefer constructor injection over field injection for easier spy creation

### Testing with Custom Annotations

Custom annotations can help reduce boilerplate code and enforce testing patterns. Here's an example of a custom annotation for TestContainers:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public @interface PostgresIntegrationTest {
}
```

Now we can use this annotation instead of repeating the same combination of annotations:

```java
@PostgresIntegrationTest
class BookIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // Tests using PostgreSQL
}
```

Custom annotations can make your tests more consistent and easier to maintain, especially for common testing patterns in your application.

## Chapter 8: Testing in the Development Workflow

### Test-Driven Development with Spring Boot

Test-Driven Development (TDD) is a development process where you write tests before implementing the code. The basic cycle is:

1. Write a failing test
2. Implement the code to make the test pass
3. Refactor the code while keeping the test passing

TDD can be particularly effective with Spring Boot, as it helps ensure that your components behave correctly from the start.

Here's an example of TDD for a new feature in a Spring Boot application:

```java
// Step 1: Write a failing test
@Test
void markBookAsLost_shouldUpdateStatus_andCalculateFee() {
    // Given
    Book book = new Book("123", "Test Title", "Test Author");
    book.setStatus(BookStatus.BORROWED);
    book.setPrice(20.0);
    when(bookRepository.findById("123")).thenReturn(Optional.of(book));

    // When
    LostBookResult result = bookService.markBookAsLost("123");

    // Then
    assertEquals(BookStatus.LOST, result.getBook().getStatus());
    assertEquals(20.0, result.getReplacementFee());
    verify(bookRepository).save(book);
}
```

After writing this test, you would implement the `markBookAsLost` method to make the test pass:

```java
public LostBookResult markBookAsLost(String isbn) {
    Book book = findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

    book.setStatus(BookStatus.LOST);
    bookRepository.save(book);

    double replacementFee = book.getPrice();

    return new LostBookResult(book, replacementFee);
}
```

Finally, you might refactor the code while keeping the test passing, for example by extracting a fee calculation method:

```java
public LostBookResult markBookAsLost(String isbn) {
    Book book = findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

    book.setStatus(BookStatus.LOST);
    bookRepository.save(book);

    double replacementFee = calculateReplacementFee(book);

    return new LostBookResult(book, replacementFee);
}

private double calculateReplacementFee(Book book) {
    return book.getPrice();
}
```

TDD helps ensure that your code is testable from the start and that your tests are meaningful (they were written to verify specific behaviors that you intend to implement).

### Continuous Integration and Testing

Integrating tests into your Continuous Integration (CI) pipeline ensures that tests are run automatically whenever changes are pushed to your code repository. This provides fast feedback on whether your changes have broken anything.

Here's a simple GitHub Actions workflow for a Spring Boot application:

```yaml
name: Java CI with Maven

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v2

    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'

    - name: Build with Maven
      run: mvn -B test

    - name: Integration Tests
      run: mvn -B verify -P integration-tests
```

This workflow runs unit tests with `mvn test` and integration tests with `mvn verify -P integration-tests` (assuming you have set up a Maven profile for integration tests).

Key points for CI testing:
- Run unit tests frequently (on every push)
- Run integration tests at least once before merging changes
- Configure your tests to use CI-friendly databases (in-memory or containers)
- Save test results and coverage reports as artifacts
- Configure notifications for test failures

### Test Coverage and Quality Metrics

Test coverage measures how much of your code is executed by your tests. While 100% coverage doesn't guarantee bug-free code, low coverage might indicate areas that aren't being tested adequately.

You can measure test coverage in a Spring Boot application using tools like JaCoCo:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Beyond coverage, other quality metrics to consider include:
- Test maintainability (Are tests easy to understand and update?)
- Test robustness (Do tests fail only when the code is broken?)
- Test performance (Do tests run quickly enough for frequent execution?)
- Test isolation (Do tests depend on each other or external factors?)

## Conclusion

Testing Spring Boot applications doesn't have to be intimidating or burdensome. With the right approach and knowledge of Spring Boot's testing support, you can create a test suite that provides confidence, serves as living documentation, and enables rapid development.

Throughout this book, we've explored various aspects of Spring Boot testing:
- The foundation of JUnit 5 and Mockito
- Sliced testing with Spring Boot's test annotations
- Integration testing with @SpringBootTest
- Testing web clients and external services
- Common pitfalls and best practices
- Advanced testing techniques
- Incorporating testing into your development workflow

Remember that the goal of testing is not to achieve some arbitrary coverage metric or to check a box in your development process. The goal is to build robust, maintainable software that meets its requirements and can evolve over time.

By adopting a thoughtful, strategic approach to testing, you can achieve this goal while making testing a natural, integrated part of your development process. May your tests be green, your deployments be confident, and your users be happy!
