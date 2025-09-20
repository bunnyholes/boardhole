package dev.xiyo.bunnyholes.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.xiyo.bunnyholes.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * sec:authorize 태그와 세션 생성 관계를 명확히 증명하는 테스트
 */
@DisplayName("sec:authorize와 세션 생성 증명 테스트")
class SecAuthorizeSessionTest extends ViewE2ETestBase {

    private boolean hasJSessionId() {
        return context.cookies().stream()
                      .anyMatch(cookie -> "JSESSIONID".equals(cookie.name));
    }

    @Test
    @DisplayName("1. sec:authorize 없는 페이지 - 세션 생성 안 됨")
    void testPageWithoutSecAuthorize() {
        // 정적 리소스는 sec:authorize가 없고 세션이 생성되지 않음
        page.navigate("http://localhost:" + port + "/css/app.css");
        page.waitForLoadState();

        System.out.println("정적 리소스(sec:authorize 없음) 접근 후 JSESSIONID: " + hasJSessionId());

        // 정적 리소스는 세션이 생성되지 않아야 함
        assertThat(hasJSessionId())
                .withFailMessage("sec:authorize가 없는 정적 리소스에서는 세션이 생성되지 않아야 합니다")
                .isFalse();
    }

    @Test
    @DisplayName("2. sec:authorize 있는 페이지(홈) - 세션 생성 안됨 (eager 아님)")
    void testPageWithSecAuthorize() {
        page.navigate("http://localhost:" + port + "/");
        page.waitForLoadState();

        System.out.println("sec:authorize 있는 페이지(홈) 접근 후 JSESSIONID: " + hasJSessionId());

        // Spring Security 6.x에서는 sec:authorize만으로는 세션이 생성되지 않음
        // 실제 인증이 필요하거나 세션이 필요한 작업이 있을 때만 생성됨
        assertThat(hasJSessionId())
                .withFailMessage("Spring Security 6.x에서는 sec:authorize만으로 세션이 생성되지 않습니다")
                .isFalse();
    }

    @Test
    @DisplayName("3. 로그인 페이지 (sec:authorize 있음) - 세션 생성됨")
    void testLoginPageWithSecAuthorize() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();

        System.out.println("로그인 페이지(sec:authorize 있음) 접근 후 JSESSIONID: " + hasJSessionId());

        // 로그인 페이지도 base.html을 통해 header를 포함하므로 sec:authorize가 있음
        assertThat(hasJSessionId())
                .withFailMessage("sec:authorize가 있는 로그인 페이지에서는 세션이 생성됩니다")
                .isTrue();
    }
}