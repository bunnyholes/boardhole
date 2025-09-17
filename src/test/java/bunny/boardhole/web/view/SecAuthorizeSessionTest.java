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
 * sec:authorize 태그와 세션 생성 관계를 명확히 증명하는 테스트
 */
@DisplayName("sec:authorize와 세션 생성 증명 테스트")
class SecAuthorizeSessionTest extends E2ETestBase {

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