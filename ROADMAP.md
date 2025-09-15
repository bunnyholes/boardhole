**목표**
- 복잡도 증가를 억제하면서 보안·운영 기초를 다지고, 문서 정합성 유지.
- 현 단계에서는 기능 추가보다 “정리와 안정화”에 집중.

**현 상태 고정(의도적 결정)**
- 테스트 실행: 단일 태스크 `./gradlew test`만 사용. 단위/E2E 분리 태스크 제공하지 않음.
- 권한 평가 캐시: 도입 보류. 추후 JWT 전환 시 재평가.
- 정렬 필드 화이트리스트: 도입 보류(현재 예외 흐름 유지).
- 컨트롤러 다중 Content-Type 처리(JSON·폼/멀티파트): 현 단계에서는 유지(변경 보류).

**보안/운영/마이그레이션**
- 현 단계에서는 추가 계획 없이 유지합니다.

**JSON vs 멀티파트/폼 처리(설명)**
- 한 메서드가 동시에 JSON(`@RequestBody`)과 폼/멀티파트(`@ModelAttribute`/MultipartResolver)를 자연스럽게 처리하는 것은 권장되지 않음.
- 권장 패턴: 같은 경로에 두 메서드로 분리하고 `consumes`로 매핑 분기
  - 예: `@PostMapping(path="/api/boards", consumes=application/json)`
  - 예: `@PostMapping(path="/api/boards", consumes=application/x-www-form-urlencoded, multipart/form-data)`
- 현 단계에서는 변경하지 않고 현행 유지.

**타임라인 제안**
- 문서 정합성 유지와 메시지 키 보완 등 경량 작업 위주로 지속.

**비고**
- 현 로드맵은 ‘도입 계획’ 중심이며, 구현은 안정성·복잡도 영향 평가 후 순차 진행.
