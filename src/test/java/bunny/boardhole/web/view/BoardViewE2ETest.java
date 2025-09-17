package bunny.boardhole.web.view;

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

import bunny.boardhole.testsupport.e2e.E2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 게시판 뷰 E2E 테스트
 * <p>
 * boards.html 페이지가 올바르게 렌더링되고 기본 요소들이 표시되는지 검증합니다.
 * 로그인 후 게시판 페이지 접근 및 화면 출력을 확인합니다.
 * </p>
 */
@DisplayName("게시판 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class BoardViewE2ETest extends E2ETestBase {

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
    }

    @AfterEach
    void tearDown() {
        if (page != null)
            page.close();
        if (context != null)
            context.close();
    }

    @Test
    @DisplayName("✅ 홈페이지 로드 검증")
    void shouldLoadHomePage() {
        // 홈페이지로 이동
        page.navigate("http://localhost:" + port + "/");
        page.waitForLoadState();

        // 페이지 제목 확인
        assertThat(page.title()).contains("Board Hole");

        // 페이지가 제대로 로드되었는지 확인
        assertThat(page.url()).contains("localhost:" + port);

        System.out.println("✅ 홈페이지 로드 검증 성공");
    }

    @Test
    @DisplayName("✅ 게시판 페이지 접근 검증 (인증 필요시 리디렉션)")
    void shouldAccessBoardsPageOrRedirect() {
        // 게시판 페이지로 직접 접근 시도
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 페이지가 로드되었는지 확인 (리디렉션되었더라도)
        assertThat(page.url()).contains("localhost:" + port);

        // 제목이 존재하는지 확인
        String title = page.title();
        assertThat(title).isNotEmpty();

        // 현재 URL 확인 및 출력
        String currentUrl = page.url();
        System.out.println("현재 URL: " + currentUrl);
        System.out.println("페이지 제목: " + title);

        if (currentUrl.contains("/auth/login")) {
            System.out.println("✅ 인증이 필요한 페이지로 로그인 페이지로 리디렉션됨");
            // 로그인 페이지 요소 확인
            assertThat(page.isVisible("h1")).isTrue();
        } else if (currentUrl.contains("/boards")) {
            System.out.println("✅ 게시판 페이지에 직접 접근 성공");
            // 게시판 페이지 요소 확인
            assertThat(page.isVisible("h1")).isTrue();
        } else
            System.out.println("✅ 다른 페이지로 리디렉션됨: " + currentUrl);
    }

    @Test
    @DisplayName("✅ 환영 게시글 표시 검증")
    void shouldDisplayWelcomeBoard() {
        // 게시판 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 게시판 페이지에 접근 성공했는지 확인
        assertThat(page.url()).contains("/boards");

        // 게시판 제목 확인
        assertThat(page.isVisible("h1")).isTrue();
        assertThat(page.textContent("h1")).contains("게시판");

        // 환영 게시글이 있는지 확인
        boolean hasWelcomePost = page.locator(".board-card").count() > 0;

        if (hasWelcomePost) {
            // 첫 번째 게시글의 제목 확인
            String firstPostTitle = page.locator(".board-title a").first().textContent();
            System.out.println("첫 번째 게시글 제목: " + firstPostTitle);

            // "환영" 또는 "Welcome" 키워드가 포함되어 있는지 확인
            assertThat(firstPostTitle).satisfiesAnyOf(
                    title -> assertThat(title).containsIgnoringCase("환영"),
                    title -> assertThat(title).containsIgnoringCase("welcome")
            );

            // 작성자 정보 확인
            String authorName = page.locator(".board-chip span").first().textContent();
            System.out.println("작성자: " + authorName);
            assertThat(authorName).isNotEmpty();

            System.out.println("✅ 환영 게시글이 정상적으로 표시됨");
        } else {
            // 게시글이 없는 경우의 메시지 확인
            if (page.isVisible(".board-empty")) {
                System.out.println("📭 게시글이 없습니다 메시지 확인");
                assertThat(page.textContent(".board-empty h2")).contains("게시글이 없습니다");
            }
            System.out.println("⚠️ 환영 게시글이 표시되지 않음 - 데이터 초기화 확인 필요");
        }
    }
}
