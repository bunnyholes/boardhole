# 🚀 Spring Boot 게시판 프로젝트 로드맵

## 📊 최신 현황 (요약)

- 테스트 체계: 도메인 중심 + Unit/Integration/E2E 태깅/태스크 정비 완료
- E2E 시나리오: 인증/권한/사용자/게시판 CRUD·검색/관리자 워크플로우 구축
- 이메일 빈 정책: smtp 프로파일에서만 `SmtpEmailService`(@Primary), 기본은 `DummyEmailService`(항상 등록)
- 커버리지: Jacoco 집계 리포트(`jacocoAllTestReport`) + Sonar 연동 완료
- ArchUnit: 테스트 클래스 제외 설정 적용 (DO_NOT_INCLUDE_TESTS)
- 잔여 이슈: 일부 이메일 관련 E2E 세팅(환경별 프로파일/모킹) 점검 필요

---

## 🗓️ 실행 로드맵

### 🚨 Phase 0: IntelliJ 인스펙션 해결 (최우선 - Week 1)

**목표**: 모든 IntelliJ IDEA 인스펙션 이슈 제거(Null/Unused/리소스 번들/스타일)

#### Sprint 0.1 (Day 1): Critical - Null 안전성 (56개)

- [ ] DataFlowIssue: EmailOutbox null 경로 방어, 서비스 계층 null-safe 보강
- [ ] NullableProblems: @Nullable 명시 + Optional 반환/소비 정리

#### Sprint 0.2 (Day 2): Major - 미사용 코드 제거 (281개)

- [ ] 리소스 번들 미사용 키 정리(messages*, ValidationMessages)
- [ ] ApiPaths/Repository/Query 계층 미사용 상수·메서드 제거
- [ ] 미사용 파라미터/변수 제거

#### Sprint 0.3 (Day 3): 리소스 번들 & Spring 설정 (157개)

- [ ] 번들 키 동기화/누락 번역 보완/중복 제거
- [ ] application*.yml 키 유효성/타입 점검 및 정합화

#### Sprint 0.4 (Day 4-5): 코드 품질 & 스타일 (300개+)

- [ ] import 정리, star import 해소
- [ ] 접근 제한자/불변성(final)/빈 메서드 제거
- [ ] Java 21 API·method reference 적용
- [ ] 맞춤법/애노테이션 간소화/동일 파라미터 개선

#### Sprint 0.5 (Day 6-7): 검증 & 마무리

- [ ] **모든 인스펙션 재실행**
    - [ ] IntelliJ IDEA Analyze → Inspect Code
    - [ ] 0 issues 확인
- [ ] **빌드 검증**
    - [ ] ./gradlew clean build (unit+integration+e2e)
- [ ] **문서화**
    - [ ] 수정 사항 CHANGELOG 작성
    - [ ] 코드 리뷰 체크리스트 업데이트

---

### 📌 Phase 1: 긴급 수정 (Week 2)

**목표**: 크리티컬 이슈 해결 및 기본 품질 확보

#### Sprint 1.1 (Day 1-2): 테스트 안정화

- [ ] **실패 테스트 수정** (5개)
    - [ ] SmtpEmailServiceRetryTest 재시도 로직
    - [ ] SmtpEmailServiceWithOutboxTest Outbox 패턴 3개
    - [ ] EmailVerificationServiceTest 이메일 검증
- [ ] **JaCoCo 설정 수정**
    - [ ] 커버리지 리포트 생성 문제 해결
    - [ ] 실제 커버리지 측정 활성화

#### Sprint 1.2 (Day 3-4): 이메일 시스템 안정화

- [ ] **Spring Retry 구현**
    - [ ] @Retryable 설정 완성
    - [ ] @Recover 메서드 구현
    - [ ] Exponential backoff 설정
- [ ] **Outbox 패턴 완성**
    - [ ] 트랜잭션 경계 정리
    - [ ] 실패 시 저장 로직 검증

#### Sprint 1.3 (Day 5-7): Checkstyle & PMD 해결

- [ ] **Checkstyle 위반 수정** (43개)
    - [ ] 120자 라인 길이 초과 수정 (32개)
    - [ ] JavaDoc 주석 추가
- [ ] **PMD 위반 수정** (1,153개)
    - [ ] 복잡도 감소
    - [ ] 명명 규칙 준수
    - [ ] 중복 코드 제거

---

### 📌 Phase 2: 품질 개선 (Week 3-4)

**목표**: 코드 품질 향상 및 테스트 커버리지 확보

#### Sprint 2.1: 테스트 커버리지 향상

- [ ] **단위 테스트 추가** (목표: 50%)
    - [ ] BoardCommandService 완전 커버리지
    - [ ] UserCommandService 핵심 로직
    - [ ] 도메인 엔티티 비즈니스 메서드
- [ ] **통합 테스트 강화**
    - [ ] 트랜잭션 롤백 시나리오
    - [ ] 동시성 테스트 추가

#### Sprint 2.2: 로깅 체계 정리

- [ ] **중복 로깅 제거**
    - [ ] UserCommandService (line 158, 184)
    - [ ] SessionAuthCommandService (line 64)
    - [ ] EmailEventListener (30, 32, 34, 47, 53, 55, 68, 70, 73)
    - [ ] SmtpEmailService (87, 103, 115, 127)
- [ ] **AOP 기반 로깅**
    - [ ] BusinessValidationAspect 구현
    - [ ] EventHandlerAspect 구현
    - [ ] MDC 활용 (요청 추적 ID)

