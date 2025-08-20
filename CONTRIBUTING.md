# Contributing to Board-Hole

Spring Boot MVC 학습 프로젝트 기여 가이드

이 문서는 Board-Hole 프로젝트에 기여하는 방법과 개발 컨벤션을 설명합니다.

## 목차

- [시작하기 전에](#시작하기-전에)
- [개발 환경 설정](#개발-환경-설정)
- [Git 워크플로우](#git-워크플로우)
- [커밋 컨벤션](#커밋-컨벤션)
- [코딩 컨벤션](#코딩-컨벤션)
- [Pull Request 가이드](#pull-request-가이드)

## 시작하기 전에

### 프로젝트 목표
- **주 목적**: Spring Boot MVC 패턴 학습
- **현재 단계**: Controller-Service 레이어 구현 완료
- **다음 단계**: Repository 레이어 추가 예정

### 기여 원칙
- 학습 목적에 맞는 간단명료한 코드
- 초보자 친화적 구조 유지
- Spring Boot Best Practice 적용

## 개발 환경 설정

### 필수 요구사항
```bash
Java 21 이상
IntelliJ IDEA 또는 VS Code
Git 2.20 이상
Gradle 8.14.3 (Wrapper 사용 권장)
```

### 프로젝트 클론 및 설정
```bash
# 1. 저장소 클론
git clone https://github.com/your-username/board-hole.git
cd board-hole

# 2. 프로젝트 빌드
./gradlew build

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. 정상 동작 확인
curl http://localhost:8080/hello
```

### IDE 설정 권장사항
```yaml
IntelliJ IDEA:
  - Lombok Plugin 설치
  - Code Style: Google Java Style
  - Line Separator: LF (Unix)
  - Encoding: UTF-8
  
VS Code:
  - Extension Pack for Java 설치
  - Spring Boot Extension Pack 설치
```

## Git 워크플로우

### 브랜치 전략
```
master (main)
├── feature/add-board-controller    # 새 기능 개발
├── bugfix/fix-member-validation    # 버그 수정
├── docs/update-readme             # 문서 수정
└── refactor/improve-service-layer # 리팩토링
```

### 브랜치 명명 규칙
| 타입 | 형식 | 예시 |
|------|------|------|
| 기능 개발 | `feature/설명` | `feature/add-board-controller` |
| 버그 수정 | `bugfix/설명` | `bugfix/fix-member-validation` |
| 문서 | `docs/설명` | `docs/update-api-guide` |
| 리팩토링 | `refactor/설명` | `refactor/improve-service-layer` |
| 테스트 | `test/설명` | `test/add-controller-tests` |

### 작업 프로세스
```bash
# 1. 최신 main 브랜치로 이동
git checkout main
git pull origin main

# 2. 새 기능 브랜치 생성
git checkout -b feature/add-board-controller

# 3. 개발 작업 수행
# ... 코딩 ...

# 4. 변경사항 커밋
git add .
git commit -m "feat: Add Board controller with CRUD operations"

# 5. 원격 저장소에 푸시
git push origin feature/add-board-controller

# 6. Pull Request 생성
# GitHub에서 PR 생성
```

## 커밋 컨벤션

### 커밋 메시지 형식
```
<타입>(<범위>): <제목>

<본문>

<푸터>
```

### 커밋 타입
| 타입 | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat: Add Member registration API` |
| `fix` | 버그 수정 | `fix: Resolve NullPointerException in MemberService` |
| `docs` | 문서 수정 | `docs: Update README with API examples` |
| `style` | 코드 스타일 변경 | `style: Apply Google Java formatting` |
| `refactor` | 코드 리팩토링 | `refactor: Extract validation logic to separate method` |
| `test` | 테스트 추가/수정 | `test: Add unit tests for HelloController` |
| `chore` | 빌드/설정 변경 | `chore: Update Spring Boot to 3.5.4` |

### 커밋 메시지 예시
```bash
# 좋은 예시
feat(controller): Add Board CRUD operations

- Implement POST /boards for creating new board
- Add GET /boards/{id} for retrieving board by ID
- Include basic validation for board title and content

Resolves #15

# 나쁜 예시
fix bug          # 너무 간단
수정함            # 한글 사용
Fixed stuff      # 구체적이지 않음
```

## 코딩 컨벤션

### Java 코드 스타일
```java
// 권장 스타일
@RestController
@RequiredArgsConstructor  // Lombok 사용
public class BoardController {
    
    private final BoardService boardService;
    
    @PostMapping("/boards")
    public ResponseEntity<BoardDto> createBoard(@RequestBody @Valid BoardCreateRequest request) {
        BoardDto board = boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(board);
    }
}
```

### 패키지 구조 규칙
```
src/main/java/bunny/boardhole/
├── controller/          # @RestController 클래스
├── service/            # @Service 클래스  
├── repository/         # @Repository 클래스 (향후 추가)
├── domain/            # Entity 클래스 (향후 추가)
├── dto/               # DTO 클래스 (향후 추가)
├── config/            # @Configuration 클래스
└── exception/         # Custom Exception 클래스
```

### 네이밍 컨벤션
| 요소 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `BoardController`, `MemberService` |
| 메서드 | camelCase | `createBoard()`, `findMemberById()` |
| 변수 | camelCase | `memberName`, `boardTitle` |
| 상수 | UPPER_SNAKE_CASE | `MAX_BOARD_TITLE_LENGTH` |
| 패키지 | lowercase | `controller`, `service` |

### 코드 작성 가이드라인
```java
// DO: 명확한 메서드명과 매개변수
public BoardDto createBoard(String title, String content, String authorName) {
    validateBoardInput(title, content);
    return boardService.createBoard(title, content, authorName);
}

// DON'T: 불명확한 메서드명
public BoardDto create(String t, String c, String a) {
    return boardService.save(t, c, a);
}

// DO: 적절한 주석 (복잡한 로직만)
/**
 * 회원 비밀번호를 검증합니다.
 * 최소 8자 이상, 영문+숫자 조합 필수
 */
private boolean validatePassword(String password) {
    return password.length() >= 8 && 
           password.matches(".*[a-zA-Z].*") && 
           password.matches(".*[0-9].*");
}

// DON'T: 불필요한 주석
// 사용자 이름을 반환합니다
public String getUserName() {
    return this.userName;  // 사용자 이름 반환
}
```

## Pull Request 가이드

### PR 제목 형식
```
[타입] 간단한 설명

예시:
[Feature] Add Board CRUD API
[Bugfix] Fix member validation logic
[Docs] Update contributing guidelines
```

### PR 템플릿
```markdown
## 변경사항
- [ ] 새로운 기능 추가
- [ ] 버그 수정
- [ ] 문서 업데이트
- [ ] 리팩토링
- [ ] 테스트 추가

## 변경 내용
간단히 무엇을 변경했는지 설명해주세요.

## 테스트
- [ ] 로컬에서 테스트 완료
- [ ] 새로운 테스트 케이스 추가
- [ ] 기존 테스트 통과 확인

## 스크린샷 (UI 변경 시)
스크린샷이나 GIF를 첨부해주세요.

## 추가 설명
리뷰어가 알아야 할 추가 정보가 있다면 작성해주세요.
```

### PR 체크리스트
- [ ] 커밋 메시지가 컨벤션을 따르는가?
- [ ] 코드 스타일 가이드를 준수하는가?
- [ ] 불필요한 파일이 포함되지 않았는가?
- [ ] 테스트가 통과하는가?
- [ ] 문서가 업데이트되었는가? (필요시)

### 추천 첫 기여
1. **문서 개선**: 오타 수정, 설명 보완
2. **코드 주석**: 학습에 도움되는 주석 추가
3. **간단한 기능**: 새로운 API 엔드포인트 추가

---

**참고**: 이 가이드는 Spring Boot 학습을 위한 것입니다. 학습 목적에 맞는 기여를 환영합니다!