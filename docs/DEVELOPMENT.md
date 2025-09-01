# Development Guide

Board-Hole 프로젝트 개발 환경 설정 및 개발 가이드

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Environment Setup](#-environment-setup)
- [IDE Configuration](#-ide-configuration)
- [Database Setup](#-database-setup)
- [Running the Application](#-running-the-application)
- [Testing](#-testing)
- [Debugging](#-debugging)
- [Development Workflow](#-development-workflow)
- [Troubleshooting](#-troubleshooting)

## 🛠 Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java** | 21+ | Runtime environment |
| **Docker** | 20.0+ | Database containerization |
| **Docker Compose** | 2.0+ | Local development services |
| **Git** | 2.20+ | Version control |

### Optional but Recommended

| Software | Version | Purpose |
|----------|---------|---------|
| **IntelliJ IDEA** | 2023.3+ | Primary IDE (권장) |
| **VS Code** | Latest | Alternative IDE |
| **Postman** | Latest | API testing |
| **MySQL Workbench** | Latest | Database management |

## 🚀 Environment Setup

### 1. Clone Repository

```bash
# HTTPS
git clone https://github.com/your-username/board-hole.git

# SSH (권장)
git clone git@github.com:your-username/board-hole.git

cd board-hole
```

### 2. Environment Variables

개발 환경에서 사용할 환경 변수를 설정합니다:

```bash
# .env 파일 생성 (선택사항)
cat > .env << EOF
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=boardhole
DB_USERNAME=boardhole
DB_PASSWORD=boardhole123

# (세션 기반 인증 사용 — JWT 불필요)

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
EOF
```

### 3. Docker Infrastructure

```bash
# 백그라운드에서 인프라 시작(MySQL:13306, Redis:16379)
docker compose up -d

# 서비스 상태 확인
docker compose ps

# 로그 확인
docker compose logs mysql
```

### 4. Gradle Build

```bash
# 의존성 다운로드 및 빌드
./gradlew build

# 빌드 스킵하고 실행 (개발 중)
./gradlew bootRun --exclude-task test
```

## 💻 IDE Configuration

### IntelliJ IDEA (권장)

#### Required Plugins
```
1. Lombok Plugin (필수)
2. Spring Assistant 
3. MapStruct Support
4. Database Navigator (선택)
```

#### Settings Configuration

**File → Settings → Editor → Code Style → Java**:
```yaml
Scheme: Google Style (권장)
Tab size: 4
Indent: 4
Continuation indent: 8
```

**File → Settings → Build → Compiler → Annotation Processors**:
```yaml
☑️ Enable annotation processing
☑️ Obtain processors from project classpath
```

**File → Settings → Tools → Actions on Save**:
```yaml
☑️ Reformat code
☑️ Optimize imports
☑️ Run code cleanup
```

#### Run Configurations

**Application Run Configuration**:
```yaml
Name: BoardHoleApplication
Main class: bunny.boardhole.BoardHoleApplication
VM options: -Dspring.profiles.active=dev
Environment variables:
  - DB_PASSWORD=boardhole123
```

**Test Run Configuration**:
```yaml
Name: All Tests
Test kind: All in package
Package: bunny.boardhole
VM options: -Dspring.profiles.active=test
```

### VS Code

#### Required Extensions
```json
{
  "recommendations": [
    "redhat.java",
    "vscjava.vscode-java-pack", 
    "pivotal.vscode-spring-boot",
    "gabrielbb.vscode-lombok",
    "ms-vscode.vscode-docker"
  ]
}
```

#### Workspace Settings (`.vscode/settings.json`)
```json
{
  "java.home": "/path/to/java-21",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/path/to/java-21"
    }
  ],
  "spring-boot.ls.java.home": "/path/to/java-21",
  "java.compile.nullAnalysis.mode": "automatic"
}
```

## 🗄 Database Setup

### Local Development (Docker)

기본적으로 Docker Compose를 사용하여 MySQL을 실행합니다:

> 참고: 루트의 `docker-compose.yml`은 `mysql:8.4` 이미지를 사용하며, 호스트 포트 `13306`으로 매핑됩니다. 아래 접속 정보 예시는 실제 구성과 일치합니다.

### Database Access

**MySQL Workbench 연결**:
```
Host: localhost
Port: 13306
Username: boardhole
Password: boardhole123
Database: boardhole
```

**Command Line 접속**:
```bash
# Docker 컨테이너에 직접 접속
docker compose exec mysql mysql -u boardhole -pboardhole123 boardhole

# 또는 로컬 MySQL 클라이언트 사용 (호스트 포트 13306)
mysql -h localhost -P 13306 -u boardhole -pboardhole123 boardhole
```

### Schema Management

애플리케이션 시작 시 자동으로 테이블이 생성되고 초기 데이터가 설정됩니다:

```java
// DataInitializer.java
@Component
public class DataInitializer {
    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        // 기본 관리자 계정 생성
        // 기본 사용자 계정 생성
        // 환영 게시글 생성
    }
}
```

## 🏃 Running the Application

### Development Mode

```bash
# 1. 데이터베이스 시작
docker compose up -d mysql

# 2. 애플리케이션 실행 (dev 프로파일)
./gradlew bootRun

# 3. 애플리케이션 확인
curl http://localhost:8080/actuator/health
```

### Different Profiles

```bash
# Development profile (기본값)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Test profile
./gradlew bootRun --args='--spring.profiles.active=test'

# Production profile (주의: 운영 DB 설정 필요)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Port Configuration

기본 포트(8080)가 사용 중인 경우:

```bash
# 다른 포트로 실행
./gradlew bootRun --args='--server.port=8090'

# 또는 환경 변수 사용
SERVER_PORT=8090 ./gradlew bootRun
```

### Hot Reload (Dev Tools)

개발 중 코드 변경 시 자동 재시작:

```gradle
// build.gradle (이미 포함됨)
dependencies {
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

## 🧪 Testing

### Test Categories

1. **Unit Tests**: 개별 클래스/메서드 테스트
2. **Integration Tests**: 여러 컴포넌트 통합 테스트  
3. **Web Layer Tests**: Controller 테스트 (`@WebMvcTest`)
4. **Data Layer Tests**: Repository 테스트 (`@DataJpaTest`)

### Running Tests

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests BoardControllerTest

# 특정 테스트 메서드 실행
./gradlew test --tests BoardControllerTest.create_Success

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### Test Profiles

```yaml
# src/test/resources/application.properties
spring.profiles.active=test
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.springframework.web=DEBUG
```

### Integration Test Setup

```java
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

### Test Data Setup

```java
@TestConfiguration
public class TestDataConfig {
    
    @Bean
    @Primary
    public DataInitializer testDataInitializer() {
        return new DataInitializer() {
            @Override
            protected void initializeDefaultUsers() {
                // 테스트용 사용자 생성
            }
        };
    }
}
```

## 🐛 Debugging

### Application Debugging

**IntelliJ IDEA**:
1. Run → Edit Configurations
2. Add new → Spring Boot
3. Enable "Debug mode"
4. Set breakpoints in code
5. Start debugging

**VS Code**:
```json
// .vscode/launch.json
{
  "type": "java",
  "name": "Debug BoardHole",
  "request": "launch",
  "mainClass": "bunny.boardhole.BoardHoleApplication",
  "args": "--spring.profiles.active=dev",
  "vmArgs": "-Dserver.port=8080"
}
```

### Database Debugging

**Query Logging**:
```yaml
# application-dev.properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Connection Pool Monitoring**:
```yaml
# HikariCP 로깅
logging.level.com.zaxxer.hikari=DEBUG
spring.datasource.hikari.leak-detection-threshold=60000
```

### HTTP Request/Response Debugging

```yaml
# 요청/응답 로깅
logging.level.org.springframework.web=DEBUG
logging.level.bunny.boardhole.config.log=DEBUG
```

## 🔄 Development Workflow

### Feature Development Cycle

```bash
# 1. 최신 코드 동기화
git checkout main
git pull origin main

# 2. 새 기능 브랜치 생성
git checkout -b feature/add-board-comments

# 3. 개발 사이클 반복
while developing:
  # a) 코드 작성
  # b) 테스트 실행
  ./gradlew test
  
  # c) 애플리케이션 실행 및 수동 테스트
  ./gradlew bootRun
  
  # d) 변경사항 커밋
  git add .
  git commit -m "feat(board): Add comment model and repository"

# 4. 최종 검증
./gradlew build
./gradlew test

# 5. Push 및 PR 생성
git push origin feature/add-board-comments
```

### Code Quality Checks

```bash
# 코드 포맷팅 확인
./gradlew spotlessCheck

# 코드 포맷팅 적용
./gradlew spotlessApply

# 정적 분석 (설정된 경우)
./gradlew checkstyleMain
./gradlew pmdMain
```

### Performance Profiling

**Application Startup Time**:
```bash
# 시작 시간 측정
time ./gradlew bootRun
```

**Memory Usage Monitoring**:
```bash
# JVM 메모리 옵션 추가
export JAVA_OPTS="-Xms256m -Xmx512m -XX:+PrintGCDetails"
./gradlew bootRun
```

## 🔧 Configuration Management

### Profile-specific Properties

```
src/main/resources/
├── application.properties           # 공통 설정
├── application-dev.properties       # 개발 환경
├── application-prod.properties      # 운영 환경
└── application-test.properties      # 테스트 환경
```

### Custom Properties

```java
// Custom configuration properties
@ConfigurationProperties(prefix = "boardhole")
@Component
public class BoardHoleProperties {
    private DefaultUsers defaultUsers;
    private Cors cors;
    
    // getters and setters
}
```

### External Configuration

```bash
# 외부 설정 파일 사용
./gradlew bootRun --args='--spring.config.location=classpath:/,file:./config/'
```

## 📊 Monitoring and Observability

### Application Health

```bash
# Health check
curl http://localhost:8080/actuator/health

# Detailed health info (dev profile)
curl http://localhost:8080/actuator/health/details
```

### Metrics

```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Custom Metrics

```java
// 커스텀 메트릭 추가
@Component
public class BoardMetrics {
    
    private final Counter boardCreationCounter;
    private final Timer boardCreationTimer;
    
    public BoardMetrics(MeterRegistry meterRegistry) {
        this.boardCreationCounter = Counter.builder("board.creation.count")
            .description("Number of boards created")
            .register(meterRegistry);
            
        this.boardCreationTimer = Timer.builder("board.creation.time")
            .description("Board creation time")
            .register(meterRegistry);
    }
}
```

## 🧩 Module Development

### Adding New Domain

새로운 도메인 모듈을 추가할 때 따라야 할 구조:

```
src/main/java/bunny/boardhole/{domain}/
├── web/                           # Web Layer
│   ├── {Domain}Controller.java
│   ├── dto/
│   │   ├── {Domain}CreateRequest.java
│   │   ├── {Domain}UpdateRequest.java
│   │   └── {Domain}Response.java
│   └── mapper/
│       └── {Domain}WebMapper.java
├── application/                   # Application Layer
│   ├── command/
│   │   ├── Create{Domain}Command.java
│   │   ├── Update{Domain}Command.java
│   │   └── {Domain}CommandService.java
│   ├── query/
│   │   ├── Get{Domain}Query.java
│   │   ├── List{Domain}Query.java
│   │   └── {Domain}QueryService.java
│   ├── result/
│   │   └── {Domain}Result.java
│   ├── event/
│   │   ├── {Domain}CreatedEvent.java
│   │   └── {Domain}EventListener.java
│   └── mapper/
│       └── {Domain}Mapper.java
├── domain/
│   └── {Domain}.java
└── infrastructure/
    └── {Domain}Repository.java
```

### CQRS Implementation Checklist

새로운 기능 구현 시 체크리스트:

- [ ] **Command 정의**: 불변 record 타입으로 정의
- [ ] **Query 정의**: 조회 조건을 담은 record 타입
- [ ] **Result 정의**: 레이어 간 데이터 전달용 record
- [ ] **CommandService**: `@Transactional` 적용
- [ ] **QueryService**: `@Transactional(readOnly = true)` 적용
- [ ] **Event 정의**: 필요시 도메인 이벤트 정의
- [ ] **EventListener**: `@Async` 비동기 처리
- [ ] **Mapper 구현**: MapStruct 매퍼 정의
- [ ] **Controller 구현**: 적절한 HTTP 메서드와 상태 코드
- [ ] **Security 설정**: `@PreAuthorize` 적용
- [ ] **Validation**: Bean Validation 적용
- [ ] **Tests**: Unit tests와 Integration tests 작성

## 🚨 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# 8080 포트 사용 중인 프로세스 확인
lsof -ti:8080

# 프로세스 종료
kill -9 $(lsof -ti:8080)

# 또는 다른 포트 사용
./gradlew bootRun --args='--server.port=8090'
```

#### Database Connection Issues
```bash
# Docker 컨테이너 상태 확인
docker compose ps

# MySQL 컨테이너 재시작
docker compose restart mysql

# 데이터베이스 로그 확인
docker compose logs mysql
```

#### Lombok Issues
```bash
# 컴파일 시 Lombok 에러
./gradlew clean build

# IDE에서 Lombok 플러그인 확인
# IntelliJ: Settings → Plugins → Lombok
```

#### Test Failures
```bash
# 테스트 데이터베이스 초기화
./gradlew cleanTest test

# 특정 프로파일로 테스트
./gradlew test -Dspring.profiles.active=test

# 병렬 테스트 비활성화 (동시성 이슈 시)
./gradlew test --max-workers=1
```

### Performance Issues

#### Slow Startup
```bash
# 시작 시간 분석
./gradlew bootRun --debug

# 불필요한 자동 설정 비활성화
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

#### Memory Issues
```bash
# Heap dump 생성
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc

# 메모리 프로파일링
java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar app.jar
```

### Development Tips

#### Fast Feedback Loop
```bash
# 테스트 없이 빠른 재시작
./gradlew bootRun --exclude-task test

# 특정 패키지만 테스트
./gradlew test --tests "bunny.boardhole.board.*"

# 빌드 캐시 활용
./gradlew build --build-cache
```

#### Debug Logging
```yaml
# application-dev.properties - 상세 로깅
logging.level.bunny.boardhole=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

#### Database Quick Reset
```bash
# 개발 데이터베이스 초기화
docker compose down -v  # 볼륨까지 삭제
docker compose up -d
./gradlew bootRun  # 자동으로 스키마 재생성
```

## 🔧 Advanced Development

### Custom Profiles

새로운 환경을 위한 프로파일 추가:

```yaml
# application-local.properties
spring.datasource.url=jdbc:mysql://localhost:3306/boardhole_local
spring.jpa.hibernate.ddl-auto=update
logging.level.bunny.boardhole=TRACE
```

### JVM Tuning

개발 환경 최적화:

```bash
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC

# JAVA_OPTS 환경 변수
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+PrintGCDetails"
```

### Build Optimization

```bash
# 병렬 빌드 활성화
./gradlew build --parallel

# 빌드 캐시 활성화
./gradlew build --build-cache

# Gradle 데몬 사용
./gradlew build --daemon
```

---

**💡 추가 도움이 필요하시면 [Issues](https://github.com/your-username/board-hole/issues)에 `question` 라벨로 문의해 주세요!**