#### Sprint 2.3: 설정 관리 통합

- [ ] **BoardholeProperties 도입**
  ```java
  @ConfigurationProperties(prefix="boardhole")
  public class BoardholeProperties {
      private Validation validation;
      private Email email;
      private Security security;
      // 중첩 구조로 관리
  }
  ```
- [ ] **시간 단위 통일**
    - [ ] 모든 시간 값 ms 단위로 통일
    - [ ] Duration 변환 로직 구현

---

### 📌 Phase 3: 성능 최적화 (Week 5)

**목표**: 성능 개선 및 확장성 확보

#### Sprint 3.1: 캐싱 전략

- [ ] **Spring Cache 도입**
    - [ ] AppPermissionEvaluator 캐싱
    - [ ] 자주 조회되는 사용자 정보
    - [ ] Redis 캐시 레이어 구축

#### Sprint 3.2: 쿼리 최적화

- [ ] **N+1 문제 해결**
    - [ ] BoardRepository fetch join
    - [ ] @EntityGraph 활용
    - [ ] 배치 사이즈 최적화

#### Sprint 3.3: 비동기 처리

- [ ] **AsyncConfig 고도화**
    - [ ] ThreadPoolTaskExecutor 튜닝
    - [ ] AsyncUncaughtExceptionHandler
    - [ ] CompletableFuture 활용

---

### 📌 Phase 4: 보안 강화 (Week 6)

**목표**: 보안 취약점 제거 및 강화

#### Sprint 4.1: 인증/인가

- [ ] **Rate Limiting**
    - [ ] Bucket4j 도입
    - [ ] IP 기반 제한
- [ ] **이메일 인증 강화**
    - [ ] 미인증 사용자 접근 제한
    - [ ] 인증 코드 만료 처리

#### Sprint 4.2: 보안 설정

- [ ] **로깅 보안**
    - [ ] 민감 정보 마스킹
    - [ ] 감사 로그 구현

---

## 📈 성공 지표 (KPI)

### IntelliJ 인스펙션 지표 (최우선)

| 카테고리      | 현재       | Day 1   | Day 2   | Day 3   | Day 4 | Day 5 | Day 7 |
|-----------|----------|---------|---------|---------|-------|-------|-------|
| Null 안전성  | 56       | 0       | 0       | 0       | 0     | 0     | 0     |
| 미사용 코드    | 281      | 281     | 0       | 0       | 0     | 0     | 0     |
| 리소스 번들    | 127      | 127     | 127     | 0       | 0     | 0     | 0     |
| Spring 설정 | 30       | 30      | 30      | 0       | 0     | 0     | 0     |
| 코드 스타일    | 300+     | 300+    | 300+    | 300+    | 0     | 0     | 0     |
| **총 이슈**  | **900+** | **750** | **450** | **300** | **0** | **0** | **0** |

### 품질 지표

| 지표            | Week 1 | Week 2 | Week 3 | Week 4 | Week 5 |
|---------------|--------|--------|--------|--------|--------|
| IntelliJ 인스펙션 | 0      | 0      | 0      | 0      | 0      |
| 테스트 커버리지      | 20%    | 40%    | 60%    | 70%    | 80%    |
| Checkstyle 위반 | 0      | 0      | 0      | 0      | 0      |
| PMD 위반        | 500    | 200    | 50     | 10     | 0      |
| 실패 테스트        | 0      | 0      | 0      | 0      | 0      |

---

## ✅ 완료된 작업

### 2025-09-06

- [x] IntelliJ 인스펙션 분석 완료
- [x] 900+ 이슈 카테고리화 및 우선순위 설정

### 2025-09-05

- [x] 만료 시간 단위 ms로 통일
- [x] test/resources/application.yml 제거
- [x] 기본 사용자 비밀번호 정책 준수

### 2025-09-03

- [x] Validation Message Migration
- [x] springdoc-openapi 전환
- [x] Swagger UI 활성화

---

## 🎯 다음 단계 우선순위

### 즉시 시작 (Today - 최우선)

1. **EmailOutbox.java null 안전성 수정**
2. **DataFlowIssue 43개 해결**
3. **NullableProblems 13개 해결**

### 내일 (Tomorrow)

1. **messages.properties 205개 미사용 키 제거**
2. **unused 코드 76개 제거**

### 이번 주 완료 (This Week)

1. **IntelliJ 인스펙션 0 issues 달성**
2. **모든 테스트 통과**
3. **Checkstyle & PMD 위반 해결**

### 이번 달 목표 (This Month)

1. 테스트 커버리지 80% 달성
2. 모든 코드 품질 이슈 해결
3. 성능 최적화 완료
4. 보안 강화 완료

---

## 📚 기술 스택

- **Backend**: Java 21, Spring Boot 3.5.5
- **Database**: MySQL 8.0, Redis
- **Testing**: JUnit 5, Testcontainers, ArchUnit
- **Quality**: IntelliJ IDEA, Checkstyle, PMD, SpotBugs, JaCoCo
- **Documentation**: SpringDoc OpenAPI, Swagger UI
- **Infrastructure**: Docker, Docker Compose

---

## 🔗 참고 문서

- [API Documentation](http://localhost:8080/swagger-ui)
- [IntelliJ Inspections](https://www.jetbrains.com/help/idea/code-inspection.html)
- [Spring Boot Reference](https://spring.io/projects/spring-boot)

---

*마지막 업데이트: 2025-09-06*  
*다음 리뷰: 2025-09-08 (Phase 0 완료 후)*  
*담당자: TBD*
