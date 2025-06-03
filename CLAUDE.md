# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot testing demonstration project consisting of:

1. A sample "Shelfie" bookshelf application in `/application`
2. A book manuscript about Spring Boot testing in `/manuscript`

The book manuscript is a lead magnet for my Spring Boot Testing blog. The old book is part of `manuscript/archive`, which is no longer maintained. Please help me write a new, more structured book using the input in `manuscript/input`. The book should be around 80-100 pages long, with a high density of information.

The new strucutre is in `manuscript/Book.txt`.

Provide high density of information, with a focus on practical examples and best practices for testing Spring Boot applications. The book should be suitable for developers who want to improve their testing skills and learn about the latest features in Spring Boot. But also make sure to soft teach and soft pitch the Masterclass and the newsletter.

## Book Conventions

- Favor us/we over you/your
- Use always two spaces for indentation of code
- Don't add large code blocks at once, but rather small snippets with explanations
- It's Testcontainers not Test Containers or TestContainers
- Code snippets can only contain 5-10 lines of code, split larger snippets into smaller parts
- Add text before and after code snippets to explain what the code does

## Common Commands

### Application Development

```bash
# Run tests
./mvnw clean test              # Unit tests only
./mvnw clean verify            # All tests including integration tests
./mvnw test -Dtest=ClassName  # Run specific test class

# Run application
./mvnw spring-boot:run

# Code formatting
./mvnw spotless:apply

# Start PostgreSQL locally
docker compose up -d
```

### Book Building
```bash
./convert-to-pandoc.sh && ./create-pdf-book.sh
```

## Architecture

### Application Structure
The Shelfie application follows a standard Spring Boot MVC pattern:
- **Domain**: `Book`, `User` entities
- **Data**: `BookRepository` (Spring Data JPA)
- **Service**: `BookshelfService` (business logic)
- **Controller**: `BookshelfController` (REST/MVC endpoints)
- **UI**: Thymeleaf templates with layout dialect
- **Database**: PostgreSQL with Flyway migrations

### Testing Strategy
- **Unit Tests**: Run with Surefire plugin (parallel execution enabled)
- **Integration Tests**: `*IT.java` pattern, run with Failsafe plugin
- **Test Containers**: PostgreSQL integration using `@ServiceConnection`
- **Test Application**: Separate main class for running with test dependencies

### Key Testing Dependencies
- `spring-boot-starter-test`: Core testing support
- `spring-boot-testcontainers`: Test container integration
- `testcontainers` (JUnit Jupiter + PostgreSQL): Container management

## Important Configuration

- **Java Version**: 21
- **Spring Boot**: 3.2.2
- **Code Style**: Enforced via Spotless (see `spotless.importorder`)
- **Database**: PostgreSQL (via Docker Compose or Testcontainers)
- **Migrations**: Flyway scripts in `src/main/resources/db/migration/`
