# Boardholes - Spring Boot 게시판 애플리케이션

## 🚀 빠른 시작 (아무것도 설정할 필요 없음!)

**Docker 데몬만 실행되어 있으면 모든 것이 자동으로 설정됩니다!**

```bash
# 1. Docker 데몬이 실행 중인지 확인 (Docker Desktop 실행)
docker --version  # Docker가 실행 중이면 버전이 표시됨

# 2. 애플리케이션 실행 (MySQL + Redis 자동으로 시작됨)
./gradlew bootRun

# 3. 브라우저에서 접속
http://localhost:8080
```

**끝! 애플리케이션이 필요한 모든 Docker 컨테이너를 자동으로 실행합니다.** 🎉

Docker Compose가 자동으로 처리하는 것들:

- ✅ MySQL 이미지 자동 다운로드 및 실행 (동적 포트)
- ✅ Redis 이미지 자동 다운로드 및 실행 (동적 포트)
- ✅ 데이터베이스 스키마 자동 생성
- ✅ 모든 환경 변수 자동 설정
- ✅ 네트워크 및 볼륨 자동 구성

**별도로 설치할 필요 없는 것들:**

- ❌ MySQL 설치 불필요
- ❌ Redis 설치 불필요
- ❌ Docker 이미지 수동 다운로드 불필요
- ❌ 데이터베이스 생성 불필요
- ❌ 환경 변수 설정 불필요

## 📋 유일한 필수 요구사항

- **Java 21**
- **Docker Desktop** (또는 Docker 데몬만 실행 중이면 됨)
- 그게 전부입니다!

## 🛠️ 주요 기능

- **게시판 CRUD**: 게시글 작성, 수정, 삭제, 조회
- **사용자 인증**: 세션 기반 로그인/로그아웃

- **권한 관리**: 사용자/관리자 역할 구분
- **API 문서**: Swagger UI 제공 (`/swagger-ui/index.html`)
- **표준 HTTP 응답**: RFC 7807 Problem Details 준수

## 🔒 기본 계정과 보안 안내

본 애플리케이션은 모든 프로필(운영 포함)에서 기본 계정들을 멱등하게 생성합니다. 이는 온보딩과 데모/E2E 테스트 편의를 위한 의도된 동작입니다. 기본 비밀번호의 변경·회전 책임은 배포/운영자에게 있습니다.

- 기본 계정(기본값은 `application.yml`에서 설정, 환경별 오버라이드 권장)
    - Admin: `admin` / `Admin123!` (ROLE_ADMIN)
    - User: `user` / `User123!` (ROLE_USER)
    - Anon: `anon` / `Anon123!` (ROLE_USER)

- 운영 환경에서의 권장 사항
    - 비밀번호를 반드시 환경별로 오버라이드하세요 (예: `application-prod.yml`).
    - 최초 기동 후 즉시 관리자 비밀번호를 변경하세요.

- 설정 오버라이드 예시 (YAML)

```yaml
# application-prod.yml 등
boardhole:
  default-users:
    admin:
      password: "CHANGE_ME_STRONG!"
    regular:
      password: "CHANGE_ME_STRONG!"
```

- 환경변수로 오버라이드 예시

```bash
export BOARDHOLE_DEFAULT_USERS_ADMIN_PASSWORD='CHANGE_ME_STRONG!'
export BOARDHOLE_DEFAULT_USERS_REGULAR_PASSWORD='CHANGE_ME_STRONG!'
```

- 비밀번호 변경 API (로그인 후 본인 비밀번호 변경)
    - `PATCH /api/users/{id}/password` (폼 전송)
    - 필드: `currentPassword`, `newPassword`, `confirmPassword`

## 🏗️ 기술 스택

- **Backend**: Spring Boot 3.5.5, Java 21
- **Database**: MySQL 8.4 (Docker)
- **Session**: Redis (Docker)
- **Build**: Gradle 8.14
- **Testing**: JUnit 5, Testcontainers, RestAssured
- **Quality**: Checkstyle, PMD, SpotBugs

## 📁 프로젝트 구조

```
src/main/java/bunny/boardhole/
├── auth/          # 인증/인가
├── board/         # 게시판
├── user/          # 사용자
└── shared/        # 공통 모듈
```

각 도메인은 레이어드 아키텍처 적용:

