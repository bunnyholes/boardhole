# Contributing to Board-Hole

🎉 Board-Hole 프로젝트에 기여해주셔서 감사합니다!

이 가이드는 프로젝트 기여 방법과 개발 컨벤션을 설명합니다.

## 📋 Table of Contents

- [Code of Conduct](#-code-of-conduct)
- [How to Contribute](#-how-to-contribute)
- [Development Process](#-development-process)
- [Getting Help](#-getting-help)
- [Git Workflow](#-git-workflow)
- [Commit Convention](#-commit-convention)
- [Coding Standards](#-coding-standards)
- [Pull Request Guidelines](#-pull-request-guidelines)

## 🤝 Code of Conduct

### Our Pledge

우리는 모든 참여자가 괴롭힘 없는 경험을 할 수 있도록 다음을 약속합니다:

- 다양한 배경과 경험을 가진 사람들을 환영
- 건설적인 피드백과 비판 수용
- 실수에 대한 책임감 있는 대응
- 학습 지향적 환경 조성

### Expected Behavior

- 📖 **학습 지향적**: 교육적 가치가 있는 기여
- 🤔 **건설적 토론**: 기술적 결정에 대한 합리적 토론
- 📝 **문서화**: 코드 변경 시 관련 문서 업데이트
- 🧪 **테스트**: 새로운 기능에는 테스트 포함

## 🎯 How to Contribute

### Project Vision

**Board-Hole은 Spring Boot + CQRS 패턴을 학습하기 위한 교육용 프로젝트입니다.**

- **🎓 Educational Focus**: 학습 가치가 높은 코드 작성
- **📚 Best Practices**: Spring Boot 모범 사례 적용
- **🔧 Practical Examples**: 실무에서 사용할 수 있는 패턴 구현
- **👥 Beginner Friendly**: 초보자도 이해하기 쉬운 구조

### Ways to Contribute

#### 🐛 Reporting Bugs

버그를 발견하셨나요?

1. **기존 이슈 확인**: [Issues](https://github.com/your-username/board-hole/issues)에서 중복 확인
2. **재현 단계 작성**: 버그를 재현할 수 있는 단계
3. **환경 정보 포함**: OS, Java 버전, 브라우저 등
4. **스크린샷 첨부**: UI 관련 버그의 경우

#### 💡 Suggesting Features

새로운 기능을 제안하고 싶으신가요?

1. **Feature Request Issue** 생성
2. **학습 가치 설명**: 이 기능이 어떤 학습 효과를 가져다주는지
3. **Use Case 제시**: 실제 사용 시나리오
4. **구현 아이디어**: 가능하다면 구현 방향 제시

#### 📝 Improving Documentation

- README, ARCHITECTURE, API 문서 개선
- 코드 주석 추가 (복잡한 로직에 한해)
- 학습 가이드 및 튜토리얼 작성
- 다국어 문서 번역

#### 🔧 Code Contributions

- 새로운 기능 구현
- 버그 수정
- 성능 개선
- 리팩토링
- 테스트 커버리지 향상

## 🛠 Development Process

### Setting up Development Environment

1. **Prerequisites**
   ```bash
   Java 21 이상
   Docker & Docker Compose
   Git 2.20 이상
   IntelliJ IDEA (권장) 또는 VS Code
   ```

2. **Fork & Clone**
   ```bash
   # 1. GitHub에서 Fork
   # 2. 로컬에 클론
   git clone https://github.com/YOUR-USERNAME/board-hole.git
   cd board-hole
   
   # 3. Upstream 원격 저장소 추가
   git remote add upstream https://github.com/ORIGINAL-OWNER/board-hole.git
   ```

3. **Development Setup**
   ```bash
   # 1. 데이터베이스 시작
   docker-compose up -d
   
   # 2. 애플리케이션 빌드
   ./gradlew build
   
   # 3. 애플리케이션 실행
   ./gradlew bootRun
   
   # 4. 테스트 실행
   ./gradlew test
   ```

4. **IDE Configuration**
   
   **IntelliJ IDEA**:
   ```yaml
   Plugins:
     - Lombok Plugin 설치
   Code Style:
     - Google Java Style Guide
   Encoding: UTF-8
   Line Separator: LF (Unix)
   ```

   **VS Code**:
   ```yaml
   Extensions:
     - Extension Pack for Java
     - Spring Boot Extension Pack
     - Lombok Annotations Support
   ```

## 🔄 Git Workflow

### Branch Strategy

```
main (production)
├── feature/add-board-likes       # 새 기능 개발
├── bugfix/fix-auth-validation    # 버그 수정  
├── docs/update-architecture      # 문서 수정
├── refactor/improve-cqrs         # 리팩토링
└── test/add-integration-tests    # 테스트 추가
```

### Branch Naming Convention

| 타입 | 형식 | 예시 | 설명 |
|------|------|------|------|
| 🆕 Feature | `feature/description` | `feature/add-board-likes` | 새 기능 개발 |
| 🐛 Bugfix | `bugfix/description` | `bugfix/fix-auth-validation` | 버그 수정 |
| 📝 Docs | `docs/description` | `docs/update-api-guide` | 문서 수정 |
| ♻️ Refactor | `refactor/description` | `refactor/improve-cqrs` | 리팩토링 |
| 🧪 Test | `test/description` | `test/add-controller-tests` | 테스트 추가 |
| 🔧 Chore | `chore/description` | `chore/update-dependencies` | 빌드/설정 변경 |

### Workflow Process

```bash
# 1. 최신 main 브랜치 동기화
git checkout main
git pull upstream main

# 2. 새 기능 브랜치 생성
git checkout -b feature/add-board-likes

# 3. 개발 작업 수행
# ... 코딩 ...

# 4. 변경사항 스테이징 및 커밋
git add .
git commit -m "feat(board): Add like functionality to boards"

# 5. 원격 저장소에 푸시
git push origin feature/add-board-likes

# 6. Pull Request 생성
# GitHub에서 PR 생성
```

## 📝 Commit Convention

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Commit Types

| 타입 | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat(board): Add like functionality` |
| `fix` | 버그 수정 | `fix(auth): Resolve JWT token validation issue` |
| `docs` | 문서 수정 | `docs(readme): Update installation instructions` |
| `style` | 코드 스타일 변경 | `style(controller): Apply Google Java formatting` |
| `refactor` | 코드 리팩토링 | `refactor(service): Extract validation logic` |
| `test` | 테스트 추가/수정 | `test(board): Add controller integration tests` |
| `chore` | 빌드/설정 변경 | `chore(deps): Update Spring Boot to 3.5.4` |
| `perf` | 성능 개선 | `perf(query): Optimize board list query` |
| `security` | 보안 개선 | `security(auth): Add rate limiting` |

### Commit Message Examples

**✅ Good Examples**:
```bash
feat(board): Add board like/unlike functionality

- Implement POST /api/boards/{id}/like endpoint
- Add like count to BoardResponse
- Include like status for authenticated users
- Add database migration for likes table

Resolves #25

fix(auth): Handle JWT token expiration gracefully

- Add proper error response for expired tokens
- Include refresh token mechanism
- Update frontend to handle 401 responses

Fixes #31
```

**❌ Bad Examples**:
```bash
fix bug                    # 너무 간단
수정함                      # 한글 사용
Fixed stuff               # 구체적이지 않음
Updated files             # 변경 내용 불명확
```

## 📐 Coding Standards

### Java Code Style

**클래스 구조**:
```java
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor  // Lombok 사용
@Slf4j                   // 로깅
@Validated               // 유효성 검증
public class BoardController {
    
    // 1. 의존성 주입 (final fields)
    private final BoardCommandService boardCommandService;
    private final BoardQueryService boardQueryService;
    
    // 2. Public methods (API endpoints)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BoardResponse> create(
            @Validated @ModelAttribute BoardCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        // 구현...
    }
    
    // 3. Private helper methods (if needed)
    private void validateRequest(BoardCreateRequest request) {
        // 구현...
    }
}
```

### CQRS Pattern Guidelines

#### Commands (쓰기)
```java
// Command 정의 (불변 객체)
public record CreateBoardCommand(
    String title,
    String content,  
    Long authorId
) {}

// CommandService 구현
@Service
@RequiredArgsConstructor
@Transactional  // 쓰기 작업은 트랜잭션 필요
public class BoardCommandService {
    
    public BoardResult create(CreateBoardCommand command) {
        // 1. 유효성 검증
        validateCommand(command);
        
        // 2. 엔티티 생성
        Board board = Board.builder()
            .title(command.title())
            .content(command.content())
            .authorId(command.authorId())
            .build();
            
        // 3. 저장
        Board saved = boardRepository.save(board);
        
        // 4. Result 반환
        return BoardMapper.INSTANCE.toResult(saved);
    }
}
```

#### Queries (읽기)
```java
// Query 정의
public record GetBoardQuery(Long id) {}

// QueryService 구현  
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 읽기 전용
public class BoardQueryService {
    
    public BoardResult handle(GetBoardQuery query) {
        Board board = boardRepository.findById(query.id())
            .orElseThrow(() -> new ResourceNotFoundException(
                MessageUtils.getMessageStatic("error.board.not-found", query.id())
            ));
            
        return BoardMapper.INSTANCE.toResult(board);
    }
}
```

### Error Handling

```java
// Custom Exception with i18n
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }
}
```

### Testing Standards

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class BoardCommandServiceTest {
    
    @Mock private BoardRepository boardRepository;
    @InjectMocks private BoardCommandService boardCommandService;
    
    @Test
    @DisplayName("게시글 생성 - 성공")
    void create_Success() {
        // Given
        CreateBoardCommand command = new CreateBoardCommand("제목", "내용", 1L);
        Board savedBoard = Board.builder()...
        when(boardRepository.save(any())).thenReturn(savedBoard);
        
        // When
        BoardResult result = boardCommandService.create(command);
        
        // Then
        assertThat(result.title()).isEqualTo("제목");
        verify(boardRepository).save(any(Board.class));
    }
}
```

#### Integration Tests
```java
@SpringBootTest
@Testcontainers
class BoardControllerIntegrationTest extends AbstractIntegrationTest {
    
    @Test
    @DisplayName("GET /api/boards/{id} - 게시글 조회 성공")
    void getBoard_Success() throws Exception {
        // Given
        Long boardId = createTestBoard();
        
        // When & Then
        mockMvc.perform(get("/api/boards/{id}", boardId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("테스트 제목"))
            .andExpect(jsonPath("$.content").value("테스트 내용"));
    }
}
```

### Package Organization

```
src/main/java/bunny/boardhole/
├── {domain}/                      # 도메인별 패키지 (board, user, auth, admin)
│   ├── web/                       # Web Layer
│   │   ├── {Domain}Controller.java    # REST 컨트롤러
│   │   ├── dto/                       # Request/Response DTOs
│   │   │   ├── {Domain}CreateRequest.java
│   │   │   ├── {Domain}UpdateRequest.java
│   │   │   └── {Domain}Response.java
│   │   └── mapper/                    # Web ↔ Application 매핑
│   │       └── {Domain}WebMapper.java
│   ├── application/               # Application Layer (CQRS)
│   │   ├── command/               # 쓰기 작업
│   │   │   ├── {Action}{Domain}Command.java
│   │   │   └── {Domain}CommandService.java
│   │   ├── query/                 # 읽기 작업
│   │   │   ├── {Action}{Domain}Query.java
│   │   │   └── {Domain}QueryService.java
│   │   ├── result/                # 결과 객체
│   │   │   └── {Domain}Result.java
│   │   ├── event/                 # 도메인 이벤트
│   │   │   ├── {Action}Event.java
│   │   │   └── {Action}EventListener.java
│   │   └── mapper/                # Application ↔ Domain 매핑
│   │       └── {Domain}Mapper.java
│   ├── domain/                    # Domain Layer
│   │   └── {Domain}.java          # 엔티티
│   └── infrastructure/            # Infrastructure Layer
│       └── {Domain}Repository.java    # 데이터 접근
│
└── common/                        # 공통 모듈
    ├── config/                    # 설정
    ├── security/                  # 보안
    ├── exception/                 # 예외 처리
    ├── util/                      # 유틸리티
    └── bootstrap/                 # 초기화
```

### Naming Conventions

| 요소 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `BoardController`, `UserService` |
| 메서드 | camelCase | `createBoard()`, `findUserById()` |
| 변수 | camelCase | `userName`, `boardTitle` |
| 상수 | UPPER_SNAKE_CASE | `MAX_TITLE_LENGTH` |
| 패키지 | lowercase | `controller`, `service` |

### CQRS Naming Rules

- **Command**: `{Action}{Domain}Command` (예: `CreateBoardCommand`)
- **Query**: `{Action}{Domain}Query` (예: `GetBoardQuery`)  
- **Result**: `{Domain}Result` (예: `BoardResult`)
- **Event**: `{Action}Event` (예: `ViewedEvent`)
- **Service**: `{Domain}{Type}Service` (예: `BoardCommandService`)

## 📋 Pull Request Guidelines

### PR Title Format

```
[Type] Brief description

Examples:
[Feature] Add board like functionality
[Bugfix] Fix JWT token validation
[Docs] Update architecture documentation
[Refactor] Improve CQRS command handling
```

### PR Template

```markdown
## 📋 Type of Change

- [ ] 🆕 New feature (non-breaking change which adds functionality)
- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📝 Documentation update
- [ ] 🧪 Test addition or modification
- [ ] ♻️ Code refactoring

## 📖 Description

<!-- Describe what this PR does and why -->

## 🧪 Testing

- [ ] 로컬 테스트 완료
- [ ] 새로운 테스트 케이스 추가
- [ ] 기존 테스트 통과 확인
- [ ] Integration tests 통과

## 📸 Screenshots (UI changes only)

<!-- Add screenshots or GIFs for UI changes -->

## 📝 Checklist

- [ ] 코드가 프로젝트의 스타일 가이드를 따름
- [ ] 자체 코드 리뷰 완료
- [ ] 관련 문서 업데이트 완료
- [ ] 새로운 의존성에 대한 설명 추가
- [ ] 테스트 통과 확인

## 🔗 Related Issues

<!-- Link related issues: Closes #123, Resolves #456 -->
```

### PR Review Process

1. **자동 검증**: CI/CD 파이프라인 통과
2. **코드 리뷰**: 최소 1명의 리뷰어 승인
3. **테스트 확인**: 모든 테스트 통과
4. **문서 검토**: 관련 문서 업데이트 확인

### Review Criteria

- **코드 품질**: Clean Code 원칙 준수
- **CQRS 패턴**: Command/Query 분리 올바른 적용
- **보안**: 인증/인가 올바른 구현
- **테스트**: 적절한 테스트 커버리지
- **문서화**: 복잡한 로직에 대한 설명
- **학습 가치**: 교육적 목적에 부합

## 🎓 Learning Guidelines

### Educational Code Standards

이 프로젝트는 학습 목적이므로 다음을 고려해주세요:

- **📚 이해하기 쉬운 코드**: 과도한 추상화보다는 명확함을 선택
- **📝 적절한 주석**: 복잡한 비즈니스 로직이나 기술적 결정에 대한 설명
- **🎯 패턴 명확성**: CQRS, 이벤트 패턴이 명확히 드러나는 구조
- **🔍 예제 코드**: 실제 사용 방법을 보여주는 예제

### Code Review Focus Areas

1. **아키텍처 준수**: CQRS 패턴 올바른 적용
2. **레이어 분리**: 각 레이어의 책임 명확성
3. **Spring Boot 활용**: 프레임워크 기능 적절한 사용
4. **예외 처리**: 일관된 에러 처리 방식
5. **국제화**: 다국어 메시지 올바른 사용

## 🆘 Getting Help

막히셨나요? 도움을 받을 수 있는 방법들:

### 📞 Communication Channels

- **🐛 Bug Reports**: [GitHub Issues](https://github.com/your-username/board-hole/issues)
- **💬 Discussions**: [GitHub Discussions](https://github.com/your-username/board-hole/discussions)  
- **❓ Questions**: Issue에 `question` 라벨로 질문
- **📧 Direct Contact**: [maintainer@example.com]

### 📚 Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **CQRS Pattern**: [Martin Fowler's CQRS](https://martinfowler.com/bliki/CQRS.html)
- **MapStruct**: https://mapstruct.org/
- **Testing**: [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)

### 🔰 Good First Issues

첫 기여를 위한 추천 작업들:

1. **📝 Documentation**: 오타 수정, 설명 보완
2. **🧪 Testing**: 테스트 케이스 추가
3. **🌐 i18n**: 새로운 언어 메시지 추가
4. **🎨 Frontend**: 정적 HTML/CSS 개선
5. **📊 Logging**: 로그 메시지 개선

---

**✨ 기여해주셔서 감사합니다!** 모든 기여는 프로젝트를 더 나은 학습 도구로 만드는 데 도움이 됩니다.