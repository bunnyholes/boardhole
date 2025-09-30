# boardholes - Spring Boot 게시판 애플리케이션

Spring Boot 3.5.5와 Java 21을 기반으로 한 현대적인 게시판 시스템입니다.

## 📋 필수 요구사항

### Java 21
프로젝트는 Java 21을 사용합니다. 반드시 Java 21이 설치되어 있어야 합니다.

**설치 확인**:
```bash
java -version
# "openjdk version "21"" 또는 "java version "21"" 표시되어야 함
```

**설치 방법**:
- **macOS**: `brew install openjdk@21`
- **Windows**: [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) 또는 [Adoptium](https://adoptium.net/)
- **Linux**: 
  ```bash
  # Ubuntu/Debian
  sudo apt update && sudo apt install openjdk-21-jdk
  
  # RHEL/CentOS/Fedora
  sudo dnf install java-21-openjdk-devel
  ```

### Docker Desktop
데이터베이스(PostgreSQL)와 세션 스토어(Redis)가 Docker 컨테이너로 자동 관리됩니다.

**설치 확인**:
```bash
docker --version  # Docker 버전이 표시되어야 함
docker info       # Docker 데몬 실행 상태 확인
```

**설치 방법**:
1. [Docker Desktop](https://www.docker.com/products/docker-desktop/) 다운로드 및 설치
2. 설치 후 Docker Desktop 애플리케이션 실행
3. Docker 데몬이 실행 중인지 확인

## 🚀 빠른 시작

### 1단계: 환경 확인
```bash
# Java 21 설치 확인
java -version
# 출력 예시: openjdk version "21" 2024-09-16

# Docker 설치 및 실행 확인  
docker --version
# 출력 예시: Docker version 24.x.x

docker info
# Docker 데몬 정보가 표시되면 정상
```

### 2단계: 프로젝트 실행
```bash
# 저장소 클론
git clone https://github.com/bunnyholes/boardhole.git
cd boardhole

# 애플리케이션 실행 (Docker 컨테이너 자동 시작)
./gradlew bootRun
```

**자동으로 실행되는 것들**:
- ✅ PostgreSQL 17 컨테이너 시작 (포트 자동 할당)
- ✅ Redis 7 컨테이너 시작 (포트 자동 할당)
- ✅ 데이터베이스 스키마 자동 생성
- ✅ 기본 사용자 계정 생성
- ✅ Spring Boot 애플리케이션 시작

### 3단계: 접속 확인
- **메인 페이지**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui/index.html
- **Actuator**: http://localhost:8080/actuator/health

## 🔒 기본 계정 정보

개발 및 테스트 편의를 위해 기본 계정들이 자동으로 생성됩니다:

| 구분 | 사용자명 | 비밀번호 | 역할 |
|------|----------|----------|------|
| 관리자 | `admin` | `Admin123!` | ROLE_ADMIN |

**⚠️ 보안 주의사항**:
- 운영 환경에서는 반드시 비밀번호를 변경하세요
- 환경 변수로 비밀번호 오버라이드 가능:
  ```bash
  export BOARDHOLE_DEFAULT_USERS_ADMIN_PASSWORD='새로운비밀번호'
  ```

## 🛠️ 주요 기능

- **게시판 CRUD**: 게시글 작성, 수정, 삭제, 조회
- **사용자 인증**: 세션 기반 로그인/로그아웃
- **권한 관리**: 사용자/관리자 역할 구분
- **API 문서**: Swagger UI 제공
- **표준 HTTP 응답**: RFC 7807 Problem Details 준수
- **국제화**: 한국어/영어 메시지 지원

## 🏗️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 21
- **Build Tool**: Gradle 9.1
- **Security**: Spring Security (세션 기반)

### Database & Cache
- **Database**: PostgreSQL 17 (Docker 자동 관리)
- **Session Store**: Redis 7 (Docker 자동 관리)
- **ORM**: Spring Data JPA + Hibernate

### Testing & Quality
- **Testing**: JUnit 5, H2 (in-memory), Spring MockMvc
- **Quality**: SonarCloud, IntelliJ 코드 검사
- **Architecture Testing**: ArchUnit

### Frontend
- **Template Engine**: Thymeleaf
- **Security Integration**: Thymeleaf Spring Security

## 📁 프로젝트 구조

```
src/main/java/dev/xiyo/bunnyholes/boardhole/
├── auth/          # 인증/인가
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   └── presentation/
├── board/         # 게시판
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   └── presentation/
├── user/          # 사용자 관리
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   └── presentation/
├── shared/        # 공통 모듈
│   ├── config/
│   ├── constants/
│   ├── exception/
│   └── security/
└── web/           # Thymeleaf 기반 웹 뷰와 정적 리소스 조립
    ├── presentation/
    └── view/
```

**레이어드 아키텍처**:
- `presentation` - REST API 컨트롤러 및 웹 컨트롤러
- `application` - 비즈니스 로직 (Command/Query 분리)
- `domain` - 엔티티 및 도메인 규칙
- `infrastructure` - 데이터 접근 계층

## 🧪 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test
```

**테스트 구성**:
- **Unit Tests**: 서비스/도메인 로직을 Mock 기반으로 검증
- **MVC Tests**: `@WebMvcTest` + MockMvc 로 REST 컨트롤러를 검증
- **Integration Tests**: H2 인메모리 데이터베이스 기반 JPA/Repository 테스트
- **Architecture Tests**: ArchUnit으로 아키텍처 규칙 검증
- **Coverage**: 현재 별도 커버리지 리포트는 제공되지 않습니다.

## 🔧 유용한 명령어

### Gradle 명령어
```bash
# 애플리케이션 실행
./gradlew bootRun

# 빌드 (테스트 포함)
./gradlew build

# 빌드 (테스트 제외)  
./gradlew build -x test

# 종료
Ctrl+C (gradlew bootRun 실행 중인 터미널에서)
```

### Docker 관리
```bash
# 실행 중인 컨테이너 확인
docker ps

# Docker 컨테이너 종료 (애플리케이션 종료 시 자동)
docker compose -f docker-compose.infra.yml down

# 컨테이너 및 볼륨 완전 삭제
docker compose -f docker-compose.infra.yml down -v

# Docker 시스템 정리
docker system prune
```

## 🖥️ 개발 환경 권장사항

### IDE 설정
**IntelliJ IDEA** (Ultimate 또는 Community):
1. Java 21 SDK 설정 확인
2. Docker 플러그인 활성화
3. Lombok 플러그인 설치
4. 코드 스타일 자동 적용:
   - Settings → Tools → Actions on Save
   - ✅ 코드 자동 포맷팅
   - ✅ import 최적화

### 성능 최적화
- **Docker Desktop**: 메모리 4GB 이상 할당 권장
- **Gradle JVM**: `-Xmx2g` (gradle.properties에 설정됨)
- **IDE JVM**: `-Xmx4g` 권장

## 🔧 문제 해결

### Java 관련 오류
```bash
# Java 21이 설치되지 않은 경우
Error: JAVA_HOME is not defined correctly.

# 해결 방법:
# 1. Java 21 설치
# 2. JAVA_HOME 환경 변수 설정
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk     # Linux
```

### Docker 관련 오류
```bash
# Docker 데몬이 실행되지 않은 경우
Cannot connect to the Docker daemon

# 해결 방법:
# macOS/Windows: Docker Desktop 애플리케이션 실행
# Linux: sudo systemctl start docker
```

### 포트 충돌 오류
```bash
# 8080 포트가 이미 사용 중인 경우
Port 8080 is already in use

# 해결 방법:
# 1. 다른 애플리케이션 종료
lsof -ti:8080 | xargs kill -9

# 2. 또는 다른 포트 사용
./gradlew bootRun --args='--server.port=8081'
```

## 📋 HTTP 응답 코드

애플리케이션은 REST API 표준을 준수합니다:

| 코드 | 의미 | 사용 예시 |
|------|------|-----------|
| **200 OK** | 조회 성공 | GET 요청 성공 |
| **201 Created** | 생성 성공 | POST 요청으로 새 데이터 생성 |
| **204 No Content** | 수정/삭제 성공 | PUT/PATCH/DELETE 성공, 반환 데이터 없음 |
| **400 Bad Request** | 요청 형식 오류 | 잘못된 JSON, 누락된 필수 필드 |
| **401 Unauthorized** | 인증 실패 | 로그인 필요, 세션 만료 |
| **409 Conflict** | 중복 데이터 | 이메일/사용자명 중복 |
| **422 Unprocessable Entity** | 유효성 검증 실패 | 비즈니스 규칙 위반 |

모든 오류 응답은 **RFC 7807 Problem Details** 표준을 따라 구조화된 JSON으로 반환됩니다.

## 👥 기여하기

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 코딩 컨벤션
- **Null Safety**: 모든 패키지에 `@NullMarked` 적용
- **국제화**: 사용자 대상 메시지는 `messages.properties` 사용
- **검증**: 생성 시 `@Valid*`, 수정 시 `@Optional*` 어노테이션
- **매핑**: MapStruct를 사용한 2단계 매핑 (Entity ↔ Result ↔ Response)

## 📄 라이선스

MIT License

## 📞 문의

프로젝트 관련 문의사항은 [Issues](https://github.com/bunnyholes/boardhole/issues) 탭을 이용해주세요.

---

**🎉 모든 설정이 완료되었습니다!**  
`./gradlew bootRun` 실행 후 http://localhost:8080 에서 애플리케이션을 확인하세요.