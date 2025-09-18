package bunny.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * sec:authorize 태그와 세션 생성 관계 테스트
 */
@DisplayName("sec:authorize 세션 생성 테스트")
class SessionCreationDebugTest extends ViewE2ETestBase {

    private boolean hasJSessionId() {
        return context.cookies().stream()
                      .anyMatch(cookie -> "JSESSIONID".equals(cookie.name));
    }

    @Test
    @DisplayName("1. 정적 리소스(CSS) 접근 시 세션 생성 여부")
    void testSessionOnStaticResource() {
        page.navigate("http://localhost:" + port + "/css/app.css");
        page.waitForLoadState();

        System.out.println("CSS 파일 접근 후 JSESSIONID: " + hasJSessionId());
        assertThat(hasJSessionId())
                .withFailMessage("정적 리소스 접근시에는 세션이 생성되지 않아야 합니다")
                .isFalse();
    }

    @Test
    @DisplayName("2. API 엔드포인트 접근 시 세션 생성 여부")
    void testSessionOnApiEndpoint() {
        var response = page.navigate("http://localhost:" + port + "/api/boards");

        System.out.println("API 접근 후 JSESSIONID: " + hasJSessionId());
        System.out.println("Response status: " + response.status());

        assertThat(hasJSessionId())
                .withFailMessage("API 엔드포인트 접근시에는 세션이 생성되지 않아야 합니다")
                .isFalse();
    }

    @Test
    @DisplayName("3. sec:authorize가 있는 페이지(홈) 접근 시 세션 생성 여부")
    void testSessionOnPageWithSecAuthorize() {
        page.navigate("http://localhost:" + port + "/");
        page.waitForLoadState();

        // header.html에 sec:authorize 태그가 있음
        System.out.println("홈페이지(sec:authorize 포함) 접근 후 JSESSIONID: " + hasJSessionId());

        // HTML 내용 확인
        String pageContent = page.content();
        boolean hasLoginButton = pageContent.contains("로그인");
        System.out.println("로그인 버튼 표시 여부: " + hasLoginButton);

        // sec:authorize 태그가 있는 페이지에서는 세션이 생성될 수 있음
        if (hasJSessionId()) {
            System.out.println("⚠️ sec:authorize 태그로 인해 세션이 생성되었습니다");
        }
    }

    @Test
    @DisplayName("4. sec:authorize 없는 간단한 테스트 페이지")
    void testSessionOnSimplePage() {
        // 404 페이지를 이용 (sec:authorize 태그 없음)
        var response = page.navigate("http://localhost:" + port + "/non-existent-page");

        System.out.println("404 페이지 접근 후 JSESSIONID: " + hasJSessionId());
        System.out.println("Response status: " + response.status());
    }
}