- `presentation` - REST API 컨트롤러
- `application` - 비즈니스 로직
- `domain` - 엔티티 및 도메인 규칙
- `infrastructure` - 데이터 접근

## 🧪 테스트 실행

```bash
# 모든 테스트
./gradlew test
```

## 🔧 유용한 명령어

```bash
# Docker 컨테이너 종료
docker-compose down

# Docker 컨테이너 및 볼륨 완전 삭제
docker-compose down -v
```

## 🚢 릴리스 & Docker 배포 자동화

릴리스 파이프라인은 [JReleaser](https://jreleaser.org) + GitHub Actions 조합으로 구성되어 있습니다. 릴리스 워크플로우는 다음 단계를 자동으로 수행합니다.

1. `./gradlew clean build`로 애플리케이션을 빌드/테스트
2. `./gradlew jreleaserFullRelease` 실행으로 버전 확정, CHANGELOG 생성, Git 태그 및 GitHub Release 작성
3. 동일한 CHANGELOG를 OCI 레이블에 포함한 채 멀티 아키텍처 Docker 이미지를 빌드하여 GHCR(`ghcr.io/Bunnyholes/Boardhole`)에 푸시

### 준비 사항

별도의 시크릿을 등록할 필요는 없습니다. GitHub Actions가 제공하는 기본 `GITHUB_TOKEN`으로
릴리스 작성과 GHCR 푸시를 모두 처리합니다. (워크플로 권한은 `contents: write`, `packages: write`로 설정됨)

### 릴리스 실행

1. GitHub Actions에서 **`Release` 워크플로우**를 선택합니다.
2. `Run workflow` 버튼을 눌러 릴리스 버전(ex: `1.6.0`)을 입력하고 실행합니다.
3. 워크플로우가 종료되면 다음 결과물을 확인할 수 있습니다.
    - Git 태그 및 Release 페이지 (CHANGELOG 자동 포함)
    - `ghcr.io/bunnyholes/board-hole:<version>` 및 `latest` Docker 이미지
    - 릴리스 노트 전문을 담은 워크플로우 아티팩트 (`RELEASE_NOTES.md`)

> **참고**: 릴리스 워크플로우는 워킹 트리를 수정하거나 `CHANGELOG.md`를 커밋하지 않습니다. 버전/CHANGELOG 파일을 수동으로 관리하고 싶다면 별도 커밋을 추가하세요.

## 📄 라이선스

MIT License

## 📚 개발 가이드

테스트 실행은 단일 명령(`./gradlew test`)만 안내합니다. 복잡한 분리/필터링은 현재 단계에서는 문서화하지 않습니다.

### 아키텍처 구조

각 도메인은 레이어드 아키텍처를 따릅니다:

- **presentation**: REST API 컨트롤러 및 DTO
- **application**: 비즈니스 로직 (Command/Query 분리)
- **domain**: 엔티티 및 도메인 규칙
- **infrastructure**: 데이터 접근 계층

### 코드 스타일 설정

IntelliJ IDEA 자동 설정:

1. 프로젝트 열기 시 `.idea/codeStyles/` 자동 감지
2. Settings → Tools → Actions on Save 에서 다음 활성화:
    - ✅ 코드 자동 포맷팅
    - ✅ import 최적화
    - ✅ 코드 재정렬 (선택사항)

## 👥 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📋 HTTP 응답 코드

애플리케이션은 REST API 표준을 준수하여 다음과 같은 HTTP 응답 코드를 사용합니다:

- **200 OK**: 조회 성공 (GET 요청 성공)
- **201 Created**: 리소스 생성 성공 (POST 요청으로 새 데이터 생성)
- **204 No Content**: 수정/삭제 성공 (PUT/PATCH/DELETE 요청 성공, 반환 데이터 없음)
- **400 Bad Request**: 요청 형식 오류 (잘못된 JSON, 누락된 필수 필드)
- **401 Unauthorized**: 인증 실패 (로그인 필요, 세션 만료)
- **409 Conflict**: 중복 데이터 (이메일/사용자명 중복)
- **422 Unprocessable Entity**: 유효성 검증 실패 (형식은 올바르나 비즈니스 규칙 위반)

모든 오류 응답은 RFC 7807 Problem Details 표준을 따라 구조화된 JSON으로 반환됩니다.

## 📞 문의

프로젝트 관련 문의사항은 Issues 탭을 이용해주세요.
