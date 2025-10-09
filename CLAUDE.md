# CLAUDE.md

Technical specifications for Claude Code AI assistant when working with this Spring Boot board application.

## Project Overview

Spring Boot 3.5.5 board application with Java 21, PostgreSQL 17, Redis session storage, domain-driven architecture, and
comprehensive quality tooling.

## Build Commands

```bash
# Build
./gradlew build              # Full build with quality checks
./gradlew build -x test      # Build without tests
./gradlew clean build        # Clean build

# Run
./gradlew bootRun            # Run with dev profile (default)

# Test
./gradlew test               # All tests

# Quality
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
dev/xiyo/bunnyholes/boardhole/[domain]/
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

`web` 도메인은 서버 렌더링 뷰와 정적 리소스를 다루며 `presentation`과 `view` 하위 디렉터리로 구성됩니다.

## Key Patterns

1. **CQRS-lite**: Commands for writes, direct queries for reads
2. **Validation**: @Valid* (required fields), @Optional* (nullable fields)
3. **MapStruct**: Two-layer mapping (Application: Entity ↔ Result, Presentation: Result ↔ Response)
4. **Security**: Session-based auth with Redis, method-level @PreAuthorize, AppUserPrincipal
5. **Events**: @EventListener for async processing (ViewedEvent example)

## 🚨 프로젝트 개발 규칙 및 제약사항

### 상수 사용 규칙

- **하드코딩 금지**: 모든 숫자, 문자열 리터럴은 상수로 정의
- **검증 상수**: `*ValidationConstants` 클래스에 정의
    - `UserValidationConstants`: 사용자 관련 검증 상수
    - `BoardValidationConstants`: 게시판 관련 검증 상수
    - `SchemaConstants`: 데이터베이스 스키마 관련 상수
- **API 경로**: `ApiPaths` 클래스에 정의, 컨트롤러에서 재사용
- **로그 관련**: `LogConstants` 클래스에 색상, 아이콘, 키 정의
- **상수 클래스 패턴**:
  ```java
  @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
  public final class [Domain]ValidationConstants {
      public static final int FIELD_MAX_LENGTH = 100;
  }
  ```

### 국제화(i18n) 메시지 응답 규칙

- **모든 사용자 대상 메시지는 국제화 필수**
- **메시지 파일 구조**:
    - `messages.properties` (기본 한국어)
    - `messages_en.properties` (영어)
    - `messages_ko.properties` (한국어 명시)
- **메시지 키 네이밍 규칙**:
  ```
  [category].[domain].[detail] = 메시지
  
  # 예시
  error.user.not-found.id = 사용자를 찾을 수 없습니다. ID: {0}
  validation.board.title.required = 제목을 입력해주세요
  log.auth.login-failed = ❌ 로그인 실패: 사용자={0}
  success.email-verification.completed = 이메일 인증이 완료되었습니다
  ```
- **메시지 카테고리**:
    - `error.*` - 에러 메시지
    - `validation.*` - 유효성 검증 메시지
    - `success.*` - 성공 메시지
    - `info.*` - 정보 메시지
    - `log.*` - 로깅 메시지
    - `exception.title.*` - 예외 제목
- **MessageUtils 사용**: 코드에서 메시지 조회 시 반드시 사용

### Null 안전성 규칙

- **모든 패키지에 `@NullMarked` package-info.java 필수**
- **JSpecify 어노테이션 활용**: 명시적 null 허용/불허 선언
- **컴파일 타임 null 체크 보장**

### 검증 어노테이션 규칙

- **생성 시**: `@Valid*` 어노테이션 (필수 필드)
- **수정 시**: `@Optional*` 어노테이션 (선택적 필드)
- **커스텀 검증**: 도메인별 검증 어노테이션 활용
- **메시지 키 연동**: `message = "{validation.key}"` 패턴 사용

### 로깅 규칙

- **보안 로깅**: `SecurityLoggingAspect` - 인증/권한 관련
- **비즈니스 로깅**: `BusinessLogAspect` - 주요 비즈니스 액션
- **요청 로깅**: `RequestLoggingFilter` - HTTP 요청/응답
- **민감정보 마스킹**:
  ```
  password → 🔒[MASKED_PASSWORD]
  token → 🎫[MASKED_TOKEN]
  secret → 🔐[MASKED_SECRET]
  ```
- **로그 레벨별 아이콘**: 📥 시작, 📤 완료, ❌ 실패, 🌐 요청

### 예외 처리 규칙

- **RFC 7807 Problem Details 표준 준수**
- **GlobalExceptionHandler 중앙 집중화**
- **커스텀 예외**: `@StandardException` 활용
- **예외별 제목 메시지**: `exception.title.*` 키 사용

### MapStruct 매핑 규칙

- **2단계 매핑**: Entity ↔ Result ↔ Response
- **컴포넌트 모델**: `MappingConstants.ComponentModel.SPRING`
- **Application 계층**: Entity ↔ Result 매핑
- **Presentation 계층**: Result ↔ Response 매핑

### 보안 규칙

- **세션 기반 인증**: Redis 세션 스토어 사용
- **메서드 레벨 권한**: `@PreAuthorize` 활용
- **커스텀 Principal**: `AppUserPrincipal` 사용
- **CSRF 보호**: 기본 활성화
- **패스워드 정책**:
    - 최소 8자, 최대 100자
    - 대문자, 소문자, 숫자, 특수문자 포함 필수
    - BCrypt 인코딩

### API 설계 규칙

- **RESTful 설계**: HTTP 메서드 의미에 맞는 사용
- **응답 상태 코드**:
    - 200 OK: 조회 성공
    - 201 Created: 생성 성공
    - 204 No Content: 수정/삭제 성공
    - 400 Bad Request: 요청 형식 오류
    - 401 Unauthorized: 인증 실패
    - 409 Conflict: 중복 데이터
    - 422 Unprocessable Entity: 유효성 검증 실패
- **OpenAPI 문서화**: 모든 API 엔드포인트 문서화 필수

### 테스트 규칙

- **테스트 태그**: 필요 시 `@Tag("unit")`, `@Tag("integration")` 등으로 분류 가능
- **네이밍 규칙**: `[Method]_[Condition]_[Expected]`
- **H2 (in-memory)**: Docker 없이 실행 가능한 통합 테스트
- **ArchUnit**: 아키텍처 계층 준수 검증
- **병렬 실행**: `maxParallelForks` 설정으로 성능 최적화
- **Mock 사용**: Spring Boot 3.5+ 에서는 `@MockitoBean` 사용 (Spring Boot 3.4부터 도입된 새로운 어노테이션)
    - 기존 `@MockBean` 대신 `@MockitoBean` 사용 필수
    - `import org.springframework.test.context.bean.override.mockito.MockitoBean;`

## Code Conventions

- All packages have @NullMarked package-info.java
- Lombok for boilerplate reduction
- MapStruct for mapping
- Import statements instead of full package paths
- For class name conflicts: import frequently used, use full path for less frequent

## Testing Structure

- **Unit**: Mock dependencies, fast execution
- **MVC**: MockMvc-based controller tests using `@WebMvcTest`
- **Architecture**: ArchUnit for layer compliance
- **Naming**: [Method]_[Condition]_[Expected]

## API Paths

All REST APIs under `/api/*`:

- `/api/auth/*` - Authentication
- `/api/users/*` - User management
- `/api/boards/*` - Board operations

## Environment

Docker services (Spring Boot Docker Compose auto-start):

- PostgreSQL: Dynamic port mapping
- Redis: Dynamic port mapping

Profiles:

- `dev`: Auto-DDL, SQL logging, debug
- `smtp`: Email functionality (currently disabled)

## Dependencies

- Spring Boot Starters: web, data-jpa, validation, aop, security, mail, thymeleaf
- Session: spring-session-data-redis
- Documentation: SpringDoc OpenAPI 2.8.12
- Mapping: MapStruct 1.6.3
- Testing: JUnit 5, Spring Security Test/MockMvc, H2 (in-memory), ArchUnit 1.4.1
- Null Safety: JSpecify 1.0.0

## Quality Standards

- SonarCloud integration
- IntelliJ IDEA inspections
- RFC 7807 Problem Details for errors
- GlobalExceptionHandler for centralized error handling
  
