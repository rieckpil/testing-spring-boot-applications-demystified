# Testing Spring Boot Applications Demystified 🍃

<p align="center">
  <img src="resources/tsbad-beginning.png" height="600" alt="Spring Boot Testing Maze Entry">
</p>

This repository contains the source code for the book [Testing Spring Boot Applications Demystified](https://rieckpil.de/free-spring-boot-testing-book/?utm_source=github&utm_medium=free-download&utm_id=free-lead-magnet).

Spring Boot testing doesn't have to be a **nightmare** of broken configurations and unreliable tests.

This book transforms testing from your biggest frustration into your **secret** **weapon**, with proven strategies that work in real projects and examples you can implement today.

Demystify testing **real-world Spring Boot applications** to deliver robust & maintainable code with **confidence**.

## Get the Missing Spring Boot Testing Manual for Free

Write code that works, scale without breaking, and deploy daily.

- **Stop guessing**: Learn to write tests that actually catch bugs and make you more productive.
- **Sleep better**: Eliminate deployment anxiety and refactor fearlessly.
- **Ship faster**: Copy-paste recipes for JUnit, Testcontainers, WireMock and Spring Boot.

Sign up below to get access to your 120+ Pages Manual for free 👇🏻

[» Testing Spring Boot Applications Demystified](https://rieckpil.de/free-spring-boot-testing-book/?utm_source=github&utm_medium=free-download&utm_id=free-lead-magnet)

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

- **Spring Boot 4** with Java 21
- **Spring Data JPA** with PostgreSQL
- **Spring Security** for authentication and authorization
- **Spring WebFlux** for reactive HTTP clients
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

### Build the application (with tests)

```bash
./mvnw verify
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
./mvnw failsafe:integration-test failsafe:verfify
```

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

**Joyful Testing!**
