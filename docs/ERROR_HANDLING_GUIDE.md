# Error Handling Guide

Board-Hole 프로젝트의 에러 처리 표준 가이드

## 📋 목차
- [개요](#개요)
- [ProblemDetail 표준](#problemdetail-표준)
- [에러 코드 체계](#에러-코드-체계)
- [클라이언트 에러 처리 가이드](#클라이언트-에러-처리-가이드)
- [서버 구현 가이드](#서버-구현-가이드)
- [테스트 가이드](#테스트-가이드)

## 개요

Board-Hole은 [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) 표준을 준수하여 일관되고 명확한 에러 응답을 제공합니다.

### 핵심 원칙
- **표준 준수**: RFC 7807 Problem Details 형식 사용
- **일관성**: 모든 API 엔드포인트에서 동일한 에러 구조
- **명확성**: 구체적이고 이해하기 쉬운 에러 메시지
- **추적성**: traceId를 통한 에러 추적 지원
- **국제화**: 다국어 에러 메시지 지원

## ProblemDetail 표준

### 기본 구조

```json
{
  "type": "urn:problem-type:not-found",
  "title": "리소스를 찾을 수 없음",
  "status": 404,
  "detail": "게시글을 찾을 수 없습니다.",
  "instance": "/api/boards/999",
  "code": "NOT_FOUND",
  "path": "/api/boards/999",
  "method": "GET",
  "timestamp": "2025-09-01T01:23:45Z",
  "traceId": "trace-abc-def-ghi"
}
```

### 필드 설명

#### RFC 7807 표준 필드
- **type** (string): 문제 유형을 식별하는 URI
  - 기본 형식: `urn:problem-type:{slug}`
  - 커스텀 설정 시: `{base-uri}/{slug}`
- **title** (string): 사람이 읽을 수 있는 간단한 제목
- **status** (number): HTTP 상태 코드
- **detail** (string): 구체적인 에러 설명
- **instance** (string): 문제가 발생한 특정 리소스 URI

#### 확장 필드
- **code** (string): 애플리케이션별 에러 코드
- **path** (string): 요청 경로
- **method** (string): HTTP 메서드
- **timestamp** (string): 에러 발생 시간 (ISO 8601)
- **traceId** (string): 요청 추적 ID
- **errors** (array): 검증 오류 목록 (400 에러 시)

### 검증 오류 구조

```json
{
  "type": "urn:problem-type:validation-error",
  "title": "유효성 검증 실패",
  "status": 400,
  "detail": "입력된 데이터가 올바르지 않습니다.",
  "code": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "title",
      "message": "제목을 입력해주세요",
      "rejectedValue": ""
    },
    {
      "field": "content",
      "message": "내용은 10자 이상이어야 합니다",
      "rejectedValue": "short"
    }
  ]
}
```

## 에러 코드 체계

### HTTP 상태 코드별 에러 타입

| 상태 코드 | Type | Code | 설명 |
|----------|------|------|------|
| 400 | validation-error | VALIDATION_ERROR | 유효성 검증 실패 |
| 400 | bad-request | BAD_REQUEST | 잘못된 요청 |
| 401 | unauthorized | UNAUTHORIZED | 인증 실패 |
| 403 | forbidden | FORBIDDEN | 권한 부족 |
| 404 | not-found | NOT_FOUND | 리소스 없음 |
| 409 | conflict | CONFLICT | 데이터 충돌 |
| 409 | duplicate-username | USER_DUPLICATE_USERNAME | 사용자명 중복 |
| 409 | duplicate-email | USER_DUPLICATE_EMAIL | 이메일 중복 |
| 500 | about:blank | INTERNAL_ERROR | 서버 내부 오류 |

### 도메인별 에러 코드

#### 인증 (Auth)
- `UNAUTHORIZED`: 인증 실패
- `USER_DUPLICATE_USERNAME`: 사용자명 중복
- `USER_DUPLICATE_EMAIL`: 이메일 중복

#### 게시판 (Board)
- `NOT_FOUND`: 게시글 없음
- `FORBIDDEN`: 수정/삭제 권한 없음
- `VALIDATION_ERROR`: 제목/내용 검증 실패

#### 사용자 (User)
- `NOT_FOUND`: 사용자 없음
- `FORBIDDEN`: 프로필 수정 권한 없음
- `CONFLICT`: 데이터 충돌

## 클라이언트 에러 처리 가이드

### JavaScript/TypeScript 예제

```javascript
// API 호출 및 에러 처리
async function createBoard(data) {
  try {
    const response = await fetch('/api/boards', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const problem = await response.json();
      handleProblemDetail(problem);
      return;
    }

    const board = await response.json();
    return board;
  } catch (error) {
    console.error('Network error:', error);
  }
}

// ProblemDetail 처리
function handleProblemDetail(problem) {
  console.error(`Error ${problem.status}: ${problem.title}`);
  console.error(`Details: ${problem.detail}`);
  console.error(`Trace ID: ${problem.traceId}`);

  switch (problem.status) {
    case 400:
      if (problem.errors) {
        // 검증 오류 처리
        problem.errors.forEach(error => {
          console.error(`Field ${error.field}: ${error.message}`);
        });
      }
      break;
    case 401:
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
      break;
    case 403:
      alert('권한이 없습니다.');
      break;
    case 404:
      alert('요청한 리소스를 찾을 수 없습니다.');
      break;
    case 409:
      if (problem.code === 'USER_DUPLICATE_USERNAME') {
        alert('이미 사용 중인 사용자명입니다.');
      }
      break;
    default:
      alert('서버 오류가 발생했습니다.');
  }
}
```

### 에러 타입 정의 (TypeScript)

```typescript
interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  code?: string;
  path?: string;
  method?: string;
  timestamp?: string;
  traceId?: string;
  errors?: ValidationError[];
}

interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: any;
}
```

## 서버 구현 가이드

### 커스텀 예외 생성

```java
// 도메인별 커스텀 예외
public class BoardNotFoundException extends ResourceNotFoundException {
    public BoardNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. ID: " + id);
    }
}

// 컨트롤러에서 사용
@GetMapping("/{id}")
public BoardResponse getBoard(@PathVariable Long id) {
    return boardService.findById(id)
        .orElseThrow(() -> new BoardNotFoundException(id));
}
```

### GlobalExceptionHandler 확장

```java
@ExceptionHandler(BoardNotFoundException.class)
public ProblemDetail handleBoardNotFound(
        BoardNotFoundException ex, 
        HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND, 
        ex.getMessage()
    );
    pd.setTitle("게시글을 찾을 수 없음");
    pd.setType(buildType("board-not-found"));
    pd.setProperty("code", "BOARD_NOT_FOUND");
    addCommon(pd, request);
    return pd;
}
```

### 설정

```yaml
# application.yml
boardhole:
  problem:
    # 문제 유형 링크의 베이스 URI
    # 예: https://api.boardhole.com/problems
    base-uri: 
```

## 테스트 가이드

### 컨트롤러 테스트

```java
@Test
@DisplayName("존재하지 않는 게시글 조회 시 404 응답")
void getBoard_NotFound() throws Exception {
    mockMvc.perform(get("/api/boards/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"))
        .andExpect(jsonPath("$.title").exists())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").exists())
        .andExpect(jsonPath("$.instance").value("/api/boards/999999"))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.timestamp").exists());
}
```

### 검증 오류 테스트

```java
@Test
@DisplayName("유효성 검증 실패 시 400 응답")
void createBoard_ValidationError() throws Exception {
    mockMvc.perform(post("/api/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.errors").isArray())
        .andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists());
}
```

## Swagger 문서화

Swagger UI에서 모든 에러 응답이 자동으로 문서화됩니다:

- **ProblemDetailExtended** 스키마 정의
- 각 HTTP 상태 코드별 예제 제공
- 검증 오류 시 errors 배열 구조 표시

### OpenAPI 설정

```java
@Bean
public OpenApiCustomizer globalProblemDetailResponses() {
    return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
        pathItem.readOperations().forEach(operation -> {
            ApiResponses responses = operation.getResponses();
            // 400, 401, 403, 404, 500 응답 자동 추가
            addProblemResponse(responses, "400", "Bad Request");
            // ...
        });
    });
}
```

## 모니터링 및 디버깅

### TraceId 활용

모든 에러 응답에는 `traceId`가 포함되어 있어 로그 추적이 가능합니다:

```bash
# 로그에서 특정 에러 추적
grep "trace-abc-def-ghi" application.log
```

### 로깅 전략

```java
@ExceptionHandler(Exception.class)
public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
    String traceId = MDC.get(TRACE_ID);
    log.error("Unexpected error [traceId={}]", traceId, ex);
    // ProblemDetail 생성 및 반환
}
```

## 베스트 프랙티스

1. **구체적인 에러 메시지**: 사용자가 문제를 이해하고 해결할 수 있도록 구체적인 메시지 제공
2. **일관된 코드 체계**: 도메인별로 일관된 에러 코드 사용
3. **적절한 HTTP 상태 코드**: 상황에 맞는 정확한 HTTP 상태 코드 사용
4. **보안 고려**: 민감한 정보는 에러 메시지에 포함하지 않음
5. **국제화 지원**: MessageSource를 통한 다국어 메시지 제공
6. **테스트 커버리지**: 모든 에러 케이스에 대한 테스트 작성

---

**Created**: 2025-09-01  
**Last Updated**: 2025-09-01  
**Version**: 1.0