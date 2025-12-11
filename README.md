# Testing Spring Boot Applications Demystified

<p align="center">
  <img src="ebook-cover.png" width="400" alt="Testing Spring Boot Applications Demystified Cover">
</p>

This repository contains the source code for the book [Testing Spring Boot Applications Demystified](https://leanpub.com/testing-spring-boot-applications-demystified).

The book provides comprehensive guidance on testing Spring Boot applications, covering everything from basic unit tests to advanced integration testing strategies with Testcontainers, WireMock, and Spring Security Test.

## Get the Missing Spring Boot Testing Manual for Free

Write code that works, scale without breaking, and deploy daily.

- **Stop guessing**: Learn to write tests that actually catch bugs and make you more productive.
- **Sleep better**: Eliminate deployment anxiety and refactor fearlessly.
- **Ship faster**: Copy-paste recipes for JUnit, Testcontainers, WireMock and Spring Boot.

Sign up below to get access to your 120+ Pages Manual for free 👇🏻

[Testing Spring Boot Applications Demystified](https://rieckpil.de/free-spring-boot-testing-book/?utm_source=github&utm_medium=free-download&utm_id=free-lead-magnet)

## Requirements

Before you begin, ensure you have the following installed:

- **Java 21**
  - You can use [SDKMAN!](https://sdkman.io/) to install Java: `sdk install java 21.0.6-tem`
  - The project includes a `.sdkmanrc` file for automatic version switching
- **Maven 3.6+** (wrapper included in the project)
- **Docker** (for running PostgreSQL via Docker Compose and Testcontainers during tests)
- **Git** (for cloning the repository)

## Technologies Used

This project demonstrates testing techniques for a Spring Boot application that includes:

- **Spring Boot 3** with Java 21
- **Spring Data JPA** with PostgreSQL
- **Spring Security** for authentication and authorization
- **Spring WebFlux** for reactive HTTP clients
- **Thymeleaf** for server-side templating
- **Flyway** for database migrations
- **Testcontainers** for integration testing
- **WireMock** for HTTP client mocking
- **JUnit 5** as the testing framework
- **AssertJ** for fluent assertions

## Building the Application

The project uses Maven as the build tool. You can use the Maven wrapper included in the project:

### Compile the project

```bash
./mvnw compile
```

### Build the application (skip tests)

```bash
./mvnw package -DskipTests
```

### Build the application (with tests)

```bash
./mvnw verify
```

## Running the Application

### Using Docker Compose

The project includes Spring Boot Docker Compose support. Simply start the application, and Docker Compose will automatically start the PostgreSQL database:

```bash
./mvnw spring-boot:run
```

The PostgreSQL database will be started automatically using the configuration from `compose.yml`.

### Manual Database Setup

Alternatively, you can start PostgreSQL manually using Docker Compose:

```bash
docker compose up -d
```

Then run the application:

```bash
./mvnw spring-boot:run
```

## Running Tests

The project includes comprehensive test coverage with both unit and integration tests.

### Run all tests

```bash
./mvnw verify
```

### Run only unit tests

```bash
./mvnw test
```

### Run only integration tests

```bash
./mvnw failsafe:integration-test
```

### Test Configuration

- **Unit Tests**: Use Surefire plugin with parallel execution enabled
- **Integration Tests**: Use Failsafe plugin and follow the naming convention `*IT.java` or `*WT.java`
- **Testcontainers**: Automatically starts PostgreSQL containers for integration tests
- **Context Caching**: Examples included to optimize Spring context reuse across tests

## Code Quality

The project enforces code quality standards:

- **Spotless**: Ensures consistent code formatting using Google Java Format
  ```bash
  ./mvnw spotless:check
  ./mvnw spotless:apply
  ```

- **SortPOM**: Keeps the `pom.xml` organized
  ```bash
  ./mvnw sortpom:verify
  ```

- **Maven Enforcer**: Validates dependency convergence

## Key Testing Patterns Demonstrated

This codebase showcases various testing patterns covered in the book:

- Unit testing with JUnit 5 and Mockito
- Integration testing with `@SpringBootTest`
- Testing Spring Data JPA repositories
- Testing REST controllers with MockMvc
- Testing reactive WebClient with WireMock
- Spring Security testing techniques
- Optimizing Spring context caching
- Parallel test execution
- Testcontainers for realistic integration tests

## License

The code in this repository is available for educational purposes as companion material for the book.

Testing is a team sport, so feel free to share the link to get the eBook copy of [Testing Spring Boot Applications Demystified](https://rieckpil.de/free-spring-boot-testing-book/?utm_source=github&utm_medium=free-download&utm_id=free-lead-magnet) with your colleagues.

---

**Joyful Testing!**
