# Spring Boot 게시판 프로젝트 TODO

## 📊 프로젝트 현황
- **코드 규모**: 157개 Java 파일 (소스), 24개 테스트 파일
- **핵심 컴포넌트**: 19개 (Controller 3, Service 11, Repository 3, Config 2)
- **코드 라인수**: 약 10,400줄
- **테스트 커버리지**: 약 15% (목표: 80%)
- **기술 스택**: Java 21, Spring Boot 3.5.5, MySQL, Redis, Docker

## 🔴 Critical Issues (즉시 수정 필요)

### 1. 이메일 발송 실패 처리 ⚠️ 데이터 손실 위험
- [ ] 이메일 재시도 메커니즘 구현 (EmailEventListener line 35, 56, 74)
- [ ] 실패 큐 구현 (DLQ - Dead Letter Queue)
- [ ] 재시도 정책 설정 (최대 3회, exponential backoff)
- [ ] Spring Retry 또는 Resilience4j 도입 검토
- [ ] 실패 이메일 관리자 알림 시스템

### 2. 극심한 테스트 커버리지 부족 🚨
- [ ] 현재 커버리지 15% → 1차 목표 50% → 최종 80%
- [ ] BoardCommandService 단위 테스트 작성
- [ ] UserCommandService 통합 테스트 보강  
- [ ] EmailEventListener 비동기 처리 테스트
- [ ] AppPermissionEvaluator 권한 검증 테스트
- [ ] 테스트 픽스처 및 팩토리 패턴 도입
- [ ] Testcontainers 활용한 통합 테스트 강화

### 3. 보안 취약점 수정
- [ ] 이메일 미인증 사용자 접근 제한 강화
- [ ] EmailVerificationFilter 보안 정책 명확화
- [ ] 역할 기반 접근 제어 (RBAC) 완성
- [ ] CSRF 토큰 검증 강화
- [ ] Rate Limiting 구현 (brute force 방어)
- [ ] 민감 정보 로깅 방지 (password, token 등)

### 4. 트랜잭션 무결성 보장
- [ ] 이벤트 처리 트랜잭션 경계 명확화
- [ ] @TransactionalEventListener 실패 시 롤백 정책
- [ ] 분산 트랜잭션 보상 패턴 (Saga Pattern) 적용
- [ ] 데이터베이스 락 전략 최적화

## 🟡 Major Issues (중요)

### 5. 성능 최적화 🚀
- [ ] **캐싱 전략 부재**: AppPermissionEvaluator 캐싱 구현 (line 74 TODO)
- [ ] **Spring Cache 도입**: @Cacheable, @CacheEvict 적용
- [ ] **Redis 캐시 레이어**: 자주 조회되는 데이터 캐싱
- [ ] **N+1 쿼리 문제**: BoardRepository 모든 메서드에 @EntityGraph 적용
- [ ] **대량 데이터 페이징**: Cursor 기반 페이징 구현
- [ ] **데이터베이스 인덱스**: 검색 성능 향상을 위한 인덱스 추가
- [ ] **HikariCP 튜닝**: Connection Pool 최적화 (현재 TODO 주석만 존재)

### 6. 비동기 처리 개선
- [ ] AsyncUncaughtExceptionHandler 구현
- [ ] 비동기 작업 모니터링 (MDC 컨텍스트 전파)
- [ ] ThreadPoolTaskExecutor 동적 설정
- [ ] CompletableFuture 체이닝 패턴 도입
- [ ] 비동기 작업 타임아웃 설정

### 7. 입력 검증 체계화
- [ ] @Valid*, @Optional* 커스텀 애노테이션 실제 구현
- [ ] Bean Validation 2.0 활용 강화
- [ ] 도메인별 ValidationGroup 정의
- [ ] 검증 오류 메시지 다국어 지원 강화
- [ ] Custom Validator 구현 (복잡한 비즈니스 규칙)

