# Board-Hole 애플리케이션 설정 가이드

## 📋 목차
- [설정 구조](#설정-구조)
- [프로필별 설정](#프로필별-설정)
- [환경변수 설정](#환경변수-설정)
- [로컬 개발 설정](#로컬-개발-설정)
- [실행 방법](#실행-방법)
- [문제 해결](#문제-해결)

## 설정 구조

애플리케이션 설정은 Spring Boot의 프로필 기반 구조를 따릅니다:

```
src/main/resources/
├── application.yml           # 기본 공통 설정
├── application-dev.yml       # 개발 환경 설정
├── application-prod.yml      # 운영 환경 설정
└── application-local.yml     # 로컬 개발자 설정 (Git 제외)

src/test/resources/
└── application-test.yml      # 테스트 환경 설정
```

### 설정 우선순위
1. 환경변수
2. 명령줄 인자
3. application-{profile}.yml (활성 프로필)
4. application.yml (기본 설정)

## 프로필별 설정

### 🏠 기본 설정 (application.yml)
- 모든 환경에서 공유되는 공통 설정
- 애플리케이션 이름, 메시지 소스
- JPA 기본 설정 (배치 크기, 정렬 등)
- CORS 기본 정책
- 기본 사용자 템플릿 (비밀번호 제외)

### 🛠️ 개발 환경 (dev)
**활성화**: `spring.profiles.active=dev` (기본값)

**특징**:
- Docker Compose 자동 실행
- MySQL 로컬 개발 DB (포트: 13306)
- JPA DDL 자동 업데이트
- SQL 쿼리 로깅 활성화
- 디버그 로그 레벨
- 로컬호스트 CORS 허용

**필요 환경**:
- Docker Desktop 설치 및 실행
- MySQL 컨테이너 자동 시작

### 🧪 테스트 환경 (test)
**활성화**: 테스트 실행시 자동

**특징**:
- H2 인메모리 데이터베이스
- 스키마 자동 생성
- 최소 로깅 (WARN 레벨)
- 격리된 테스트 환경

### 🚀 운영 환경 (prod)
**활성화**: `spring.profiles.active=prod`

**특징**:
- 환경변수 기반 설정
- 스키마 검증만 수행 (변경 없음)
- 보안 강화 (HTTPS, 세션 보안)
- 파일 로깅 활성화
- 에러 정보 숨김

## 환경변수 설정

### 운영 환경 필수 환경변수

| 환경변수 | 설명 | 예시 |
|---------|------|------|
| `DATABASE_URL` | 데이터베이스 연결 URL | `jdbc:mysql://db.example.com:3306/boardhole` |
| `DATABASE_USERNAME` | DB 사용자명 | `boardhole_user` |
| `DATABASE_PASSWORD` | DB 비밀번호 | `strong_password_here` |
| `ADMIN_PASSWORD` | 관리자 기본 비밀번호 | `Admin@2024!` |
| `USER_PASSWORD` | 일반 사용자 기본 비밀번호 | `User@2024!` |
| `CORS_ALLOWED_ORIGINS` | 허용된 CORS 오리진 | `https://boardhole.com,https://www.boardhole.com` |

### 선택적 환경변수

| 환경변수 | 설명 | 기본값 |
|---------|------|--------|
| `ADMIN_EMAIL` | 관리자 이메일 | `admin@boardhole.com` |
| `USER_EMAIL` | 일반 사용자 이메일 | `user@boardhole.com` |
| `LOG_PATH` | 로그 파일 경로 | `/var/log/boardhole` |

### 환경변수 설정 방법

#### Linux/Mac
```bash
export DATABASE_URL="jdbc:mysql://localhost:3306/boardhole"
export DATABASE_USERNAME="root"
export DATABASE_PASSWORD="password"
export ADMIN_PASSWORD="admin123"
export USER_PASSWORD="user123"
export CORS_ALLOWED_ORIGINS="https://example.com"
```

#### Windows
```cmd
set DATABASE_URL=jdbc:mysql://localhost:3306/boardhole
set DATABASE_USERNAME=root
set DATABASE_PASSWORD=password
set ADMIN_PASSWORD=admin123
set USER_PASSWORD=user123
set CORS_ALLOWED_ORIGINS=https://example.com
```

#### Docker Compose
```yaml
environment:
  - DATABASE_URL=jdbc:mysql://db:3306/boardhole
  - DATABASE_USERNAME=boardhole
  - DATABASE_PASSWORD=${DB_PASSWORD}
  - ADMIN_PASSWORD=${ADMIN_PASSWORD}
  - USER_PASSWORD=${USER_PASSWORD}
  - CORS_ALLOWED_ORIGINS=https://boardhole.com
```

## 로컬 개발 설정

### application-local.yml 사용법

1. **템플릿 복사**
   ```bash
   cp src/main/resources/application-local.yml.template \
      src/main/resources/application-local.yml
   ```

2. **개인 설정 수정**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/my_local_db
       username: myuser
       password: mypassword
   
   server:
     port: 8081  # 다른 포트 사용
   
   logging:
     level:
       bunny.boardhole: TRACE  # 상세 로깅
   ```

3. **프로필 활성화**
   ```bash
   # 방법 1: application.yml 수정
   spring.profiles.active: local
   
   # 방법 2: 실행 인자
   java -jar app.jar --spring.profiles.active=local
   
   # 방법 3: 환경변수
   export SPRING_PROFILES_ACTIVE=local
   ```

## 실행 방법

### Gradle 사용
```bash
# 개발 환경 (기본)
./gradlew bootRun

# 특정 프로필 지정
./gradlew bootRun --args='--spring.profiles.active=prod'

# 테스트 실행
./gradlew test
```

### JAR 파일 실행
```bash
# 빌드
./gradlew build

# 개발 환경
java -jar build/libs/board-hole.jar

# 운영 환경
java -jar -Dspring.profiles.active=prod build/libs/board-hole.jar

# 로컬 개발 환경
java -jar -Dspring.profiles.active=local build/libs/board-hole.jar
```

### IDE 실행 (IntelliJ IDEA)
1. Run Configuration 생성
2. Active profiles 설정: `dev`, `local`, 또는 `prod`
3. 환경변수 추가 (prod의 경우)
4. 실행

## 문제 해결

### 일반적인 문제

#### 1. Docker Compose 연결 실패
```
문제: Connection refused to localhost:13306
해결: 
- Docker Desktop 실행 확인
- docker-compose.yml 파일 확인
- 포트 충돌 확인: lsof -i :13306
```

#### 2. 운영 환경 시작 실패
```
문제: Required environment variable not found
해결:
- 모든 필수 환경변수 설정 확인
- 환경변수 이름 오타 확인
- export 명령으로 환경변수 확인
```

#### 3. 테스트 실행 실패
```
문제: Test database not found
해결:
- H2 의존성 확인
- application-test.yml 파일 존재 확인
- 테스트 프로필 자동 활성화 확인
```

### 설정 검증 스크립트

```bash
#!/bin/bash
# validate-config.sh

echo "Checking configuration..."

# 필수 파일 확인
files=(
  "src/main/resources/application.yml"
  "src/main/resources/application-dev.yml"
  "src/main/resources/application-prod.yml"
  "src/test/resources/application-test.yml"
)

for file in "${files[@]}"; do
  if [ -f "$file" ]; then
    echo "✅ $file exists"
  else
    echo "❌ $file not found"
  fi
done

# 운영 환경변수 확인 (prod 프로필일 경우)
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
  echo "Checking production environment variables..."
  
  required_vars=(
    "DATABASE_URL"
    "DATABASE_USERNAME"
    "DATABASE_PASSWORD"
    "ADMIN_PASSWORD"
    "USER_PASSWORD"
    "CORS_ALLOWED_ORIGINS"
  )
  
  for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
      echo "❌ $var is not set"
    else
      echo "✅ $var is set"
    fi
  done
fi
```

## 보안 주의사항

⚠️ **중요한 보안 사항**:
- 운영 환경 비밀번호는 절대 코드에 하드코딩하지 마세요
- application-local.yml은 Git에 커밋하지 마세요
- 환경변수는 안전한 방법으로 관리하세요 (예: AWS Secrets Manager, Vault)
- 운영 환경에서는 HTTPS를 반드시 사용하세요
- 정기적으로 비밀번호를 변경하세요

## 추가 리소스

- [Spring Boot 외부 설정](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Spring Boot 프로퍼티](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)