# 코드 품질 및 스타일 가이드

## Null 안전성
- **JSpecify**: 모든 패키지에 `@NullMarked` 적용
- 모든 package-info.java 파일에 null 안전성 선언
- 컴파일 시점 null 체크 보장

## Lombok 사용 패턴
- **생성자**: `@RequiredArgsConstructor` (final 필드용)
- **로깅**: `@Slf4j` (모든 서비스/컨트롤러)
- **데이터 클래스**: `@Getter`, `@Builder`, `@NoArgsConstructor`
- **유틸리티**: `@UtilityClass`, `@NoArgsConstructor(access = PRIVATE)`
- **예외**: `@StandardException` (커스텀 예외)

## Import 스타일
- Java 표준 라이브러리 → Jakarta → Third Party → 프로젝트 내부
- 패키지별 그룹화 및 알파벳 순 정렬
- import 문 명시적 사용 (와일드카드 최소화)

## 아키텍처 규칙
- **패키지 구조**: presentation → application → domain → infrastructure
- **검증**: @Valid* (필수), @Optional* (선택적)
- **매핑**: MapStruct 2단계 (Entity ↔ Result ↔ Response)
- **CQRS**: Command/Query 분리

## 코딩 규칙

### 네이밍
- **클래스**: PascalCase
- **메서드/변수**: camelCase  
- **상수**: UPPER_SNAKE_CASE
- **패키지**: lowercase

### 검증 어노테이션
- **필수 검증**: @Valid* (생성 시)
- **선택적 검증**: @Optional* (업데이트 시)
- **도메인 특화**: UserValidationConstants, BoardValidationConstants

### 보안
- **권한**: @PreAuthorize 메서드 레벨
- **인증**: AppUserPrincipal 커스텀 Principal
- **세션**: Redis 기반 클러스터링

## 품질 도구

### 정적 분석
- **Qodana**: JetBrains 코드 품질 (Java 21)
- **SonarCloud**: 코드 커버리지 및 품질 메트릭
- **IntelliJ IDEA**: 내장 inspection

### 테스트 전략
- **단위 테스트**: Mockito 기반
- **통합 테스트**: @SpringBootTest
- **E2E 테스트**: @Tag("e2e") + RestAssured + Testcontainers
- **아키텍처 테스트**: ArchUnit으로 계층 준수 검증

## 문서화
- **API**: SpringDoc OpenAPI 3 (Swagger UI)
- **JavaDoc**: 공개 API 메서드에 필수
- **README**: 실행 가이드 상세 제공

## 로깅 전략
- **개발**: 콘솔 출력 + SQL 로깅
- **운영**: JSON 형태 (Logstash encoder)
- **보안**: SecurityLoggingAspect로 인증/권한 로깅
- **비즈니스**: BusinessLogAspect로 주요 액션 로깅

## 예외 처리
- **RFC 7807**: Problem Details 표준 준수
- **전역 핸들러**: GlobalExceptionHandler 중앙화
- **커스텀 예외**: @StandardException 활용
- **세분화**: ValidationException, ConflictException, ResourceNotFoundException

## 성능 고려사항
- **테스트**: 병렬 실행 (maxParallelForks)
- **JPA**: 지연 로딩 기본
- **세션**: Redis 클러스터링으로 확장성
- **Docker**: 동적 포트로 개발 환경 격리