### 8. 로깅 및 모니터링 📊
- [ ] **구조화된 로깅**: JSON 포맷 로깅 (prod 환경)
- [ ] **MDC 활용**: 요청 추적 ID, 사용자 컨텍스트
- [ ] **Spring Boot Actuator**: health, metrics, info 엔드포인트
- [ ] **Micrometer 메트릭**: 커스텀 메트릭 정의
- [ ] **ELK 스택 연동**: 로그 중앙화 및 분석
- [ ] **Grafana 대시보드**: 실시간 모니터링
- [ ] **알림 시스템**: 임계치 기반 알림

### 9. 설정 관리 개선
- [ ] **@ConfigurationProperties 마이그레이션**: @Value 대체 (36개 발견)
- [ ] **환경별 설정 분리**: dev/staging/prod 명확화
- [ ] **외부 설정 관리**: Spring Cloud Config 또는 Vault
- [ ] **민감 정보 암호화**: Jasypt 도입

## 🔵 Minor Issues (개선 권장)

### 10. 코드 품질 개선
- [ ] **MapStruct 중복 제거**: BaseMapper 인터페이스 도입
- [ ] **Lombok 최적화**: @SuperBuilder 활용
- [ ] **패키지 구조 정리**: event 패키지 일관성
- [ ] **순환 의존성 검사**: ArchUnit 규칙 추가
- [ ] **코드 복잡도 감소**: 메서드당 최대 20줄
- [ ] **정적 분석 강화**: ErrorProne, NullAway 설정

### 11. API 개선
- [ ] **API 버저닝**: URL path vs Header 전략 결정
- [ ] **RFC 7807 완전 적용**: 모든 에러 응답 표준화
- [ ] **Rate Limiting**: Bucket4j 또는 Resilience4j
- [ ] **API 문서 자동화**: OpenAPI 3.0 스펙 완성
- [ ] **HATEOAS**: RESTful 성숙도 레벨 3 달성
- [ ] **GraphQL 검토**: 복잡한 조회 요구사항 대응

### 12. 개발/운영 환경
- [ ] **테스트 데이터**: Fixture, ObjectMother 패턴
- [ ] **Docker 환경**: .env 파일 템플릿 제공
- [ ] **Make 파일**: 빌드/배포 자동화
- [ ] **개발 환경 스크립트**: setup.sh 작성
- [ ] **로컬 HTTPS**: mkcert 활용

## 📚 문서화 (완료 및 진행중)

### ✅ 완료된 작업

#### Validation Message Migration (2025-09-03)
- [x] 11개 커스텀 검증 애노테이션 메시지 키 형식 통일
- [x] ValidationMessages.properties 일관된 키 형식 (validation.*)
- [x] ValidationMessages_en.properties 영문 번역 완성
- [x] messages.properties 정리 (비즈니스/예외 메시지만 유지)

