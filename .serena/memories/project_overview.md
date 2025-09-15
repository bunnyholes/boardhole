# Board Hole - 프로젝트 개요

## 프로젝트 목적
- Spring Boot 3.5.5 기반의 웹 게시판 애플리케이션
- 게시글 CRUD, 사용자 인증/권한 관리 기능 제공
- REST API와 Swagger UI 지원
- 도메인 주도 설계(DDD) 아키텍처 적용

## 기술 스택
- **Backend**: Spring Boot 3.5.5, Java 21
- **Database**: MySQL 8.4 (Docker)
- **Session**: Redis (Docker, spring-session-data-redis)
- **Build**: Gradle 8.14 
- **Testing**: JUnit 5, Testcontainers, RestAssured 5.5.6
- **Documentation**: SpringDoc OpenAPI 2.8.12
- **Mapping**: MapStruct 1.6.3
- **Null Safety**: JSpecify 1.0.0
- **Quality**: SonarCloud, Checkstyle, PMD, SpotBugs

## 프로젝트 구조
```
src/main/java/bunny/boardhole/
├── auth/          # 인증/인가 도메인
├── board/         # 게시판 도메인  
├── user/          # 사용자 도메인
└── shared/        # 공통 모듈 (config, security, validation 등)
```

## 도메인 아키텍처
각 도메인은 4계층 구조:
- presentation → application → domain → infrastructure
- CQRS-lite 패턴 (Command/Query 분리)
- 검증: @Valid* (필수), @Optional* (선택)
- 매핑: MapStruct 2단계 (Entity ↔ Result ↔ Response)

## 보안 및 세션
- 세션 기반 인증 (Redis 세션 스토어)
- Spring Security + 메서드 레벨 @PreAuthorize
- AppUserPrincipal 커스텀 사용자 정보
- RFC 7807 Problem Details 표준 에러 응답

## 테스트 구조
- 총 180개 Java 파일
- 단위 테스트 + E2E 테스트 (@Tag("e2e"))
- Testcontainers로 실제 DB/Redis 테스트
- RestAssured로 API 테스트
- ArchUnit으로 아키텍처 검증

## 개발 환경 설정
- Docker Compose 자동 시작 (MySQL, Redis)
- 프로필: dev(기본), prod
- 기본 계정: admin/Admin123!, user/User123!, anon/Anon123!