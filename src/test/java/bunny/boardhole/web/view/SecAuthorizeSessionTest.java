package bunny.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.ViewE2ETestBase;

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
        page.navigate("http://localhost:" + port + "/test-no-sec");
        page.waitForLoadState();
        
        System.out.println("sec:authorize 없는 페이지 접근 후 JSESSIONID: " + hasJSessionId());
        
        // sec:authorize가 없으면 세션이 생성되지 않아야 함
        assertThat(hasJSessionId())
                .withFailMessage("sec:authorize가 없는 페이지에서는 세션이 생성되지 않아야 합니다")
                .isFalse();
    }

    @Test
    @DisplayName("2. sec:authorize 있는 페이지(홈) - 세션 생성됨")
    void testPageWithSecAuthorize() {
        page.navigate("http://localhost:" + port + "/");
        page.waitForLoadState();
        
        System.out.println("sec:authorize 있는 페이지(홈) 접근 후 JSESSIONID: " + hasJSessionId());
        
        // sec:authorize가 있으면 세션이 생성됨
        assertThat(hasJSessionId())
                .withFailMessage("sec:authorize가 있는 페이지에서는 세션이 생성됩니다")
                .isTrue();
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