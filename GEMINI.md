# GEMINI.md

This file provides guidance to Gemini when working with code in this repository.

## Project Overview

This is a Spring Boot bulletin board application built with Java 21 and Spring Boot 3.x. The project emphasizes a clean,
domain-driven architecture with a clear separation of layers. It utilizes MySQL for the database and Redis for session
management. A strong focus is placed on code quality, with a comprehensive suite of tools including Checkstyle, PMD,
SpotBugs, JaCoCo, SonarCloud, and ErrorProne.

## Build & Run Commands

### Building the Project

- **Full build with quality checks:**
  ```bash
  ./gradlew build
  ```
- **Build without running tests:**
  ```bash
  ./gradlew build -x test
  ```
- **Clean and build:**
  ```bash
  ./gradlew clean build
  ```

### Running the Application

- **Run with the `dev` profile (default):**
  ```bash
  ./gradlew bootRun
  ```
- **Run with a specific profile (e.g., `prod`):**
  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=prod'
  ```

### Running Tests

- **Run all tests:**
  ```bash
  ./gradlew test
  ```
- **Run tests in a specific package:**
  ```bash
  ./gradlew test --tests "bunny.boardhole.board.*"
  ```
- **Run a single test class:**
  ```bash
  ./gradlew test --tests BoardControllerTest
  ```

### Quality Checks

- **Run all quality checks (PMD, SpotBugs, Checkstyle, Coverage):**
  ```bash
  ./gradlew qualityCheck
  ```
- **Generate code coverage report:**
  ```bash
  ./gradlew jacocoTestReport
  ```

### Docker Services

- **Start MySQL and Redis containers:**
  ```bash
  docker-compose up -d
  ```
- **Stop containers:**
  ```bash
  docker-compose down
  ```

## Architecture & Development Conventions

### Layered Architecture

The project follows a strict layered architecture:

`presentation` → `application` → `domain` → `infrastructure`

- **Presentation:** Handles web requests, DTOs, and API endpoints.
- **Application:** Contains business logic, commands, queries, and services.
- **Domain:** Core domain objects, entities, and validation rules.
- **Infrastructure:** Implements repositories, external services, and persistence.

### Key Design Patterns

- **Command/Query Responsibility Segregation (CQRS-lite):** Write operations are handled by `Commands` and read
  operations by `Queries` to separate concerns.
- **Two-Layer DTO Mapping:** MapStruct is used for mapping between layers:
    1. `Entity` ↔ `Result` (in the application layer)
    2. `Result` ↔ `Response`/`Command` (in the presentation layer)
- **Custom Validation Annotations:** The `domain.validation` package contains custom annotations for create (`@Valid*`)
  and update (`@Optional*`) operations.

### Code Style

- **Null Safety:** All packages are annotated with `@NullMarked` using `package-info.java` files, which are
  automatically generated.
- **Lombok:** Used to reduce boilerplate code.
- **MapStruct:** Used for object mapping.

### API and Error Handling

- **API Paths:** All REST APIs are versioned and located under `/api/v1/`.
- **Error Handling:** Follows RFC 7807 Problem Details standard, with a `GlobalExceptionHandler` for centralized error
  management.
