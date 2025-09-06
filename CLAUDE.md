# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 게시판 애플리케이션 with:

- Java 21, Spring Boot 3.5.5
- MySQL (Docker) + Redis (Session)
- Domain-driven architecture with clear layer separation
- Quality tooling: JaCoCo (coverage), SonarCloud integration

## Build & Run Commands

```bash
# Build
./gradlew build              # Full build with quality checks
./gradlew build -x test      # Build without tests
./gradlew clean build        # Clean build

# Run 
./gradlew bootRun            # Run with dev profile (default)
./gradlew bootRun --args='--spring.profiles.active=prod'  # Run with prod profile

# Test
./gradlew test               # Run all tests
./gradlew test --tests "bunny.boardhole.board.*"  # Run specific package tests
./gradlew test --tests BoardControllerTest         # Run single test class

# Quality Checks
./gradlew jacocoTestReport   # Generate coverage report
./gradlew sonarAnalysis      # Run SonarCloud analysis with coverage

# Database & Redis (via Docker Compose)
docker-compose up -d         # Start MySQL (port 13306) & Redis (port 16379)
docker-compose down          # Stop containers
```

## IntelliJ IDEA Setup

### Code Formatting & Import Organization

This project uses **IntelliJ IDEA code style settings** instead of Spotless for consistent formatting.

**Required Setup (All Team Members)**:

1. **Open Project**: IntelliJ will automatically detect `.idea/codeStyles/` settings
2. **Enable Auto Actions on Save**:
    - Go to Settings → Tools → Actions on Save (Ctrl/Cmd + Alt + S)
    - Enable:
        - ✅ **Reformat code**
        - ✅ **Optimize imports**
        - ✅ **Rearrange code** (optional)

**Benefits over Spotless**:

- 🚀 Faster builds (no formatting check overhead)
- 💡 Real-time formatting while typing
- 🎯 Perfect IDE integration (no conflicts)
- 🔧 More advanced formatting options available

## Architecture & Structure

### Layer Architecture

```
presentation → application → domain → infrastructure
         ↓           ↓          ↓           ↓
      (DTOs)    (Commands)  (Entities) (Repository)
```

### Domain Structure Pattern

Each domain follows consistent structure:

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

### Key Design Patterns

1. **Command/Query Separation (CQRS-lite)**
    - Commands for write operations
    - Direct queries for reads
    - Clear separation of concerns

2. **Validation Annotations**
    - `@Valid*` for required fields (creation)
    - `@Optional*` for nullable fields (updates)
    - Centralized in domain.validation package

3. **MapStruct Two-Layer Mapping**
    - Application layer: Entity ↔ Result
    - Presentation layer: Result ↔ Response/Command
    - Clean separation between layers

4. **Security Architecture**
    - Session-based auth with Redis
    - Method-level security with @PreAuthorize
    - Custom AppUserPrincipal for user context

5. **Event-Driven Features**
    - @EventListener for async processing
    - Example: ViewedEvent for board view count

## Important Conventions

### Code Style

- All packages have @NullMarked package-info.java (auto-generated)
- Lombok for boilerplate reduction
- MapStruct for mapping

### Testing Structure

- Unit tests: Mock dependencies
- Integration tests: @SpringBootTest with real DB/Redis
- Architecture tests: ArchUnit for layer compliance
- Test naming: [Method]_[Condition]_[Expected]

### Configuration

- application.yml: Common config
- application-dev.yml: Development (Docker services)
- application-prod.yml: Production settings
- boardhole.* properties for domain config

### API Paths

All REST APIs under `/api/v1/*`:

- `/api/v1/auth/*` - Authentication
- `/api/v1/users/*` - User management
- `/api/v1/boards/*` - Board operations

### Error Handling

- RFC 7807 Problem Details format
- GlobalExceptionHandler for centralized handling
- Custom exceptions extend base types
- Consistent error response structure

## Quality Standards

- Code coverage: ≥60% (current ~64%)
- SonarCloud: Continuous analysis
- IntelliJ IDEA: Built-in inspections and code analysis

## Environment Setup

Docker services required:

- MySQL: localhost:13306 (root/root)
- Redis: localhost:16379

Spring profiles:

- `dev` (default): Auto-DDL, SQL logging, debug enabled
- `prod`: Optimized settings, JSON logging

## Key Dependencies

- Spring Boot Starters: web, data-jpa, validation, aop, security, mail
- Session: spring-session-data-redis
- Documentation: SpringDoc OpenAPI
- Mapping: MapStruct 1.6.3
- Testing: JUnit 5, Testcontainers, ArchUnit
- Quality: JaCoCo (coverage)
- 코드를 수정하고 주석을 남기지마라 , 특히 무엇무엇슥 삭제햇습니다.