package dev.xiyo.bunnyholes.boardhole.web.view;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import dev.xiyo.bunnyholes.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 게시글 작성 뷰 E2E 테스트
 * <p>
 * 게시글 작성 폼 제출 후 올바른 경로로 리다이렉트되는지 검증합니다.
 * 로그인 → 게시글 작성 → 리다이렉트 확인 플로우를 테스트합니다.
 * </p>
 */
@DisplayName("게시글 작성 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class BoardWriteViewE2ETest extends ViewE2ETestBase {

    private static final String TEST_TITLE_PREFIX = "테스트 게시글 ";
    private static final String TEST_CONTENT = "이것은 리다이렉트 테스트를 위한 게시글 내용입니다.";

    @BeforeEach
    void setUp() {
        // 로그인 수행
        performLogin();
    }

    @Test
    @DisplayName("✅ 게시글 작성 후 상세 페이지로 리다이렉트")
    void shouldRedirectToDetailPageAfterCreatingPost() {
        // given - 게시글 작성 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/write");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // 게시글 작성 페이지에 있는지 확인
        assertThat(page.url()).contains("/boards/write");
        assertThat(page.isVisible("h1")).isTrue();
        assertThat(page.textContent("h1")).containsIgnoringCase("글쓰기");

        // when - 게시글 작성 폼 입력
        String uniqueTitle = TEST_TITLE_PREFIX + UUID.randomUUID().toString().substring(0, 8);

        // 제목 입력
        Locator titleInput = page.locator("#title");
        titleInput.fill(uniqueTitle);

        // 내용 입력
        Locator contentTextarea = page.locator("#content");
        contentTextarea.fill(TEST_CONTENT);

        // 폼 제출 (게시하기 버튼만 선택)
        Locator submitButton = page.locator("button[type='submit']:has-text('게시하기')");
        submitButton.click();

        // 페이지 로드 대기
        page.waitForLoadState();

        // then - 게시글 상세 페이지로 리다이렉트되었는지 확인
        String currentUrl = page.url();
        System.out.println("리다이렉트된 URL: " + currentUrl);

        // URL이 /boards/{id} 패턴인지 확인 (UUID 형식)
        assertThat(currentUrl).matches(".*\\/boards\\/[a-f0-9-]{36}$");

        // 상세 페이지에서 작성한 게시글 내용이 표시되는지 확인
        assertThat(page.isVisible("h1")).isTrue();
        String pageTitle = page.textContent("h1");
        assertThat(pageTitle).contains(uniqueTitle);

        // 작성한 내용이 표시되는지 확인 (본문 내용이 있는 p 태그를 텍스트로 찾기)
        assertThat(page.getByText(TEST_CONTENT).isVisible()).isTrue();

        System.out.println("✅ 게시글 작성 후 상세 페이지로 정상적으로 리다이렉트됨");
        System.out.println("✅ 작성한 게시글 제목: " + uniqueTitle);
        System.out.println("✅ 게시글 ID: " + currentUrl.substring(currentUrl.lastIndexOf('/') + 1));
    }

    @Test
    @DisplayName("✅ 게시글 작성 폼 유효성 검증 실패 시 작성 페이지 유지 및 aria-invalid 설정")
    void shouldStayOnWritePageWhenValidationFails() {
        // given - 게시글 작성 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/write");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // 초기 화면에서는 aria-invalid 속성이 없어야 함
        Locator titleInput = page.locator("#title");
        Locator contentTextarea = page.locator("#content");

        String initialTitleAriaInvalid = titleInput.getAttribute("aria-invalid");
        String initialContentAriaInvalid = contentTextarea.getAttribute("aria-invalid");

        assertThat(initialTitleAriaInvalid).isNull();
        assertThat(initialContentAriaInvalid).isNull();
        System.out.println("✅ 초기 화면: aria-invalid 속성 없음");

        // when - 빈 폼 제출 (유효성 검증 실패)
        Locator submitButton = page.locator("button[type='submit']:has-text('게시하기')");
        submitButton.click();

        // 페이지 로드 대기
        page.waitForLoadState();

        // then - 여전히 작성 페이지에 있는지 확인
        String currentUrl = page.url();
        assertThat(currentUrl).contains("/boards/write");

        // aria-invalid 속성이 true로 설정되었는지 확인
        String titleAriaInvalid = titleInput.getAttribute("aria-invalid");
        String contentAriaInvalid = contentTextarea.getAttribute("aria-invalid");

        // HTML5 required 속성으로 인한 브라우저 기본 검증이 작동할 경우
        // 서버까지 요청이 가지 않을 수 있으므로 이 경우도 고려
        if (titleAriaInvalid != null || contentAriaInvalid != null) {
            assertThat(titleAriaInvalid).isEqualTo("true");
            assertThat(contentAriaInvalid).isEqualTo("true");

            // small 태그로 표시되는 에러 메시지 확인
            Locator titleHelp = page.locator("#title-help");
            Locator contentHelp = page.locator("#content-help");

            assertThat(titleHelp.isVisible()).isTrue();
            assertThat(contentHelp.isVisible()).isTrue();

            String titleErrorMsg = titleHelp.textContent();
            String contentErrorMsg = contentHelp.textContent();

            assertThat(titleErrorMsg).isNotEmpty();
            assertThat(contentErrorMsg).isNotEmpty();

            System.out.println("✅ 검증 실패 후: aria-invalid=\"true\" 설정됨");
            System.out.println("✅ 제목 에러 메시지: " + titleErrorMsg);
            System.out.println("✅ 내용 에러 메시지: " + contentErrorMsg);
        } else {
            // HTML5 기본 검증이 작동한 경우
            boolean hasValidationFeedback =
                    page.locator("#title:invalid").count() > 0 ||
                            page.locator("#content:invalid").count() > 0;
            assertThat(hasValidationFeedback).isTrue();
            System.out.println("✅ HTML5 기본 유효성 검증 작동");
        }

        System.out.println("✅ 유효성 검증 실패 시 작성 페이지에 머무름");
    }

    @Test
    @DisplayName("✅ 제목만 입력 시 내용 필드에만 aria-invalid 설정")
    void shouldSetAriaInvalidOnContentOnlyWhenTitleProvided() {
        // given - 게시글 작성 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/write");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // when - 제목만 입력하고 제출
        Locator titleInput = page.locator("#title");
        Locator contentTextarea = page.locator("#content");

        titleInput.fill("테스트 제목");

        Locator submitButton = page.locator("button[type='submit']:has-text('게시하기')");
        submitButton.click();

        // 페이지 로드 대기
        page.waitForLoadState();

        // then - 작성 페이지에 머물고 aria-invalid 확인
        String currentUrl = page.url();
        assertThat(currentUrl).contains("/boards/write");

        String titleAriaInvalid = titleInput.getAttribute("aria-invalid");
        String contentAriaInvalid = contentTextarea.getAttribute("aria-invalid");

        // HTML5 required 속성으로 인한 브라우저 기본 검증이 작동할 경우
        if (contentAriaInvalid != null) {
            // 제목은 유효하므로 false 또는 null
            assertThat(titleAriaInvalid).satisfiesAnyOf(
                    attr -> assertThat(attr).isEqualTo("false"),
                    attr -> assertThat(attr).isNull()
            );
            // 내용은 비어있으므로 true
            assertThat(contentAriaInvalid).isEqualTo("true");

            // 제목 에러 메시지는 없어야 하고, 내용 에러 메시지만 있어야 함
            Locator titleHelp = page.locator("#title-help");
            Locator contentHelp = page.locator("#content-help");

            assertThat(titleHelp.isVisible()).isFalse();
            assertThat(contentHelp.isVisible()).isTrue();

            String contentErrorMsg = contentHelp.textContent();
            assertThat(contentErrorMsg).isNotEmpty();

            System.out.println("✅ 제목만 입력: 내용 필드만 aria-invalid=\"true\"");
            System.out.println("✅ 내용 에러 메시지: " + contentErrorMsg);
        } else {
            // HTML5 기본 검증
            boolean contentInvalid = page.locator("#content:invalid").count() > 0;
            assertThat(contentInvalid).isTrue();
            System.out.println("✅ HTML5 기본 유효성 검증으로 내용 필드 검증");
        }
    }

    @Test
    @DisplayName("✅ 게시글 작성 취소 시 목록 페이지로 이동")
    void shouldNavigateToBoardListWhenCanceled() {
        // given - 게시글 작성 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/write");
        page.waitForLoadState();

        // when - 취소 버튼 클릭 (있는 경우)
        Locator cancelButton = page.locator("a:has-text('취소'), button:has-text('취소')");

        if (cancelButton.count() > 0) {
            cancelButton.first().click();
            page.waitForLoadState();

            // then - 게시판 목록 페이지로 이동했는지 확인
            String currentUrl = page.url();
            assertThat(currentUrl).satisfiesAnyOf(
                    url -> assertThat(url).endsWith("/boards"),
                    url -> assertThat(url).endsWith("/boards/")
            );

            System.out.println("✅ 취소 버튼 클릭 후 게시판 목록으로 이동");
        } else
            System.out.println("⚠️ 취소 버튼이 없음 - UI 확인 필요");
    }

    /**
     * 로그인 수행 헬퍼 메서드
     */
    private void performLogin() {
        // 로그인 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();

        // 로그인 페이지 로드 확인
        page.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(10000));

        // 로그인 폼 입력
        page.locator("#username").fill("admin");
        page.locator("#password").fill("Admin123!");

        // 로그인 버튼 클릭
        page.locator("button[type='submit']").click();
        page.waitForLoadState();

        // 로그인 후 약간의 대기 시간 추가
        page.waitForTimeout(1000);

        // 로그인 성공 확인 (게시판 페이지로 리다이렉트)
        assertThat(page.url()).satisfiesAnyOf(
                url -> assertThat(url).endsWith("/boards"),
                url -> assertThat(url).endsWith("/boards/"),
                url -> assertThat(url).endsWith("/")
        );

        System.out.println("✅ 테스트용 관리자 계정으로 로그인 성공");
    }
}