#### API 문서화 정리
- [x] Spring REST Docs 제거, springdoc-openapi 중심 운영
- [x] Swagger UI 활성화 (/swagger-ui, /v3/api-docs)
- [x] springdoc 경로 스캔 제한 (/api/**)

### 🚧 진행 중인 문서화 작업

#### API 문서 개선 (단기)
- [ ] 컨트롤러별 @Operation(summary, tags) 보강
- [ ] ProblemDetails 공통 오류 모델 문서화

#### OpenAPI 스펙 관리 (중기)
- [ ] /v3/api-docs 기반 docs/openapi.yaml 생성
- [ ] ReDoc 정적 HTML 렌더링
- [ ] CI 파이프라인 통합
  - [ ] Spectral: 스타일/설명 lint
  - [ ] openapi-diff: 브레이킹 변경 감지
  - [ ] schemathesis: 스펙-서버 검증

#### Javadoc/코드 문서화
- [ ] Gradle javadoc 태스크 설정
- [ ] 패키지별 package-info.java 작성
- [ ] 도메인 모델 및 비즈니스 로직 문서화
- [ ] GitHub Pages 배포 설정

## 🚀 미구현 기능 (신규 개발)

### 게시판 고급 기능
- [ ] **댓글 시스템**: 대댓글, 좋아요, 신고 기능
- [ ] **파일 첨부**: 이미지/문서 업로드 (S3 연동)
- [ ] **검색 고도화**: Elasticsearch 전문 검색
- [ ] **태그 시스템**: 해시태그 기반 분류
- [ ] **북마크/즐겨찾기**: 게시글 저장 기능
- [ ] **조회수 중복 방지**: IP/세션 기반 체크
- [ ] **게시글 히스토리**: 수정 이력 관리

### 사용자 기능 확장
- [ ] **프로필 관리**: 아바타, 자기소개
- [ ] **팔로우 시스템**: 사용자 간 팔로우
- [ ] **알림 시스템**: 실시간 알림 (SSE/WebSocket)
- [ ] **활동 로그**: 사용자 활동 내역
- [ ] **포인트/레벨**: 게이미피케이션
- [ ] **소셜 로그인**: OAuth2 (Google, Kakao)

### 관리자 기능
- [ ] **어드민 대시보드**: 통계, 사용자 관리
- [ ] **컨텐츠 관리**: 게시글 일괄 처리
- [ ] **신고 관리**: 신고 접수 및 처리
- [ ] **공지사항**: 시스템 공지 관리
- [ ] **배치 작업**: 정기 데이터 정리 (@Scheduled)
- [ ] **감사 로그**: 모든 관리 작업 기록

### 시스템 고도화
- [ ] **실시간 기능**: WebSocket 채팅
- [ ] **이벤트 스토어**: Event Sourcing 도입
- [ ] **검색 엔진**: Elasticsearch 통합
- [ ] **캐시 워밍**: 자주 사용되는 데이터 사전 로드
- [ ] **CDN 연동**: 정적 자원 최적화
- [ ] **국제화(i18n)**: 다국어 지원 완성

## 🗓 개선 로드맵

### 🏃 Phase 1 (1주차) - Critical 해결
1. **Day 1-2**: 이메일 재시도 메커니즘 (Spring Retry)
2. **Day 3-4**: 핵심 테스트 작성 (Service Layer)
3. **Day 5**: 보안 취약점 패치
4. **Day 6-7**: 트랜잭션 정책 수립 및 적용

### 🔨 Phase 2 (2-3주차) - Major 해결
1. **Week 2**: 
   - Redis 캐싱 레이어 구축
   - N+1 쿼리 최적화
   - 비동기 에러 핸들링
2. **Week 3**:
   - 입력 검증 체계 완성
   - 로깅/모니터링 인프라
   - 설정 관리 개선

### 🎯 Phase 3 (4주차) - Minor 및 신기능
1. **Week 4**:
   - 코드 품질 개선
   - API 고도화
   - 개발 환경 자동화
   - 우선순위 높은 신기능 1-2개 구현

### 📈 Phase 4 (5-8주차) - 장기 개선
1. **Month 2**: 
   - 게시판 고급 기능 구현
   - 사용자 경험 개선
   - 성능 최적화 고도화
2. **Month 3**:
   - 관리자 기능 구축
   - 시스템 고도화
   - 마이크로서비스 전환 POC

## 🔧 기술 부채 해결 전략

### 아키텍처 개선
- [ ] **이벤트 기반 아키텍처**: Event Sourcing + CQRS
- [ ] **도메인 주도 설계**: Aggregate 경계 명확화
- [ ] **헥사고날 아키텍처**: 포트/어댑터 패턴
- [ ] **모듈화**: Multi-module 프로젝트 구조

### 인프라/DevOps
- [ ] **CI/CD 고도화**: GitHub Actions + ArgoCD
- [ ] **성능 테스트**: K6/Gatling 자동화
- [ ] **보안 스캔**: OWASP ZAP, SonarQube
- [ ] **인프라 코드화**: Terraform/Ansible
- [ ] **컨테이너화**: Kubernetes 배포

### 운영 준비
- [ ] **무중단 배포**: Blue-Green/Canary
- [ ] **장애 대응**: Circuit Breaker, Fallback
- [ ] **백업 전략**: 자동 백업, 복구 테스트
- [ ] **SRE**: SLI/SLO/SLA 정의
- [ ] **운영 문서**: Runbook, Playbook 작성
