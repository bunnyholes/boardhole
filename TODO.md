# 📋 Spring Boot 게시판 프로젝트 TODO

## 📊 프로젝트 현황 대시보드
| 항목 | 현재 | 목표 |
|------|------|------|
| **코드 규모** | 157개 Java 파일 | - |
| **테스트 파일** | 24개 | 100개+ |
| **테스트 커버리지** | ~15% | 80% |
| **코드 라인수** | ~10,400줄 | - |
| **기술 스택** | Java 21, Spring Boot 3.5.5, MySQL, Redis, Docker | - |

---

## 🚨 우선순위별 작업 분류

### 🔴 P0 - Critical (즉시 수정 필요)
> 데이터 손실, 보안 취약점, 시스템 장애 위험

#### 1. 이메일 발송 실패 처리
- [ ] 재시도 메커니즘 구현 (EmailEventListener line 35, 56, 74)
- [ ] 실패 큐 구현 (DLQ - Dead Letter Queue)
- [ ] Spring Retry 또는 Resilience4j 도입
- [ ] 실패 알림 시스템 구축

#### 2. 테스트 커버리지 긴급 개선
- [ ] 현재 15% → 1차 목표 50% (1주)
- [ ] BoardCommandService 단위 테스트
- [ ] UserCommandService 통합 테스트
- [ ] EmailEventListener 비동기 처리 테스트
- [ ] AppPermissionEvaluator 권한 검증 테스트

#### 3. 보안 취약점 패치
- [ ] 이메일 미인증 사용자 접근 제한
- [ ] Rate Limiting 구현 (brute force 방어)
- [ ] 민감 정보 로깅 방지 (password, token)
- [ ] CSRF 토큰 검증 강화

#### 4. 트랜잭션 무결성
- [ ] @TransactionalEventListener 실패 정책
- [ ] 이벤트 처리 경계 명확화
- [ ] 데이터베이스 락 전략 최적화

---

### 🟡 P1 - Major (2주 내 처리)
> 성능 저하, 유지보수성 문제, 기능 제한

#### 5. 성능 최적화
- [ ] **캐싱 전략**
  - [ ] AppPermissionEvaluator 캐싱 (line 74 TODO)
  - [ ] Spring Cache 도입 (@Cacheable, @CacheEvict)
  - [ ] Redis 캐시 레이어 구축
- [ ] **쿼리 최적화**
  - [ ] N+1 문제 해결 (BoardRepository)
  - [ ] Cursor 기반 페이징 구현
  - [ ] 데이터베이스 인덱스 추가
- [ ] **Connection Pool**
  - [ ] HikariCP 튜닝

#### 6. 비동기 처리 개선
- [ ] AsyncUncaughtExceptionHandler 구현
- [ ] MDC 컨텍스트 전파
- [ ] CompletableFuture 체이닝
- [ ] 타임아웃 설정

#### 7. 입력 검증 체계화
- [ ] @Valid*, @Optional* 커스텀 애노테이션 완성
- [ ] ValidationGroup 정의
- [ ] 다국어 검증 메시지

#### 8. 로깅 체계 정리 🆕
##### 중복 로깅 제거
- [ ] UserCommandService (line 158, 184)
- [ ] SessionAuthCommandService (line 64)
- [ ] EmailEventListener (line 30, 32, 34, 47, 53, 55, 68, 70, 73)
- [ ] SmtpEmailService (line 87, 103, 115, 127)

##### 신규 AOP Aspect
- [ ] BusinessValidationAspect
- [ ] EventHandlerAspect
- [ ] EmailServiceAspect

##### 로깅 표준화
- [ ] 레벨별 용도 정의 (DEBUG/INFO/WARN/ERROR)
- [ ] MDC 활용 (요청 추적 ID)
- [ ] 구조화된 로깅 (JSON 포맷)

