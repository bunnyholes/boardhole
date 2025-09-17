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
 * 로그인 뷰 E2E 테스트
 * <p>
 * 로그인 페이지에서 admin 계정으로 로그인 시 JSESSIONID 쿠키가 올바르게 발급되는지 검증합니다.
 * 이전에 SESSION 쿠키가 발급되던 문제가 해결되어 JSESSIONID 쿠키가 발급되는지 확인합니다.
 * </p>
 */
@DisplayName("로그인 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class LoginViewE2ETest extends E2ETestBase {

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

        // 앱이 준비될 때까지 대기 (Playwright로 직접 체크)
        waitForAppReady();
    }

    private void waitForAppReady() {
        // Playwright로 서버가 준비될 때까지 대기
        final int maxAttempts = 240; // 120초 (500ms * 240)
        int attempts = 0;
        boolean ready = false;

        while (attempts < maxAttempts && !ready) {
            try {
                var response = page.navigate("http://localhost:" + port + "/auth/login");
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

    @Test
    @DisplayName("✅ 로그인 페이지 로드 검증")
    void shouldLoadLoginPage() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("input[name='username']");

        // 페이지 제목 확인
        assertThat(page.title()).contains("Board Hole");

        // 로그인 폼 요소들 확인
        assertThat(page.isVisible("input[name='username']")).isTrue();
        assertThat(page.isVisible("input[name='password']")).isTrue();
        assertThat(page.isVisible("input[type='submit']")).isTrue();
        assertThat(page.textContent("h1")).contains("로그인");
    }

    @Test
    @DisplayName("✅ 로그인 페이지 HTTP 응답 검증")
    void shouldRenderLoginPageWithoutServerError() {
        // Playwright로 HTTP 응답 검증
        var response = page.navigate("http://localhost:" + port + "/auth/login");

        // HTTP 상태 코드 검증
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(200);

        // Content-Type 헤더 검증
        String contentType = response.headers().get("content-type");
        assertThat(contentType)
                .isNotNull()
                .contains("text/html")
                .contains("charset=UTF-8");

        // 페이지가 정상적으로 로드되었는지 확인
        page.waitForLoadState();
        assertThat(page.title()).isNotNull();
    }

    @Test
    @DisplayName("✅ admin 계정 로그인 후 JSESSIONID 쿠키 발급 검증")
    void shouldIssueJSESSIONIDCookieAfterLogin() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("form[action='/auth/login']");

        // admin 계정으로 로그인 폼 입력
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");

        // 로그인 버튼 클릭
        page.click("input[type='submit']");

        // 로그인 처리 완료 대기 (리디렉션 또는 페이지 변경)
        page.waitForLoadState();

        // 현재 URL 확인 (성공 시 /boards로 리디렉션, 실패 시 /auth/login 유지)
        String currentUrl = page.url();
        System.out.println("로그인 후 현재 URL: " + currentUrl);

        // 브라우저 컨텍스트에서 모든 쿠키 가져오기
        List<Cookie> cookies = context.cookies();

        // 디버깅용: 모든 쿠키 출력
        System.out.println("=== 로그인 후 발급된 쿠키 목록 ===");
        for (Cookie cookie : cookies)
            System.out.printf("쿠키명: %s, 값: %s, 도메인: %s, 경로: %s%n",
                    cookie.name, cookie.value, cookie.domain, cookie.path);

        // JSESSIONID 쿠키 검증
        Cookie jsessionIdCookie = cookies.stream()
                                         .filter(cookie -> "JSESSIONID".equals(cookie.name))
                                         .findFirst()
                                         .orElse(null);

        // JSESSIONID 쿠키가 발급되었는지 확인
        assertThat(jsessionIdCookie)
                .withFailMessage("JSESSIONID 쿠키가 발급되지 않았습니다. 발급된 쿠키: %s",
                        cookies.stream().map(c -> c.name).toList())
                .isNotNull();

        // JSESSIONID 쿠키 값이 유효한지 확인
        assertThat(jsessionIdCookie.value)
                .withFailMessage("JSESSIONID 쿠키 값이 비어있습니다")
                .isNotEmpty();

        // SESSION 쿠키가 발급되지 않았는지 확인 (이전 문제 상황)
        Cookie sessionCookie = cookies.stream()
                                      .filter(cookie -> "SESSION".equals(cookie.name))
                                      .findFirst()
                                      .orElse(null);

        assertThat(sessionCookie)
                .withFailMessage("SESSION 쿠키가 발급되었습니다. JSESSIONID가 아닌 SESSION 쿠키 발급은 문제입니다.")
                .isNull();

        System.out.println("✅ JSESSIONID 쿠키 발급 검증 성공: " + jsessionIdCookie.value);
    }

    @Test
    @DisplayName("✅ 로그인 성공 후 리디렉션 검증")
    void shouldRedirectAfterSuccessfulLogin() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("input[name='username']");

        // admin 계정으로 로그인
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Admin123!");
        page.click("input[type='submit']");

        // 리디렉션 대기 (최대 5초)
        page.waitForURL("**/boards", new Page.WaitForURLOptions().setTimeout(5000));

        // 게시판 페이지로 리디렉션되었는지 확인
        String currentUrl = page.url();
        assertThat(currentUrl).endsWith("/boards");

        // 페이지 콘텐츠 확인 (인증된 사용자만 접근 가능)
        assertThat(page.title()).contains("Board Hole");
    }

    @Test
    @DisplayName("✅ 잘못된 비밀번호로 로그인 실패 검증")
    void shouldFailLoginWithWrongPassword() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("input[name='username']");

        // 잘못된 비밀번호로 로그인 시도
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "WrongPassword!");
        page.click("input[type='submit']");

        // 페이지 로드 대기
        page.waitForLoadState();

        // 글로벌 에러 등장 대기
        page.waitForSelector("form[action='/auth/login'] .global-error", new Page.WaitForSelectorOptions().setTimeout(5000));

        // 로그인 페이지에 머물러있는지 확인 (리디렉션되지 않음)
        String currentUrl = page.url();
        assertThat(currentUrl).contains("/auth/login");

        // 글로벌 에러 표시 확인
        // 폼 내부 global-error block 존재 및 텍스트 확인 (국/영 메시지 지원)
        int globalErrorCount = page.locator("form[action='/auth/login'] .global-error del").count();
        assertThat(globalErrorCount)
                .withFailMessage("로그인 실패 시 글로벌 에러 메시지가 표시되어야 합니다")
                .isGreaterThan(0);

        String globalErrorText = page.textContent("form[action='/auth/login'] .global-error");
        assertThat(globalErrorText)
                .containsAnyOf("잘못된", "Invalid")
                .containsAnyOf("사용자명", "사용자이름", "username");

        // 두 필드 모두 aria-invalid="true"
        assertThat(page.getAttribute("input[name='username']", "aria-invalid")).isEqualTo("true");
        assertThat(page.getAttribute("input[name='password']", "aria-invalid")).isEqualTo("true");

        // 쿠키가 발급되지 않았는지 확인
        List<Cookie> cookies = context.cookies();
        Cookie jsessionIdCookie = cookies.stream()
                                         .filter(cookie -> "JSESSIONID".equals(cookie.name))
                                         .findFirst()
                                         .orElse(null);

        assertThat(jsessionIdCookie)
                .withFailMessage("로그인 실패 시에는 JSESSIONID 쿠키가 발급되지 않아야 합니다")
                .isNull();
    }

    @Test
    @DisplayName("✅ 초기 로그인 페이지 - aria-invalid 속성 없음")
    void shouldNotHaveAriaInvalidOnInitialLoad() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("input[name='username']");

        // username 필드의 aria-invalid 속성이 없는지 확인
        String usernameAriaInvalid = page.getAttribute("input[name='username']", "aria-invalid");
        assertThat(usernameAriaInvalid)
                .withFailMessage("초기 로드 시 username 필드에 aria-invalid 속성이 있으면 안됩니다")
                .isNull();

        // password 필드의 aria-invalid 속성이 없는지 확인
        String passwordAriaInvalid = page.getAttribute("input[name='password']", "aria-invalid");
        assertThat(passwordAriaInvalid)
                .withFailMessage("초기 로드 시 password 필드에 aria-invalid 속성이 있으면 안됩니다")
                .isNull();

        // aria-describedby 속성도 없는지 확인
        String usernameAriaDescribedby = page.getAttribute("input[name='username']", "aria-describedby");
        String passwordAriaDescribedby = page.getAttribute("input[name='password']", "aria-describedby");

        assertThat(usernameAriaDescribedby)
                .withFailMessage("초기 로드 시 username 필드에 aria-describedby 속성이 있으면 안됩니다")
                .isNull();
        assertThat(passwordAriaDescribedby)
                .withFailMessage("초기 로드 시 password 필드에 aria-describedby 속성이 있으면 안됩니다")
                .isNull();
    }

    @Test
    @DisplayName("✅ 빈 값 제출 후 - aria-invalid='true' 속성 확인")
    void shouldHaveAriaInvalidTrueAfterEmptySubmission() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForSelector("input[name='username']");

        // 빈 값으로 로그인 시도 (NotBlank 검증 실패)
        page.click("input[type='submit']");
        page.waitForLoadState();

        // username 필드의 aria-invalid="true" 확인
        String usernameAriaInvalid = page.getAttribute("input[name='username']", "aria-invalid");
        assertThat(usernameAriaInvalid)
                .withFailMessage("빈 값 제출 후 username 필드에 aria-invalid='true'가 있어야 합니다")
                .isEqualTo("true");

        // password 필드의 aria-invalid="true" 확인
        String passwordAriaInvalid = page.getAttribute("input[name='password']", "aria-invalid");
        assertThat(passwordAriaInvalid)
                .withFailMessage("빈 값 제출 후 password 필드에 aria-invalid='true'가 있어야 합니다")
                .isEqualTo("true");

        // aria-describedby 속성도 있는지 확인 (에러 메시지 연결)
        String usernameAriaDescribedby = page.getAttribute("input[name='username']", "aria-describedby");
        String passwordAriaDescribedby = page.getAttribute("input[name='password']", "aria-describedby");

        assertThat(usernameAriaDescribedby)
                .withFailMessage("검증 실패 시 username 필드에 aria-describedby가 있어야 합니다")
                .isEqualTo("username-help");
        assertThat(passwordAriaDescribedby)
                .withFailMessage("검증 실패 시 password 필드에 aria-describedby가 있어야 합니다")
                .isEqualTo("password-help");

        // 에러 메시지도 표시되는지 확인
        assertThat(page.isVisible("#username-help")).isTrue();
        assertThat(page.isVisible("#password-help")).isTrue();
    }

    @Test
    @DisplayName("✅ username 유효, password 무효 시 - aria-invalid='false'와 'true' 혼재")
    void shouldHaveAriaInvalidFalseForValidUsernameAndTrueForInvalidPassword() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();

        // username은 유효하게, password는 빈 값으로 제출
        page.fill("input[name='username']", "admin");
        // password는 빈 값으로 남겨둠
        page.click("input[type='submit']");
        page.waitForLoadState();

        // username 필드는 aria-invalid="false" (검증은 수행됐지만 통과)
        String usernameAriaInvalid = page.getAttribute("input[name='username']", "aria-invalid");
        assertThat(usernameAriaInvalid)
                .withFailMessage("유효한 username 필드는 aria-invalid='false'여야 합니다")
                .isEqualTo("false");

        // password 필드는 aria-invalid="true" (검증 실패)
        String passwordAriaInvalid = page.getAttribute("input[name='password']", "aria-invalid");
        assertThat(passwordAriaInvalid)
                .withFailMessage("무효한 password 필드는 aria-invalid='true'여야 합니다")
                .isEqualTo("true");

        // username은 aria-describedby 없음 (에러 없음)
        String usernameAriaDescribedby = page.getAttribute("input[name='username']", "aria-describedby");
        assertThat(usernameAriaDescribedby)
                .withFailMessage("유효한 username 필드는 aria-describedby가 없어야 합니다")
                .isNull();

        // password는 aria-describedby 있음 (에러 메시지 연결)
        String passwordAriaDescribedby = page.getAttribute("input[name='password']", "aria-describedby");
        assertThat(passwordAriaDescribedby)
                .withFailMessage("무효한 password 필드는 aria-describedby가 있어야 합니다")
                .isEqualTo("password-help");

        // username 에러 메시지는 없고, password 에러 메시지는 있어야 함
        assertThat(page.isVisible("#username-help")).isFalse();
        assertThat(page.isVisible("#password-help")).isTrue();
    }

    @Test
    @DisplayName("✅ password 유효, username 무효 시 - aria-invalid='true'와 'false' 혼재")
    void shouldHaveAriaInvalidTrueForInvalidUsernameAndFalseForValidPassword() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();

        // username은 빈 값으로, password는 유효하게 제출
        // username은 빈 값으로 남겨둠
        page.fill("input[name='password']", "ValidPassword123!");
        page.click("input[type='submit']");
        page.waitForLoadState();

        // username 필드는 aria-invalid="true" (검증 실패)
        String usernameAriaInvalid = page.getAttribute("input[name='username']", "aria-invalid");
        assertThat(usernameAriaInvalid)
                .withFailMessage("무효한 username 필드는 aria-invalid='true'여야 합니다")
                .isEqualTo("true");

        // password 필드는 aria-invalid="false" (검증은 수행됐지만 통과)
        String passwordAriaInvalid = page.getAttribute("input[name='password']", "aria-invalid");
        assertThat(passwordAriaInvalid)
                .withFailMessage("유효한 password 필드는 aria-invalid='false'여야 합니다")
                .isEqualTo("false");

        // username은 aria-describedby 있음 (에러 메시지 연결)
        String usernameAriaDescribedby = page.getAttribute("input[name='username']", "aria-describedby");
        assertThat(usernameAriaDescribedby)
                .withFailMessage("무효한 username 필드는 aria-describedby가 있어야 합니다")
                .isEqualTo("username-help");

        // password는 aria-describedby 없음 (에러 없음)
        String passwordAriaDescribedby = page.getAttribute("input[name='password']", "aria-describedby");
        assertThat(passwordAriaDescribedby)
                .withFailMessage("유효한 password 필드는 aria-describedby가 없어야 합니다")
                .isNull();

        // username 에러 메시지는 있고, password 에러 메시지는 없어야 함
        assertThat(page.isVisible("#username-help")).isTrue();
        assertThat(page.isVisible("#password-help")).isFalse();
    }

}
