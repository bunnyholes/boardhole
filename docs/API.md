# API Reference

Board-Hole REST API 완전 명세서

## 📋 Table of Contents

- [Overview](#overview)
- [Authentication](#authentication)
- [Common Patterns](#common-patterns)
- [Boards API](#boards-api)
- [Users API](#users-api)
- [Auth API](#auth-api)
- [Email API](#email-api)
- [Admin API](#admin-api)
- [Error Handling](#error-handling)
- [Examples](#examples)

## 🌐 Overview

### Base URL
```
Local Development: http://localhost:8080/api
Production: https://your-domain.com/api
```

### Content Types
- **Request**: `application/json`, `application/x-www-form-urlencoded`, `multipart/form-data`
- **Response**: `application/json`

### Internationalization
모든 API 요청에 `lang` 쿼리 파라미터를 추가하여 언어를 변경할 수 있습니다:
- `?lang=ko` - 한국어 (기본값)
- `?lang=en` - English

예시: `GET /api/boards?lang=en`

## 🔐 Authentication

### Session-based Authentication

대부분의 API는 세션 기반 인증이 필요합니다. 로그인 성공 시 `JSESSIONID` 쿠키가 발급되며, 이후 요청에 쿠키를 포함해야 합니다.

```bash
# 로그인(세션 생성) — 쿠키 저장
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -c cookies.txt \
  -d 'username=admin&password=admin123'

# 세션 쿠키로 인증된 요청 보내기
curl -b cookies.txt http://localhost:8080/api/auth/me
```

### Authentication Required Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| 🟢 `GET` | `/api/boards` | 게시글 목록 | ❌ |
| 🟢 `GET` | `/api/boards/{id}` | 게시글 조회 | ❌ |
| 🟡 `POST` | `/api/boards` | 게시글 작성 | ✅ (세션) |
| 🟡 `PUT` | `/api/boards/{id}` | 게시글 수정 | ✅ (작성자/관리자) |
| 🔴 `DELETE` | `/api/boards/{id}` | 게시글 삭제 | ✅ (작성자/관리자) |

## 🔄 Common Patterns

### Pagination

페이지네이션을 지원하는 엔드포인트에서 사용하는 공통 쿼리 파라미터:

```http
GET /api/boards?page=0&size=10&sort=id,desc
```

**Parameters**:
- `page` (int, default: 0) - 페이지 번호 (0부터 시작)
- `size` (int, default: 10) - 페이지 크기
- `sort` (string) - 정렬 필드와 방향 (예: `id,desc`, `title,asc`)

**Response**:
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {"sorted": true, "orders": [...]}
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false,
  "numberOfElements": 10
}
```

### Error Response Format

모든 에러는 [RFC 7807 Problem Details](https://tools.ietf.org/html/rfc7807) 형식을 따릅니다:

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "게시글을 찾을 수 없습니다. ID: 999",
  "instance": "/api/boards/999"
}
```

## 📝 Boards API

게시글 관련 API 엔드포인트

### Get Boards List

게시글 목록을 페이지네이션과 검색으로 조회합니다.

```http
GET /api/boards
```

**Query Parameters**:
- `page` (int, optional) - 페이지 번호 (default: 0)
- `size` (int, optional) - 페이지 크기 (default: 10)
- `sort` (string, optional) - 정렬 (default: "id,desc")
- `search` (string, optional) - 검색어 (제목/내용에서 검색)
- `lang` (string, optional) - 언어 설정 (ko/en)

**Example Request**:
```bash
curl "http://localhost:8080/api/boards?page=0&size=5&search=hello&lang=en"
```

**Example Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Hello World",
      "content": "This is my first post!",
      "authorId": 1,
      "authorName": "admin",
      "viewCount": 42,
      "createdAt": "2024-12-22T10:30:00",
      "updatedAt": "2024-12-22T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### Get Board

특정 게시글을 조회합니다. 조회 시 비동기로 조회수가 증가합니다.

```http
GET /api/boards/{id}
```

**Path Parameters**:
- `id` (long) - 게시글 ID

**Example Request**:
```bash
curl "http://localhost:8080/api/boards/1?lang=en"
```

**Example Response** (200 OK):
```json
{
  "id": 1,
  "title": "Hello World",
  "content": "This is my first post with detailed content!",
  "authorId": 1,
  "authorName": "admin", 
  "viewCount": 43,
  "createdAt": "2024-12-22T10:30:00",
  "updatedAt": "2024-12-22T10:30:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "type": "about:blank",
  "title": "Resource Not Found", 
  "status": 404,
  "detail": "Board not found with ID: 999",
  "instance": "/api/boards/999"
}
```

### Create Board

새로운 게시글을 작성합니다. 인증이 필요합니다.

```http
POST /api/boards
Content-Type: application/x-www-form-urlencoded
Cookie: JSESSIONID=...
```

**Request Body** (Form Data):
```
title=Hello World
content=This is my first post!
```

**Example Request** (세션 쿠키):
```bash
curl -X POST "http://localhost:8080/api/boards" \
  -b cookies.txt \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "title=Hello World&content=This is my first post!"
```

**Example Response** (201 Created):
```json
{
  "id": 5,
  "title": "Hello World",
  "content": "This is my first post!",
  "authorId": 1,
  "authorName": "admin",
  "viewCount": 0,
  "createdAt": "2024-12-22T15:45:00",
  "updatedAt": "2024-12-22T15:45:00"
}
```

### Update Board

기존 게시글을 수정합니다. 작성자 또는 관리자만 가능합니다.

```http
PUT /api/boards/{id}
Content-Type: application/x-www-form-urlencoded
Cookie: JSESSIONID=...
```

**Path Parameters**:
- `id` (long) - 게시글 ID

**Request Body** (Form Data):
```
title=Updated Title
content=Updated content here!
```

**Example Request**:
```bash
curl -X PUT "http://localhost:8080/api/boards/1" \
  -b cookies.txt \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "title=Updated Title&content=Updated content here!"
```

**Example Response** (200 OK):
```json
{
  "id": 1,
  "title": "Updated Title", 
  "content": "Updated content here!",
  "authorId": 1,
  "authorName": "admin",
  "viewCount": 43,
  "createdAt": "2024-12-22T10:30:00",
  "updatedAt": "2024-12-22T15:50:00"
}
```

### Delete Board

게시글을 삭제합니다. 작성자 또는 관리자만 가능합니다.

```http
DELETE /api/boards/{id}
Cookie: JSESSIONID=...
```

**Example Request**:
```bash
curl -X DELETE "http://localhost:8080/api/boards/1" -b cookies.txt
```

**Example Response** (204 No Content):
```
(빈 응답)
```

## 👤 Users API

사용자 관련 API 엔드포인트

### Create User (Register)

새로운 사용자를 등록합니다.

```http
POST /api/users
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "newuser",
  "password": "password123",
  "name": "홍길동",
  "email": "newuser@example.com"
}
```

**Example Response** (201 Created):
```json
{
  "id": 3,
  "username": "newuser",
  "name": "홍길동", 
  "email": "newuser@example.com",
  "roles": ["USER"],
  "createdAt": "2024-12-22T16:00:00",
  "lastLogin": null
}
```

### Get Users List

전체 사용자 목록을 조회합니다. 관리자 권한이 필요합니다.

```http
GET /api/users
Cookie: JSESSIONID=...
```

**Example Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin",
      "name": "관리자",
      "email": "admin@boardhole.com", 
      "roles": ["ADMIN"],
      "createdAt": "2024-12-22T09:00:00",
      "lastLogin": "2024-12-22T16:00:00"
    }
  ],
  "totalElements": 1
}
```

### Get User

특정 사용자 정보를 조회합니다.

```http
GET /api/users/{id}
```

### Update User

사용자 정보를 수정합니다. 본인 또는 관리자만 가능합니다.

```http
PUT /api/users/{id}
Content-Type: application/json
Cookie: JSESSIONID=...
```

**Request Body**:
```json
{
  "name": "수정된 이름",
  "email": "newemail@example.com"
}
```

## 🔑 Auth API

인증 관련 API 엔드포인트

### Login / Logout

로그인은 Form URL Encoded 요청으로 수행되며, 성공 시 세션 쿠키가 발급됩니다.

```http
POST /api/auth/login
Content-Type: application/x-www-form-urlencoded
```

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -c cookies.txt \
  -d 'username=admin&password=admin123'

# 로그아웃 (세션 종료)
curl -X POST http://localhost:8080/api/auth/logout -b cookies.txt
```

### Get Current User

현재 인증된 사용자의 정보를 조회합니다.

```http
GET /api/auth/me
Cookie: JSESSIONID=...
```

**Example Response** (200 OK):
```json
{
  "id": 1,
  "username": "admin",
  "name": "관리자",
  "email": "admin@boardhole.com",
  "roles": ["ADMIN"],
  "lastLogin": "2024-12-22T16:00:00"
}
```

## ✉️ Email API

이메일 인증 관련 엔드포인트

### Verify Email

사용자 이메일을 토큰으로 검증합니다.

```http
GET /api/users/{id}/email/verify?token=abc123
```

### Resend Verification Email

미인증 사용자에게 인증 이메일을 다시 보냅니다.

```http
POST /api/users/{id}/email/resend
```

## 👑 Admin API

관리자 전용 API 엔드포인트

### Admin Dashboard

관리자 대시보드 정보를 조회합니다.

```http
GET /api/admin/dashboard
Cookie: JSESSIONID=...
```

**Response** (200 OK):
```json
{
  "totalUsers": 10,
  "totalBoards": 50,
  "recentUsers": [...],
  "recentBoards": [...],
  "systemStats": {
    "uptime": "2 days 5 hours",
    "memoryUsage": "45%",
    "activeUsers": 5
  }
}
```

## ❌ Error Handling

### HTTP Status Codes

| Status Code | Description | When Used |
|-------------|-------------|-----------|
| 200 | OK | 성공적인 GET, PUT 요청 |
| 201 | Created | 성공적인 POST 요청 (리소스 생성) |
| 204 | No Content | 성공적인 DELETE 요청 |
| 400 | Bad Request | 잘못된 요청 형식 또는 유효성 검증 실패 |
| 401 | Unauthorized | 인증 실패 (로그인 필요) |
| 403 | Forbidden | 권한 없음 (인증은 됐지만 접근 권한 없음) |
| 404 | Not Found | 요청한 리소스를 찾을 수 없음 |
| 409 | Conflict | 데이터 충돌 (중복 사용자명, 이메일 등) |
| 422 | Unprocessable Entity | 유효성 검증 실패 |
| 500 | Internal Server Error | 서버 내부 오류 |

### Error Response Examples

#### Validation Error (400 Bad Request)
```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "입력값 검증에 실패했습니다",
  "instance": "/api/boards",
  "errors": [
    {
      "field": "title",
      "rejectedValue": "",
      "message": "게시글 제목은 필수입니다"
    },
    {
      "field": "content", 
      "rejectedValue": "",
      "message": "게시글 내용은 필수입니다"
    }
  ]
}
```

#### Authentication Error (401 Unauthorized)
```json
{
  "type": "about:blank",
  "title": "Unauthorized", 
  "status": 401,
  "detail": "Authentication required",
  "instance": "/api/boards"
}
```

#### Authorization Error (403 Forbidden)
```json
{
  "type": "about:blank",
  "title": "Access Denied",
  "status": 403, 
  "detail": "접근이 거부되었습니다",
  "instance": "/api/boards/1"
}
```

#### Not Found Error (404 Not Found)
```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "게시글을 찾을 수 없습니다. ID: 999", 
  "instance": "/api/boards/999"
}
```

#### Conflict Error (409 Conflict)
```json
{
  "type": "about:blank",
  "title": "Duplicate Username",
  "status": 409,
  "detail": "이미 사용 중인 사용자명입니다",
  "instance": "/api/users"
}
```

## 📝 Examples

### Complete Workflow Example

1. **사용자 등록**:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "name": "테스트 사용자", 
    "email": "test@example.com"
  }'
```

2. **로그인**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -c cookies.txt \
  -d 'username=testuser&password=test123'
```

3. **게시글 작성**:
```bash
curl -X POST http://localhost:8080/api/boards \
  -b cookies.txt \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "title=My First Post&content=Hello everyone!"
```

4. **게시글 목록 조회**:
```bash
curl "http://localhost:8080/api/boards?page=0&size=10"
```

5. **게시글 조회** (조회수 증가):
```bash
curl "http://localhost:8080/api/boards/1"
```

### Testing with Different Languages

```bash
# 한국어 에러 메시지
curl "http://localhost:8080/api/boards/999?lang=ko"
# Response: "게시글을 찾을 수 없습니다. ID: 999"

# 영어 에러 메시지  
curl "http://localhost:8080/api/boards/999?lang=en"
# Response: "Board not found with ID: 999"
```

### Postman Collection

API 테스트를 위한 Postman Collection은 [여기](./postman/Board-Hole.postman_collection.json)에서 다운로드할 수 있습니다.

## 🔧 Rate Limiting

현재 Rate Limiting은 구현되지 않았습니다. 향후 버전에서 추가될 예정입니다.

**계획된 제한**:
- 로그인 시도: 5회/분
- API 호출: 100회/분 (인증된 사용자)
- 게시글 작성: 10회/시간

## 📊 API Versioning

현재는 단일 버전 API를 제공하며, 향후 버전 관리 전략:

- **URL Versioning**: `/api/v1/boards`, `/api/v2/boards`
- **Header Versioning**: `Accept: application/vnd.boardhole.v1+json`
- **Backward Compatibility**: 최소 2개 버전 지원

---

**💡 Tip**: 더 자세한 API 탐색은 [Swagger UI](http://localhost:8080/swagger-ui/index.html)를 사용하세요!