#### 9. 설정 관리 (프로퍼티 전략)
- [ ] BoardholeProperties 루트 구성 도입 (@ConfigurationProperties)
  - [ ] validation.emailVerification (expirationMs, signupExpirationMs, codeLength)
  - [ ] email (verificationExpirationMs, fromName, baseUrl)
  - [ ] security.verificationCode (charset, length, expiryMinutes)
  - [ ] cors, defaultUsers 등 기존 구조 유지하되 묶음으로 주입
- [ ] 서비스 주입 간소화: 각 서비스는 BoardholeProperties 하나만 주입 → 서브뷰로 사용
- [ ] 시간 단위 통일: 모두 ms 단위로 관리 (코드에서 Duration 변환)
- [ ] 폴백 제거: 미설정 시 즉시 실패 (main에 기본값 명시, test는 최소 오버라이드)
- [ ] YAML 중복 제거: 파생 값은 참조 사용 (예: email.verification-expiration-ms → ${boardhole.validation.email-verification.expiration-ms})
- [ ] 테스트 프로필 가이드 정착
  - [ ] test에서는 application-test.yml만 사용 (@ActiveProfiles("test")), test/resources/application.yml 금지
  - [ ] 정말 필요한 키만 오버라이드(만료 ms 등), 나머지는 main 기본값 사용
  - [ ] 케이스별 임시 오버라이드는 @DynamicPropertySource로 처리
- [ ] 민감 정보 암호화 (Jasypt)

---

### 🔵 P2 - Minor (1개월 내)
> 코드 품질, 개발 편의성, 확장성

#### 10. 코드 품질
- [ ] MapStruct BaseMapper 인터페이스
- [ ] 패키지 구조 일관성 (event 패키지)
- [ ] ArchUnit 순환 의존성 검사
- [ ] 메서드 복잡도 감소 (최대 20줄)

#### 11. API 개선
- [ ] API 버저닝 전략 결정
- [ ] RFC 7807 Problem Details 완전 적용
- [ ] Rate Limiting (Bucket4j)
- [ ] OpenAPI 3.0 스펙 완성
  
##### 알아보기 메모
- [ ] Problem Details 자동 적용 범위와 전역 핸들러/시큐리티 핸들러와의 상호작용 정리 (application.yml의 spring.mvc.problemdetails.enabled=true 영향 범위)

---

## 🧭 설정/프로퍼티 전략 (Direction)
- 루트 묶음 도입: `@ConfigurationProperties(prefix="boardhole")`로 BoardholeProperties 하나에 하위 설정을 중첩 타입으로 구성.
- 주입 간소화: 서비스는 BoardholeProperties만 주입하고, 필요한 서브뷰(예: validation().emailVerification())로 접근.
- 단위 통일: 시간 관련 값은 전부 ms로 관리하고, 코드에서 `Duration` 변환. YAML에 서로 다른 단위 혼재 금지.
- 폴백 금지: `@Value` 기본값 문법(`:`) 사용 금지. 미설정은 바로 실패로 감지.
- 중복 제거: 파생 값은 YAML 참조로 정의해 키 개수는 유지하고, 정의 중복을 제거.
- 테스트 정책:
  - test 프로필만 오버라이드(최소 키), 케이스별 변동은 `@DynamicPropertySource`로 처리.
  - test-data는 텍스트 픽스처만 유지(만료 값은 공용(ms) 키 사용).

### Next Steps
- [ ] BoardholeProperties 초안 추가 + `@EnableConfigurationProperties` 등록
- [ ] UserCommandService/EmailVerificationService를 BoardholeProperties 의존으로 전환 (@Value 제거)
- [ ] EntityTestBase 등 테스트 주입부 정리 (TestDataConfig → ConfigurationProperties로 이관)

#### 12. 개발 환경
- [ ] 테스트 Fixture/ObjectMother 패턴
- [ ] Docker .env 템플릿
- [ ] Makefile 자동화
- [ ] setup.sh 스크립트

---

## ✅ 완료된 작업

### 2025-09-03
- [x] Validation Message Migration
  - [x] 11개 커스텀 애노테이션 키 통일
  - [x] ValidationMessages.properties 정리
  - [x] 영문 번역 완성

