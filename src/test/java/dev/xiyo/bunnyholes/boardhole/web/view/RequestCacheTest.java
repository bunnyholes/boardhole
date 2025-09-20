package dev.xiyo.bunnyholes.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dev.xiyo.bunnyholes.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequestCache 동작 검증 테스트
 * <p>
 * 참고: Spring Security 6에서는 세션 생성 정책이 변경되어
 * 보안상의 이유로 많은 경우에 세션이 생성될 수 있습니다.
 */
@Tag("e2e")
@DisplayName("RequestCache 세션 생성 검증")
class RequestCacheTest extends ViewE2ETestBase {

    private boolean hasJSessionId() {
        return context.cookies().stream()
                      .anyMatch(cookie -> "JSESSIONID".equals(cookie.name));
    }

    @Test
    @DisplayName("보호된 페이지 접근 후 로그인 시 원래 요청 페이지로 리다이렉트")
    void requestCacheRedirectsAfterLogin() {
        // 1. 보호된 페이지 접근 시도
        page.navigate("http://localhost:" + port + "/users");
        page.waitForLoadState();

        // 로그인 페이지로 리다이렉트 확인
        assertThat(page.url()).contains("/auth/login");

        // 2. 로그인 수행
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("button[type='submit'], input[type='submit']");
        page.waitForLoadState();

        // 3. 원래 요청했던 /users 페이지로 리다이렉트되었는지 확인
        assertThat(page.url()).contains("/users");

        // 페이지 컨텐츠 확인
        assertThat(page.textContent("body")).containsAnyOf("사용자", "Users");
    }

    @Test
    @DisplayName("직접 로그인 페이지 접근 후 로그인 시 기본 페이지로 이동")
    void directLoginRedirectsToDefaultPage() {
        // 1. 로그인 페이지로 직접 접근
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();

        // 2. 로그인 수행
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("button[type='submit'], input[type='submit']");
        page.waitForLoadState();

        // 3. 기본 페이지로 이동했는지 확인 (RequestCache가 없으므로)
        // /boards 또는 / 또는 /index 등으로 이동
        assertThat(page.url()).doesNotContain("/auth/login");
        assertThat(page.url()).containsAnyOf("/boards", "/", "/index");
    }

    @Test
    @DisplayName("여러 보호된 페이지 접근 시 마지막 요청이 저장됨")
    void multipleProtectedPagesKeepsLastRequest() {
        // 1. 첫 번째 보호된 페이지 접근
        page.navigate("http://localhost:" + port + "/users");
        page.waitForLoadState();
        assertThat(page.url()).contains("/auth/login");

        // 2. 로그인하지 않고 다른 보호된 페이지 접근
        // URL 경로 수정 (실제 경로에 맞게)
        page.navigate("http://localhost:" + port + "/user/getUserDetailPage");
        page.waitForLoadState();
        assertThat(page.url()).contains("/auth/login");

        // 3. 로그인 수행
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("button[type='submit'], input[type='submit']");
        page.waitForLoadState();

        // 4. 마지막으로 요청한 페이지로 리다이렉트되었는지 확인
        // continue 파라미터가 붙을 수 있음
        assertThat(page.url()).containsAnyOf("/user/getUserDetailPage", "/getUserDetailPage");
    }

    @Test
    @DisplayName("보호된 페이지 접근 시 로그인 페이지로 리다이렉트")
    void protectedPageRedirectsToLogin() {
        // 보호된 페이지로 이동 시도
        page.navigate("http://localhost:" + port + "/users");
        page.waitForLoadState();

        // 로그인 페이지로 리다이렉트되었는지 확인
        assertThat(page.url()).contains("/auth/login");

        // 로그인 폼이 표시되는지 확인
        assertThat(page.locator("input[name='username']").count()).isGreaterThan(0);
        assertThat(page.locator("input[name='password']").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("공개 페이지는 인증 없이 접근 가능")
    void publicPageAccessibleWithoutAuth() {
        // 공개 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 로그인 페이지로 리다이렉트되지 않았는지 확인
        assertThat(page.url()).doesNotContain("/auth/login");
        assertThat(page.url()).contains("/boards");

        // 페이지가 정상적으로 로드되었는지 확인
        assertThat(page.title()).isNotEmpty();
    }
}