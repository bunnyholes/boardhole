ㅊ# Repository Guidelines

## Project Structure & Modules

- Java 21 + Spring Boot 3 (Gradle).
- Source: `src/main/java/dev/xiyo/bunnyholes/boardhole/**` organized by domain: `auth`, `board`, `user`, `shared`, `web`.
- Layers per domain: `presentation` → `application` → `domain` → `infrastructure`.
- Web assets: `src/main/resources/static/assets/**`.
- Tests: `src/test/java/dev/xiyo/bunnyholes/boardhole/**` (JUnit 5, H2 in-memory DB, MockMvc).

## Build, Test, Run

- Build JAR + run tests: `./gradlew clean build`
- Run locally (auto-starts PostgreSQL/Redis via Spring Boot Docker Compose if Docker is running):
    - `./gradlew bootRun` → http://localhost:8080
- Unit/MVC tests:
    - All tests: `./gradlew test`
    - Filter: `./gradlew test --tests "*ControllerTest"`
- Package executable JAR: `./gradlew bootJar` (output in `build/libs/`).
- Stop dev containers: `docker-compose down` (or `down -v` to remove volumes).

## Coding Style & Naming

- Use IntelliJ project code style (`.idea/codeStyles/*`), 4-space indentation.
- Packages: `lower.case`; Classes/Enums: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Suffixes by role: `*Controller`, `*Service`, `*Repository`, `*Mapper` (MapStruct), DTO: `*Request`/`*Response`, tests:
  `*Test`.
- Prefer constructor injection and immutability where practical.

## Testing Guidelines

- Frameworks: JUnit 5, Spring Boot Test, H2 in-memory database, MockMvc for controller tests.
- No local DB needed for tests; containers start automatically.
- Name tests by unit under test and behavior, e.g., `BoardControllerTest`, `UserControllerTest`.
- Run locally before pushing: `./gradlew test`.

## Commit & PR Guidelines

- Conventional Commits (see `.gitmessage`):
    - `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `perf:`, `ci:`, `build:`, `revert:`; use `!` for
      breaking changes.
    - Example: `feat(board): add search by title`.
- Branch names: `feature/<topic>`, `fix/<issue-id>`.
- PRs must include:
    - Clear description, linked issues (`Closes #123`), and any UI/API screenshots or sample requests.
    - Proof of passing CI (build + tests) and no critical Qodana findings.

## Security & Config

- Default demo users load on startup; override credentials in `application-*.yml` or env vars (e.g.,
  `BOARDHOLE_DEFAULT_USERS_ADMIN_PASSWORD`).
- Do not commit secrets. Prefer environment overrides for prod.
