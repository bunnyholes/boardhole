# CLAUDE.md

Technical specifications for Claude Code AI assistant when working with this Spring Boot board application.

## Project Overview

Spring Boot 3.5.5 board application with Java 21, MySQL 8.4, Redis session storage, domain-driven architecture, and
comprehensive quality tooling.

## Build Commands

```bash
# Build
./gradlew build              # Full build with quality checks
./gradlew build -x test      # Build without tests
./gradlew clean build        # Clean build

# Run
./gradlew bootRun            # Run with dev profile (default)
./gradlew bootRun --args='--spring.profiles.active=prod'

# Test
./gradlew test               # All tests (unit + e2e)
./gradlew e2eTest           # @Tag("e2e") tests only

# Quality
./gradlew jacocoTestReport   # Generate coverage report
./gradlew sonarAnalysis      # Run SonarCloud analysis
```

## Architecture

### Layer Structure

```
presentation → application → domain → infrastructure
         ↓           ↓          ↓           ↓
      (DTOs)    (Commands)  (Entities) (Repository)
```

### Domain Pattern

```
bunny.boardhole.[domain]/
├── application/
│   ├── command/     # Write operations (Commands + CommandService)
│   ├── query/       # Read operations (QueryService)
│   ├── mapper/      # MapStruct mappers (Entity ↔ Result)
│   └── result/      # Internal DTOs
├── domain/
│   ├── [Entity].java
│   └── validation/
│       ├── required/ # @Valid* annotations for creation
│       └── optional/ # @Optional* annotations for updates
├── infrastructure/
│   └── [Repository].java
└── presentation/
    ├── [Controller].java
    ├── dto/         # Request/Response DTOs
    └── mapper/      # MapStruct mappers (Result ↔ Response)
```

## Key Patterns

1. **CQRS-lite**: Commands for writes, direct queries for reads
2. **Validation**: @Valid* (required fields), @Optional* (nullable fields)
3. **MapStruct**: Two-layer mapping (Application: Entity ↔ Result, Presentation: Result ↔ Response)
4. **Security**: Session-based auth with Redis, method-level @PreAuthorize, AppUserPrincipal
5. **Events**: @EventListener for async processing (ViewedEvent example)

## Code Conventions

- All packages have @NullMarked package-info.java
- Lombok for boilerplate reduction
- MapStruct for mapping
- Import statements instead of full package paths
- For class name conflicts: import frequently used, use full path for less frequent

## Testing Structure

- **Unit**: Mock dependencies, fast execution
- **E2E**: Full system tests with real DB/Redis and RestAssured, @Tag("e2e")
- **Architecture**: ArchUnit for layer compliance
- **Naming**: [Method]_[Condition]_[Expected]

## API Paths

All REST APIs under `/api/*`:

- `/api/auth/*` - Authentication
- `/api/users/*` - User management
- `/api/boards/*` - Board operations

## Environment

Docker services (Spring Boot Docker Compose auto-start):

- MySQL: Dynamic port mapping
- Redis: Dynamic port mapping

Profiles:

- `dev` (default): Auto-DDL, SQL logging, debug
- `prod`: Optimized settings, JSON logging

## Dependencies

- Spring Boot Starters: web, data-jpa, validation, aop, security, mail, thymeleaf
- Session: spring-session-data-redis
- Documentation: SpringDoc OpenAPI 2.8.12
- Mapping: MapStruct 1.6.3
- Testing: JUnit 5, RestAssured 5.5.0, Testcontainers, ArchUnit 1.4.1
- Quality: JaCoCo 0.8.12
- Null Safety: JSpecify 1.0.0

## Quality Standards

- Code coverage: ≥60%
- SonarCloud integration
- IntelliJ IDEA inspections
- RFC 7807 Problem Details for errors
- GlobalExceptionHandler for centralized error handling
- mockbean 사용 금지 반드시 mokitoBean을 사용