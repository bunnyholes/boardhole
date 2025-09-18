package bunny.boardhole.testsupport.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import bunny.boardhole.testsupport.container.ContainersConfig;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainersConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ViewE2ETestBase {

    @LocalServerPort
    protected int port;

    @BeforeAll
    void restAssuredSetup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private static Playwright playwright;
    private static Browser browser;
    protected BrowserContext context;
    protected Page page;

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
                // 세션을 생성하지 않는 공개 페이지로 헬스체크
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

}
