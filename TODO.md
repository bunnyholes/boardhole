문서화/테스팅 정리 (2025-09-03)

결론
- Spring REST Docs 및 restdocs-api-spec 제거. 문서 자동화는 springdoc-openapi(Runtime) 중심으로 운영.
- 필요 시, 중기적으로 스펙-우선(OpenAPI YAML)로 전환하여 CI 검증을 추가.

현재 상태
- Swagger UI 활성: /swagger-ui (기본 Runtime: /v3/api-docs)
- springdoc 경로 스캔 제한: /api/**
- REST Docs 관련 코드/플러그인/샘플 테스트 제거 완료.

단기 TODO (빠른 가치)
- [ ] 컨트롤러별 최소 `@Operation(summary, tags)` 보강(필요 시에만)
- [ ] 공통 오류 모델(ProblemDetails) 설명을 Swagger 문서 상단 또는 각 응답에 링크
- [ ] Dev/Stage에서만 Swagger UI 공개, Prod는 정책 결정(공개/제한)

중기 TODO (스펙-우선 전환 시)
- [ ] `/v3/api-docs`를 기반으로 `docs/openapi.yaml` 시드 생성 및 커밋
- [ ] ReDoc로 `openapi.yaml` 렌더링(정적 HTML) → `/docs` 서빙(선택)
- [ ] CI 파이프라인에 추가
  - [ ] Spectral: 스타일/설명 lint
  - [ ] openapi-diff: 브레이킹 변경 감지(버전 정책 연동)
  - [ ] schemathesis: 스펙↔서버 응답 스키마 검증(Dev/Stage)

가이드라인(기여자 경험 최소화)
- 문서=코드 자동화가 우선일 때: springdoc 기본값 유지, 주석은 꼭 필요할 때만 추가
- 스펙-우선 선택 시: YAML만 수정해도 문서/검증이 동작하도록 CI로 보호
- REST Docs는 핵심 흐름(결제/인증) 증거 필요 시에만 제한적으로 도입

보류/제외
- REST Docs(테스트 기반 문서화) 및 OpenAPI 내보내기(restdocs-api-spec) 제거
- Asciidoctor 기반 정적 문서 빌드 제거

Javadoc/Dokka 정리 (추가 TODO)
- 목표: 코드 레벨 API 문서를 자동 생성하고, GitHub에서 읽기 좋은 포맷도 제공

- Javadoc (표준, HTML)
  - [ ] Gradle 태스크 사용: `./gradlew javadoc`
  - [ ] 출력 확인: `build/docs/javadoc/index.html`
  - [ ] 옵션(선택):
    - `tasks.named("javadoc") { options.encoding = "UTF-8"; options.links("https://docs.oracle.com/en/java/javase/21/docs/api/"); options.addBooleanOption("Xdoclint:none", true) }`
  - [ ] 패키지 문서: 각 패키지에 `package-info.java` 추가(개요/가이드/예제)
  - [ ] GitHub Pages 공개(선택): `docs/`로 복사 커밋 또는 `gh-pages` 브랜치 배포

- Dokka (GitHub‑Flavored Markdown / HTML)
  - [ ] Gradle 플러그인 추가: `id("org.jetbrains.dokka") version "1.9.20"`
  - [ ] GFM(Markdown) 생성: `./gradlew dokkaGfm` → `build/dokka/gfm/**.md`
  - [ ] HTML 생성(선택): `./gradlew dokkaHtml` → `build/dokka/html/index.html`
  - [ ] GitHub에서 읽기: `build/dokka/gfm` 산출물을 `docs/`에 복사/커밋
  - [ ] 루트 `docs/README.md`에 패키지별 문서 링크 목차화

- 패키지별 문서화 가이드
  - [ ] package-info.java 템플릿(섹션 고정): 개요 → 핵심 타입/계약 → 사용 예제 → 주의/제한 → 변경 이력
  - [ ] 공통 용어/오류/규칙은 최상위 패키지 문서에 정리하고 하위 패키지에서 링크

- CI/배포(선택)
  - [ ] GitHub Actions에서 `javadoc`/`dokkaGfm` 실행 → `docs/` 커밋 또는 `gh-pages` 배포
  - [ ] 릴리스 버전 디렉터리 운영(예: `docs/1.2.0/…`) 및 최신 링크 유지
