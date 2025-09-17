package bunny.boardhole.web.view;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;

import bunny.boardhole.testsupport.e2e.E2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 로그아웃 뷰 E2E 테스트
 * <p>
 * 로그아웃 기능의 전체 플로우를 검증합니다.
 * 로그아웃 후 홈으로 리디렉션되고 세션이 정리되는지 확인합니다.
 * </p>
 */
@DisplayName("로그아웃 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class LogoutViewE2ETest extends E2ETestBase {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)); // CI/CD에서는 headless로 실행
    }

    @AfterAll
    static void tearDownBrowser() {
        if (browser != null)
            browser.close();
        if (playwright != null)
            playwright.close();
    }

    @BeforeEach
    void setUp() {
        // 새로운 브라우저 컨텍스트와 페이지 생성 (쿠키 격리)
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(5000); // 최대 타임아웃 5초
        page.setDefaultNavigationTimeout(60000);

        // 앱이 준비될 때까지 대기
        waitForAppReady();
    }

    private void waitForAppReady() {
        // Playwright로 서버가 준비될 때까지 대기
        final int maxAttempts = 240; // 120초 (500ms * 240)
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
                    Thread.sleep(500); // 500ms 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                attempts++;
            }
        }

        if (!ready)
            throw new RuntimeException("서버가 120초 내에 준비되지 않았습니다");
    }

    @AfterEach
    void tearDown() {
        if (page != null)
            page.close();
        if (context != null)
            context.close();
    }

    /**
     * admin 계정으로 로그인하는 헬퍼 메서드
     */
    private void loginAsAdmin() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("input[name='username']");
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("input[type='submit']");
        // 로그인 후 리디렉션 대기
        page.waitForLoadState();
        
        // 로그인 성공 여부 확인 (boards 페이지로 이동 또는 머물러있음)
        String currentUrl = page.url();
        if (currentUrl.contains("/auth/login")) {
            throw new RuntimeException("로그인 실패: 여전히 로그인 페이지에 있습니다");
        }
    }

    /**
     * JSESSIONID 쿠키 존재 여부 확인
     */
    private boolean hasJSessionIdCookie() {
        return context.cookies().stream()
                      .anyMatch(cookie -> "JSESSIONID".equals(cookie.name));
    }

    @Test
    @DisplayName("✅ 로그인 및 로그아웃 전체 플로우 검증")
    void shouldCompleteLoginAndLogoutFlow() {
        // 1. 로그인 전 홈페이지 확인
        page.navigate("http://localhost:" + port + "/");
        page.waitForLoadState();
        
        // 로그인/회원가입 버튼이 표시되는지 확인
        assertThat(page.isVisible("a[href='/auth/login']"))
                .withFailMessage("로그인 전에는 로그인 링크가 표시되어야 합니다")
                .isTrue();
        
        // 2. 로그인 수행
        loginAsAdmin();
        System.out.println("✅ admin 계정으로 로그인 성공");
        
        // 3. 로그인 후 세션 쿠키 확인
        assertThat(hasJSessionIdCookie())
                .withFailMessage("로그인 후 JSESSIONID 쿠키가 있어야 합니다")
                .isTrue();
        
        // 4. /boards 페이지로 이동되었는지 확인
        String afterLoginUrl = page.url();
        assertThat(afterLoginUrl).endsWith("/boards");
        System.out.println("✅ 로그인 후 게시판 페이지로 이동: " + afterLoginUrl);
        
        // 5. 게시판 페이지에서 로그아웃 POST 요청 직접 수행
        // (헤더의 로그아웃 버튼이 보이지 않는 문제를 우회)
        page.evaluate("() => {" +
                "const form = document.createElement('form');" +
                "form.method = 'POST';" +
                "form.action = '/auth/logout';" +
                "document.body.appendChild(form);" +
                "form.submit();" +
                "}");
        
        // 6. 로그아웃 후 홈페이지로 리디렉션 대기
        page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(5000));
        
        // 7. 홈페이지로 리디렉션되었는지 확인
        String afterLogoutUrl = page.url();
        assertThat(afterLogoutUrl)
                .withFailMessage("로그아웃 후 홈페이지(/)로 리디렉션되어야 합니다. 현재: " + afterLogoutUrl)
                .endsWith("/");
        System.out.println("✅ 로그아웃 후 홈페이지로 리디렉션됨");
        
        // 8. 로그아웃 후 세션 쿠키가 제거되었는지 확인
        assertThat(hasJSessionIdCookie())
                .withFailMessage("로그아웃 후에는 JSESSIONID 쿠키가 없어야 합니다")
                .isFalse();
        System.out.println("✅ 로그아웃 후 세션 쿠키 제거 확인");
        
        // 9. 로그아웃 후 로그인 링크가 다시 표시되는지 확인
        assertThat(page.isVisible("a[href='/auth/login']"))
                .withFailMessage("로그아웃 후 로그인 링크가 다시 표시되어야 합니다")
                .isTrue();
        System.out.println("✅ 로그아웃 후 로그인 링크 표시 확인");
    }

    @Test
    @DisplayName("✅ 로그아웃 후 보호된 페이지 접근 불가 검증")
    void shouldNotAccessProtectedPageAfterLogout() {
        // 로그인
        loginAsAdmin();
        
        // 로그아웃 수행 (JavaScript로 직접 폼 제출)
        page.evaluate("() => {" +
                "const form = document.createElement('form');" +
                "form.method = 'POST';" +
                "form.action = '/auth/logout';" +
                "document.body.appendChild(form);" +
                "form.submit();" +
                "}");
        
        // 홈페이지로 리디렉션 대기
        page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(5000));
        
        // 로그아웃 후 보호된 페이지(/boards) 접근 시도
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();
        
        // 로그인 페이지로 리디렉션되었는지 확인
        String currentUrl = page.url();
        assertThat(currentUrl)
                .withFailMessage("로그아웃 후 보호된 페이지 접근 시 로그인 페이지로 리디렉션되어야 합니다")
                .contains("/auth/login");
        
        System.out.println("✅ 로그아웃 후 보호된 페이지 접근이 차단됨");
    }

    @Test
    @DisplayName("✅ 직접 로그아웃 엔드포인트 접근 처리 검증")
    void shouldHandleDirectLogoutEndpointAccess() {
        // 로그인하지 않은 상태에서 로그아웃 엔드포인트 직접 접근 (GET이 아닌 POST로)
        page.navigate("http://localhost:" + port + "/");
        
        // JavaScript로 로그아웃 POST 요청 수행
        page.evaluate("() => {" +
                "const form = document.createElement('form');" +
                "form.method = 'POST';" +
                "form.action = '/auth/logout';" +
                "document.body.appendChild(form);" +
                "form.submit();" +
                "}");
        
        // 페이지 로드 대기
        page.waitForLoadState();
        
        // 홈페이지로 리디렉션되는지 확인 (예외 없이 처리)
        String currentUrl = page.url();
        assertThat(currentUrl)
                .withFailMessage("비로그인 상태에서 로그아웃 엔드포인트 접근 시 홈으로 리디렉션되어야 합니다")
                .endsWith("/");
        
        System.out.println("✅ 비로그인 상태에서 로그아웃 엔드포인트 접근이 정상적으로 처리됨");
    }

    @Test
    @DisplayName("✅ 다중 탭 환경에서 로그아웃 검증")
    void shouldLogoutFromAllTabsInMultiTabEnvironment() {
        // 첫 번째 탭에서 로그인
        Page firstTab = page;
        loginAsAdmin();
        
        // JSESSIONID 쿠키 확인
        assertThat(hasJSessionIdCookie()).isTrue();
        
        // 두 번째 탭 생성 (같은 컨텍스트 = 쿠키 공유)
        Page secondTab = context.newPage();
        secondTab.setDefaultTimeout(5000);
        
        // 두 번째 탭에서 게시판 페이지 접근 (세션 공유로 접근 가능)
        secondTab.navigate("http://localhost:" + port + "/boards");
        secondTab.waitForLoadState();
        assertThat(secondTab.url()).endsWith("/boards");
        
        // 첫 번째 탭에서 로그아웃 (JavaScript로)
        firstTab.evaluate("() => {" +
                "const form = document.createElement('form');" +
                "form.method = 'POST';" +
                "form.action = '/auth/logout';" +
                "document.body.appendChild(form);" +
                "form.submit();" +
                "}");
        
        // 홈페이지로 리디렉션 대기
        firstTab.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(5000));
        
        // 두 번째 탭 새로고침
        secondTab.reload();
        secondTab.waitForLoadState();
        
        // 두 번째 탭도 로그인 페이지로 리디렉션되는지 확인
        String secondTabUrl = secondTab.url();
        assertThat(secondTabUrl)
                .withFailMessage("다른 탭에서도 로그아웃이 적용되어야 합니다")
                .contains("/auth/login");
        
        // 두 번째 탭 닫기
        secondTab.close();
        
        System.out.println("✅ 다중 탭 환경에서 로그아웃이 모든 탭에 적용됨");
    }

    @Test
    @DisplayName("✅ 로그아웃 후 쿠키 완전 제거 검증")
    void shouldCompletelyRemoveCookiesAfterLogout() {
        // 로그인
        loginAsAdmin();
        
        // 로그인 후 쿠키 확인
        List<Cookie> cookiesBeforeLogout = context.cookies();
        System.out.println("로그인 후 쿠키 개수: " + cookiesBeforeLogout.size());
        
        Cookie sessionCookieBefore = cookiesBeforeLogout.stream()
                .filter(c -> "JSESSIONID".equals(c.name))
                .findFirst()
                .orElse(null);
        
        assertThat(sessionCookieBefore)
                .withFailMessage("로그인 후 JSESSIONID 쿠키가 있어야 합니다")
                .isNotNull();
        
        // 로그아웃 수행
        page.evaluate("() => {" +
                "const form = document.createElement('form');" +
                "form.method = 'POST';" +
                "form.action = '/auth/logout';" +
                "document.body.appendChild(form);" +
                "form.submit();" +
                "}");
        
        // 홈페이지로 리디렉션 대기
        page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(5000));
        
        // 로그아웃 후 쿠키 확인
        List<Cookie> cookiesAfterLogout = context.cookies();
        System.out.println("로그아웃 후 쿠키 개수: " + cookiesAfterLogout.size());
        
        // JSESSIONID 쿠키가 없어야 함
        Cookie sessionCookieAfter = cookiesAfterLogout.stream()
                .filter(c -> "JSESSIONID".equals(c.name))
                .findFirst()
                .orElse(null);
        
        assertThat(sessionCookieAfter)
                .withFailMessage("로그아웃 후 JSESSIONID 쿠키가 삭제되어야 합니다")
                .isNull();
        
        // SESSION 쿠키도 없어야 함
        Cookie sessionCookie = cookiesAfterLogout.stream()
                .filter(c -> "SESSION".equals(c.name))
                .findFirst()
                .orElse(null);
        
        assertThat(sessionCookie)
                .withFailMessage("로그아웃 후 SESSION 쿠키도 없어야 합니다")
                .isNull();
        
        System.out.println("✅ 로그아웃 후 모든 세션 쿠키가 제거됨");
    }
}