package bunny.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("로그인 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class LoginViewE2ETest extends ViewE2ETestBase {

    @Test
    @DisplayName("✅ 로그인 페이지 로드 검증")
    void shouldLoadLoginPage() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("form[action='/auth/login']");

        assertThat(page.isVisible("input[name='username']")).isTrue();
        assertThat(page.isVisible("input[name='password']")).isTrue();
        assertThat(page.textContent("h1")).contains("로그인");
    }

    @Test
    @DisplayName("✅ 로그인 성공 후 /boards 리디렉션 검증")
    void shouldRedirectAfterSuccessfulLogin() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("form[action='/auth/login']");

        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("button[type='submit']");

        page.waitForURL("**/boards", new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(5000));
        assertThat(page.url()).contains("/boards");
    }

    @Test
    @DisplayName("✅ 보호된 페이지 접근 후 로그인 시 원래 페이지로 복귀")
    void shouldReturnToProtectedPageAfterLogin() {
        page.navigate("http://localhost:" + port + "/boards/write");
        page.waitForURL("**/auth/login**", new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(5000));

        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("button[type='submit']");

        page.waitForURL("**/boards**", new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(8000));
        assertThat(page.url()).contains("/boards");
    }

    @Test
    @DisplayName("✅ 잘못된 비밀번호로 로그인 실패 시 오류 표시")
    void shouldShowErrorOnWrongPassword() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("form[action='/auth/login']");

        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "WrongPassword!");
        page.click("button[type='submit']");

        page.waitForSelector("form[action='/auth/login'] [role='alert']",
                new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
        String errorText = page.textContent("form[action='/auth/login'] [role='alert']");
        assertThat(errorText).containsAnyOf("잘못된", "Invalid");

        // 실패 후 비밀번호 필드는 비어 있어야 함
        String pw = page.inputValue("form[action='/auth/login'] input[name='password']");
        assertThat(pw).isEmpty();
    }

}
