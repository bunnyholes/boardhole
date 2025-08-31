# 개발 패턴 가이드

Board-Hole 프로젝트의 일관된 개발 패턴과 규칙을 설명합니다.

## 🎯 핵심 원칙

### 1. **통일성 > 복잡도**
- 모든 도메인은 동일한 구조와 패턴 사용
- 예외 패턴은 반드시 문서화된 이유가 있어야 함
- 개발자 혼란 방지가 최우선

### 2. **CQRS 패턴 일관성**
- 모든 도메인: Command(쓰기) / Query(읽기) 분리
- 성능상 필요한 경우만 이벤트 사용 (예: ViewCount)
- 모든 처리는 CommandService/QueryService를 통해

### 3. **MapStruct 전체 적용**
- 모든 DTO 변환에 MapStruct 사용
- 수동 매핑 코드 금지
- 컴파일 타임 안전성 보장

## 📁 표준 도메인 구조

모든 도메인은 다음 구조를 **반드시** 따라야 합니다:

```
{domain}/
├── web/                              # Web Layer
│   ├── {Domain}Controller.java       # REST Controller
│   ├── dto/                          # Request/Response DTOs
│   │   ├── {Domain}CreateRequest.java
│   │   ├── {Domain}UpdateRequest.java
│   │   └── {Domain}Response.java
│   └── mapper/                       # Web ↔ Application 매핑
│       └── {Domain}WebMapper.java
├── application/                      # Application Layer
│   ├── command/                      # 쓰기 작업 (Commands)
│   │   ├── {Domain}CommandService.java
│   │   ├── Create{Domain}Command.java
│   │   └── Update{Domain}Command.java
│   ├── query/                        # 읽기 작업 (Queries)
│   │   ├── {Domain}QueryService.java
│   │   ├── Get{Domain}Query.java
│   │   └── List{Domain}Query.java
│   ├── dto/                          # Application Layer 결과
│   │   └── {Domain}Result.java
│   └── mapper/                       # Application ↔ Domain 매핑
│       └── {Domain}Mapper.java
├── domain/                           # Domain Layer
│   └── {Domain}.java                 # 도메인 엔티티
└── infrastructure/                   # Infrastructure Layer
    └── {Domain}Repository.java       # 데이터 접근
```

### 구조 예외 금지 사항

❌ **절대 하지 말 것:**
- 단순하다고 application 계층 생략
- Command/Query 없이 직접 Service 호출
- DTO 변환을 수동으로 처리
- 패키지 구조 변경

## 🏷 네이밍 규칙

### Command 클래스
```java
{Action}{Domain}Command
// 예시:
- CreateBoardCommand
- UpdateUserCommand  
- DeleteCommentCommand
- IncrementBoardViewCountCommand  // ViewCount 특수 케이스
```

### Query 클래스
```java
{Action}{Domain}Query
// 예시:
- GetBoardQuery
- ListBoardsQuery
- SearchBoardsQuery
- GetCurrentUserQuery
```

### Result 클래스
```java
{Domain}Result
// 예시:
- BoardResult
- UserResult  
- AuthResult
```

### Service 클래스
```java
{Domain}CommandService  // 쓰기 작업
{Domain}QueryService    // 읽기 작업
// 예시:
- BoardCommandService / BoardQueryService
- UserCommandService / UserQueryService
- AuthCommandService / AuthQueryService
```

### Mapper 클래스
```java
{Domain}WebMapper      // Web ↔ Application
{Domain}Mapper         // Application ↔ Domain
// 예시:
- BoardWebMapper / BoardMapper
- UserWebMapper / UserMapper
- AuthWebMapper        // Auth는 도메인 엔티티 없으므로 WebMapper만
```

## 🔄 Data Flow 패턴

### 1. Command Flow (쓰기)
```
HTTP Request (POST/PUT/DELETE)
    ↓
Controller
    ↓
WebMapper: DTO → Command
    ↓
CommandService: 비즈니스 로직 처리
    ↓
Repository: 데이터 저장
    ↓
Mapper: Entity → Result
    ↓
WebMapper: Result → Response DTO
    ↓
HTTP Response
```

### 2. Query Flow (읽기)
```
HTTP Request (GET)
    ↓
Controller  
    ↓
QueryService: 조회 로직 처리
    ↓
Repository: 데이터 조회
    ↓
Mapper: Entity → Result
    ↓
WebMapper: Result → Response DTO
    ↓
HTTP Response
```

## ⚡ 성능 예외 패턴

### ViewCount 이벤트 처리

**이유**: 낙관적 동시성 제어
- 조회 시 즉시 응답 (블로킹 방지)
- 대량 동시 접속자 처리 가능
- 장애 격리 (ViewCount 실패가 조회에 영향 없음)

**패턴**:
```java
// 1. Query에서 이벤트 발행
@GetMapping("/{id}")
public BoardResponse get(@PathVariable Long id) {
    // 즉시 응답
    BoardResult result = boardQueryService.handle(new GetBoardQuery(id));
    
    // 비동기 이벤트 발행
    eventPublisher.publishEvent(new ViewedEvent(id, viewerId));
    
    return boardWebMapper.toResponse(result);
}

// 2. 이벤트 리스너에서 Command 처리
@EventListener
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void onViewed(ViewedEvent event) {
    // CQRS 패턴 유지
    boardCommandService.incrementViewCount(
        new IncrementViewCountCommand(event.boardId())
    );
}
```

**적용 조건**:
- 대량 동시 접속이 예상되는 작업
- 실시간성보다 응답 속도가 중요한 경우
- 실패해도 핵심 기능에 영향 없는 작업
- **아키텍트 승인 필수**

## 📝 새 도메인 추가 가이드

### 1단계: 패키지 구조 생성
```bash
mkdir -p src/main/java/bunny/boardhole/{domain}/web/{dto,mapper}
mkdir -p src/main/java/bunny/boardhole/{domain}/application/{command,query,dto,mapper}
mkdir -p src/main/java/bunny/boardhole/{domain}/domain
mkdir -p src/main/java/bunny/boardhole/{domain}/infrastructure
```

### 2단계: 기본 클래스 생성 (board 도메인 참고)
1. Domain Entity
2. Repository  
3. Commands, Queries, Result
4. CommandService, QueryService
5. Mappers (WebMapper, Mapper)
6. Controller
7. Request/Response DTOs

### 3단계: 검증
- [ ] 패키지 구조가 다른 도메인과 동일한가?
- [ ] 네이밍 규칙을 준수했는가?
- [ ] Command/Query 패턴을 사용했는가?
- [ ] MapStruct를 사용했는가?
- [ ] 테스트 작성했는가?

## 🚨 주의사항

### DO
✅ 모든 도메인 동일한 구조 유지
✅ Command/Query 패턴 일관성  
✅ MapStruct 활용
✅ 성능 예외는 문서화
✅ 테스트 작성

### DON'T  
❌ 단순하다고 구조 생략
❌ 수동 DTO 매핑
❌ 네이밍 규칙 무시
❌ 예외 패턴 남용
❌ 문서화 없는 특수 처리

---

**기억하세요**: "복잡해도 일관되게" - 통일성이 복잡도보다 중요합니다!