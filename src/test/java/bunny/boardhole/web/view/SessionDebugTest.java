package bunny.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.options.Cookie;

import bunny.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 세션 생성 시점 디버깅 테스트
 */
@DisplayName("세션 생성 시점 확인 테스트")
class SessionDebugTest extends ViewE2ETestBase {

    private boolean hasJSessionId() {
        return context.cookies().stream()
                .anyMatch(cookie -> "JSESSIONID".equals(cookie.name));
    }

    @Test
    @DisplayName("1. 홈페이지 방문 시 세션 생성 여부")
    void testSessionOnHomePageVisit() {
        page.navigate("http://localhost:" + port + "/");
        page.waitForLoadState();
        
        System.out.println("홈페이지 방문 후 JSESSIONID: " + hasJSessionId());
        assertThat(hasJSessionId()).isFalse();
    }

    @Test
    @DisplayName("2. 로그인 페이지 방문 시 세션 생성 여부")
    void testSessionOnLoginPageVisit() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();
        
        System.out.println("로그인 페이지 방문 후 JSESSIONID: " + hasJSessionId());
        assertThat(hasJSessionId()).isFalse();
    }

    @Test
    @DisplayName("3. 로그인 실패 시 세션 생성 여부")
    void testSessionOnLoginFailure() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();
        
        System.out.println("로그인 페이지 방문 후 JSESSIONID: " + hasJSessionId());
        
        // 잘못된 비밀번호로 로그인 시도
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "WrongPassword!");
        page.click("input[type='submit']");
        page.waitForLoadState();
        
        System.out.println("로그인 실패 후 JSESSIONID: " + hasJSessionId());
        
        // 세션이 생성되었는지 확인
        if (hasJSessionId()) {
            Cookie sessionCookie = context.cookies().stream()
                    .filter(c -> "JSESSIONID".equals(c.name))
                    .findFirst()
                    .orElse(null);
            System.out.println("세션 쿠키 정보: " + sessionCookie);
        }
    }

    @Test  
    @DisplayName("4. 로그인 성공 시 세션 생성 여부")
    void testSessionOnLoginSuccess() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();
        
        System.out.println("로그인 페이지 방문 후 JSESSIONID: " + hasJSessionId());
        
        // 올바른 비밀번호로 로그인
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("input[type='submit']");
        page.waitForLoadState();
        
        System.out.println("로그인 성공 후 JSESSIONID: " + hasJSessionId());
        assertThat(hasJSessionId()).isTrue();
    }
}