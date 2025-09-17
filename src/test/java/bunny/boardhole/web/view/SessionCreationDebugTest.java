package bunny.boardhole.web.view;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import bunny.boardhole.testsupport.e2e.E2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * sec:authorize 태그와 세션 생성 관계 테스트
 */
@DisplayName("sec:authorize 세션 생성 테스트")
class SessionCreationDebugTest extends E2ETestBase {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));
    }

    @AfterAll
    static void tearDownBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(5000);
        waitForAppReady();
    }

    private void waitForAppReady() {
        final int maxAttempts = 120;
        int attempts = 0;
        boolean ready = false;

        while (attempts < maxAttempts && !ready) {
            try {
                var response = page.navigate("http://localhost:" + port + "/");
                if (response != null && response.status() == 200)
                    ready = true;
            } catch (Exception e) {
                // 서버가 아직 준비되지 않음
            }

            if (!ready) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                attempts++;
            }
        }

        if (!ready)
            throw new RuntimeException("서버가 60초 내에 준비되지 않았습니다");
    }

    @AfterEach
    void tearDown() {
        if (page != null) page.close();
        if (context != null) context.close();
    }

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