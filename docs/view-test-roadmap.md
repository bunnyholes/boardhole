# 뷰 테스트 전환 로드맵

Playwright 기반 뷰 E2E 테스트를 MockMvc 통합 테스트로 전환하는 과정에서 아직 구현되지 않은 시나리오를 정리한다. 현재는 `@Disabled` 상태의 자리표시자 테스트를 통해 잔여 범위를 추적하고 있다.

## Auth 도메인
- [`AuthViewScenarioTest`](../src/test/java/dev/xiyo/bunnyholes/boardhole/auth/presentation/view/AuthViewScenarioTest.java)
  - 로그인 성공/실패 플로우 검증
  - 로그아웃 시 세션 무효화 및 피드백 배너 검증
  - 회원가입 폼 검증 및 자동 로그인 흐름

## Board 도메인
- [`BoardViewScenarioTest`](../src/test/java/dev/xiyo/bunnyholes/boardhole/board/presentation/view/BoardViewScenarioTest.java)
  - 게시판 목록 접근 제어와 RequestCache 복귀
  - 게시글 상세 화면 권한 및 UI 요소
  - 게시글 작성/수정 폼 검증과 리다이렉션

## Shared 시나리오
- [`LegacyViewScenarioTest`](../src/test/java/dev/xiyo/bunnyholes/boardhole/shared/presentation/view/LegacyViewScenarioTest.java)
  - RequestCache 원래 URL 복귀 흐름
  - 세션 생성·만료 라이프사이클 및 Remember-me 전략
  - 홈 화면 인증 상태별 메시지 노출
  - 권한 부족 시 커스텀 에러 페이지 처리

## 향후 작업 가이드
1. 각 자리표시자 테스트를 `@SpringBootTest` + `@AutoConfigureMockMvc` 기반 통합 테스트로 교체한다.
2. 기존 Playwright 테스트 명세(시나리오, 예상 UI 요소, 리다이렉션 경로)를 참고하여 검증 로직을 세분화한다.
3. 필요 시 테스트 전용 픽스처(사용자, 게시글)를 `testsupport` 모듈에 추가하여 중복 생성을 방지한다.
4. 전환이 완료된 시나리오는 `@Disabled`를 제거하고 문서에서 체크리스트 항목을 완료 처리한다.
