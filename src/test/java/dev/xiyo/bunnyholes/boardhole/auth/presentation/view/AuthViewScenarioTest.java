package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TODO: 로그인/로그아웃/회원가입 플로우를 MockMvc 통합 테스트로 재현한다.
 * <p>
 * Playwright 기반 테스트가 제공하던 시나리오를 유지하기 위한 비활성화된 테스트 모음이다.
 */
@Tag("view")
class AuthViewScenarioTest {

    @Test
    @Disabled("TODO: 로그인 성공/실패 시나리오를 MockMvc 통합 테스트로 복원한다")
    @DisplayName("로그인 - 성공과 실패 흐름")
    void loginFlow() {
        // 이전 LoginViewE2ETest에서 검증한 내용:
        // 1. 올바른 자격 증명으로 로그인하면 /boards 로 리다이렉션된다.
        // 2. 잘못된 비밀번호 입력 시 오류 메시지가 표시되며 입력한 이메일은 유지된다.
        // 3. 잠금된 계정은 로그인할 수 없으며 경고 메시지를 출력한다.
    }

    @Test
    @Disabled("TODO: 로그아웃 플로우를 MockMvc 통합 테스트로 복원한다")
    @DisplayName("로그아웃 - 세션 만료와 피드백")
    void logoutFlow() {
        // 이전 LogoutViewE2ETest에서 검증한 내용:
        // 1. 로그아웃 후 세션이 무효화되고 쿠키가 제거된다.
        // 2. 홈 화면으로 리다이렉션되며 로그아웃 성공 배너가 노출된다.
        // 3. 로그아웃 후 보호된 페이지 접근 시 다시 로그인 페이지로 이동한다.
    }

    @Test
    @Disabled("TODO: 회원가입 흐름을 MockMvc 통합 테스트로 복원한다")
    @DisplayName("회원가입 - 폼 검증과 자동 로그인")
    void signupFlow() {
        // 이전 SignupViewE2ETest에서 검증한 내용:
        // 1. 폼 필수 값 누락 시 각 필드에 검증 메시지가 표시된다.
        // 2. 이미 사용 중인 이메일로 가입 시 중복 오류가 노출된다.
        // 3. 정상 가입 후 자동 로그인되어 대시보드로 이동한다.
    }
}
