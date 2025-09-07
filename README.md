# Board Hole - Spring Boot 게시판 애플리케이션

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
- **이메일 인증**: 회원가입 시 이메일 검증
- **권한 관리**: 사용자/관리자 역할 구분
- **API 문서**: Swagger UI 제공 (`/swagger-ui/index.html`)

## 🏗️ 기술 스택

- **Backend**: Spring Boot 3.5.5, Java 21
- **Database**: MySQL 9.4 (Docker)
- **Session**: Redis (Docker)
- **Build**: Gradle 8.14
- **Testing**: JUnit 5, Testcontainers, RestAssured
- **Quality**: Checkstyle, PMD, SpotBugs, JaCoCo, SonarCloud

## 📁 프로젝트 구조

```
src/main/java/bunny/boardhole/
├── auth/          # 인증/인가
├── board/         # 게시판
├── email/         # 이메일
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
# 전체 테스트
./gradlew test

# 단위 테스트만
./gradlew test --tests "*Test"

# 통합 테스트
./gradlew integrationTest

# E2E 테스트
./gradlew e2eTest

# 테스트 커버리지 리포트
./gradlew jacocoTestReport
```

## 🔧 유용한 명령어

```bash
# 품질 검사 (Checkstyle, PMD, SpotBugs)
./gradlew qualityCheck

# SonarCloud 분석
./gradlew sonarAnalysis

# Docker 컨테이너 종료
docker-compose down

# Docker 컨테이너 및 볼륨 완전 삭제
docker-compose down -v
```

## 📄 라이선스

MIT License

## 📚 개발 가이드

### 상세한 테스트 명령어

```bash
# 특정 패키지 테스트
./gradlew test --tests "bunny.boardhole.board.*"

# 특정 테스트 클래스 실행
./gradlew test --tests BoardControllerTest

# 모든 테스트 스위트 실행
./gradlew allTests          # unit + integration + e2e

# 커버리지 검증
./gradlew jacocoTestCoverageVerification
```

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

## 📞 문의

프로젝트 관련 문의사항은 Issues 탭을 이용해주세요.
