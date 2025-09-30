package dev.xiyo.bunnyholes.boardhole.shared.presentation.view;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TODO: RequestCache, 세션 유지, 홈 화면 시나리오 등을 MockMvc로 재구현한다.
 * <p>
 * Playwright 기반 보조 테스트가 다루던 세부 흐름을 추적하기 위해 비활성화된 테스트를 남겨둔다.
 */
@Tag("view")
class LegacyViewScenarioTest {

    @Test
    @Disabled("TODO: RequestCache를 통한 원래 URL 복귀를 MockMvc 테스트로 구현한다")
    @DisplayName("보호된 페이지 접근 후 로그인하면 원래 URL로 복귀한다")
    void requestCacheRestoresOriginalUrl() {
        // 이전 RequestCacheTest에서 검증한 내용:
        // 1. 보호된 URL 접근 시 RequestCache에 저장된다.
        // 2. 로그인 성공 후 저장된 URL로 리다이렉션된다.
        // 3. RequestCache가 비어 있을 때는 기본 이동 경로(/boards)가 사용된다.
    }

    @Test
    @Disabled("TODO: 세션 생성과 쿠키 갱신 흐름을 MockMvc 테스트로 구현한다")
    @DisplayName("세션 생성과 쿠키 상태를 추적한다")
    void sessionLifecycle() {
        // 이전 SessionCreationDebugTest와 SessionDebugTest에서 검증한 내용:
        // 1. 최초 로그인 시 JSESSIONID가 생성되고 이후 요청에도 재사용된다.
        // 2. 세션 만료 후 재요청하면 새 세션이 발급된다.
        // 3. Remember-me 사용 시 세션 유지 전략이 다르게 동작한다.
    }

    @Test
    @Disabled("TODO: 인증 상태에 따른 홈 화면 메시지를 MockMvc로 검증한다")
    @DisplayName("홈 화면 - 인증 여부에 따라 다른 메시지를 표시한다")
    void indexPageGreeting() {
        // 이전 IndexViewE2ETest에서 검증한 내용:
        // 1. 비로그인 사용자는 로그인/회원가입 CTA를 본다.
        // 2. 로그인 사용자는 최근 활동 링크와 사용자명을 본다.
        // 3. 오류 배너가 세션 플래시 속성에 있을 때만 노출된다.
    }

    @Test
    @Disabled("TODO: 권한에 따라 접근이 제한되는 뷰를 MockMvc로 검증한다")
    @DisplayName("권한 검사 - 허용된 역할만 접근할 수 있다")
    void authorizationGuards() {
        // 이전 SecAuthorizeSessionTest에서 검증한 내용:
        // 1. 관리자 전용 화면은 ROLE_ADMIN만 접근 가능하다.
        // 2. 일반 사용자는 403 페이지로 이동한다.
        // 3. 권한 부족 시 커스텀 에러 페이지가 노출된다.
    }
}