### API 문서화
- [x] Spring REST Docs 제거
- [x] springdoc-openapi 중심 전환
- [x] Swagger UI 활성화 (/swagger-ui)

### 2025-09-05 (설정/프로퍼티 정리)
- [x] 만료 시간 단위 ms로 통일 (validation/email), 폴백 제거 → 실패 조기 감지
- [x] test-data 만료 관련 프로퍼티 제거, 공용(ms)만 사용
- [x] test/resources/application.yml 제거(가림 문제 방지), @ActiveProfiles("test") 유지
- [x] 기본 사용자 비밀번호 정책 준수 값으로 상향 (Admin123!, User123!)

---

## 🚀 신규 기능 백로그

### 게시판 기능
- [ ] 댓글 시스템 (대댓글, 좋아요)
- [ ] 파일 첨부 (S3 연동)
- [ ] Elasticsearch 검색
- [ ] 태그 시스템
- [ ] 북마크 기능

### 사용자 기능
- [ ] 프로필 관리
- [ ] 팔로우 시스템
- [ ] 실시간 알림 (SSE/WebSocket)
- [ ] 소셜 로그인 (OAuth2)

### 관리자 기능
- [ ] 어드민 대시보드
- [ ] 컨텐츠 관리
- [ ] 배치 작업 (@Scheduled)
- [ ] 감사 로그

---

## 📅 실행 로드맵

### Sprint 1 (Week 1) - 긴급 조치
| 일정 | 작업 | 담당자 | 상태 |
|------|------|--------|------|
| Day 1-2 | 이메일 재시도 메커니즘 | - | ⏳ |
| Day 3-4 | 핵심 테스트 작성 | - | ⏳ |
| Day 5 | 보안 패치 | - | ⏳ |
| Day 6-7 | 트랜잭션 정리 | - | ⏳ |

### Sprint 2 (Week 2-3) - 안정화
| 일정 | 작업 | 담당자 | 상태 |
|------|------|--------|------|
| Week 2 | 캐싱 전략 구현 | - | ⏳ |
| Week 2 | N+1 쿼리 최적화 | - | ⏳ |
| Week 3 | 로깅 체계 정리 | - | ⏳ |
| Week 3 | 입력 검증 완성 | - | ⏳ |

### Sprint 3 (Week 4) - 개선
| 일정 | 작업 | 담당자 | 상태 |
|------|------|--------|------|
| Week 4 | 코드 품질 개선 | - | ⏳ |
| Week 4 | API 고도화 | - | ⏳ |
| Week 4 | 신기능 1-2개 | - | ⏳ |

---

## 🏗️ 기술 부채

### 아키텍처
- [ ] Event Sourcing + CQRS
- [ ] Aggregate 경계 정의
- [ ] 헥사고날 아키텍처
- [ ] Multi-module 구조

### DevOps
- [ ] GitHub Actions + ArgoCD
- [ ] K6/Gatling 성능 테스트
- [ ] SonarQube 품질 관리
- [ ] Kubernetes 배포

### 운영 준비
- [ ] Blue-Green 배포
- [ ] Circuit Breaker
- [ ] 백업/복구 전략
- [ ] SLI/SLO/SLA 정의

---

## 📝 문서화 진행 상황

### 진행 중 🚧
- [ ] @Operation 애노테이션 보강
- [ ] ProblemDetails 문서화
- [ ] package-info.java 작성
- [ ] 도메인 모델 문서화

### 계획 중 📋
- [ ] OpenAPI YAML 생성
- [ ] ReDoc 정적 사이트
- [ ] GitHub Pages 배포
- [ ] API 버전 관리

---

## 🔗 참고 자료
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [프로젝트 Wiki](./docs/wiki)
- [API 문서](http://localhost:8080/swagger-ui)
- [아키텍처 다이어그램](./docs/architecture)

---

*마지막 업데이트: 2025-09-04*
*다음 리뷰: 2025-09-11*
