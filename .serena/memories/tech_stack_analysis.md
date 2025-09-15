# 기술 스택 및 의존성 분석

## 핵심 Spring Boot 의존성
- **Spring Boot**: 3.5.5 (최신 안정버전)
- **Java**: 21 (LTS, 최신)
- **Gradle**: 8.14

## 주요 의존성 분석

### Core Framework
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - JPA/Hibernate
- `spring-boot-starter-validation` - Bean Validation
- `spring-boot-starter-aop` - AOP 지원
- `spring-boot-starter-security` - Spring Security
- `spring-boot-starter-data-redis` - Redis 연동

### 세션 관리
- `spring-session-data-redis` - Redis 기반 세션 클러스터링

### 데이터베이스
- `mysql-connector-j:8.4.0` - MySQL 8.4 드라이버
- Docker Compose 자동 MySQL/Redis 시작

### API 문서화
- `springdoc-openapi-starter-webmvc-ui:2.8.12` - Swagger UI

### 코드 생성 및 품질
- `lombok:1.18.40` - 보일러플레이트 코드 감소
- `mapstruct:1.6.3` - 타입 안전 매핑
- `jspecify:1.0.0` - Null 안전성

### 테스트
- `spring-boot-starter-test` - JUnit 5, Mockito
- `spring-security-test` - Security 테스트
- `spring-boot-testcontainers` - 통합 테스트
- `testcontainers:mysql` - MySQL 테스트 컨테이너
- `rest-assured:5.5.6` - API 테스트

### 로깅
- `logstash-logback-encoder:8.1` - JSON 로깅 (prod)

### 개발 도구
- `spring-boot-devtools` - 개발 편의
- `spring-boot-docker-compose` - Docker 자동 시작

## 품질 관리 도구
- **Qodana**: JetBrains 코드 품질 분석 (qodana.yaml)
- **SonarCloud**: 코드 품질 및 보안 분석 (언급됨)
- **JSpecify**: 모든 패키지에 @NullMarked 적용

## 아키텍처 특징
- **멀티 프로파일**: dev(기본), prod
- **Docker 통합**: 완전 자동화된 로컬 개발 환경
- **세션 클러스터링**: Redis 기반 확장 가능
- **타입 안전성**: Null 안전성 + MapStruct
- **테스트 전략**: 단위 + E2E + 컨테이너 테스트

## 성능 최적화 가능성
- 테스트 병렬 실행: `maxParallelForks = Runtime.runtime.availableProcessors()`
- Resource filtering으로 빌드 시점 변수 치환
- Docker Compose 동적 포트로 개발 환경 격리