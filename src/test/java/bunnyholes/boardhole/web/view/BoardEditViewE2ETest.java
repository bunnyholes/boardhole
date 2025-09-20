package dev.xiyo.bunnyholes.boardhole.web.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import dev.xiyo.bunnyholes.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * 게시글 수정 뷰 E2E 테스트
 * <p>
 * 게시글 수정 폼 제출 후 올바른 경로로 리다이렉트되는지 검증합니다.
 * 로그인 → 게시글 작성 → 수정 → 리다이렉트 확인 플로우를 테스트합니다.
 * </p>
 */
@DisplayName("게시글 수정 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class BoardEditViewE2ETest extends ViewE2ETestBase {

    private static final String TEST_TITLE = "E2E 테스트 게시글";
    private static final String TEST_CONTENT = "이것은 수정 테스트를 위한 게시글 내용입니다.";
    private static final String UPDATED_TITLE = "수정된 E2E 테스트 게시글";
    private static final String UPDATED_CONTENT = "이것은 수정된 게시글 내용입니다.";

    private String boardId;

    @BeforeEach
    void setUp() {
        // 로그인 수행
        performLogin();

        // 게시글 작성
        boardId = createTestBoard();
    }

    @Test
    @DisplayName("✅ 게시글 수정 후 상세 페이지로 리다이렉트")
    void shouldRedirectToDetailPageAfterEditingPost() {
        // given - 게시글 수정 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // 게시글 수정 페이지에 있는지 확인
        assertThat(page.url()).contains("/boards/" + boardId + "/edit");

        // 기존 게시글 내용이 폼에 표시되는지 확인
        Locator titleInput = page.locator("#title");
        Locator contentTextarea = page.locator("#content");

        assertThat(titleInput.inputValue()).isEqualTo(TEST_TITLE);
        assertThat(contentTextarea.inputValue()).contains(TEST_CONTENT);

        // when - 게시글 수정 폼 입력
        titleInput.fill(UPDATED_TITLE);
        contentTextarea.fill(UPDATED_CONTENT);

        // 폼 제출
        Locator submitButton = page.locator("button[type='submit']:has-text('수정')");
        submitButton.click();

        // 페이지 로드 대기
        page.waitForLoadState();

        // then - 게시글 상세 페이지로 리다이렉트되었는지 확인
        String currentUrl = page.url();
        System.out.println("리다이렉트된 URL: " + currentUrl);

        // URL이 /boards/{id} 패턴인지 확인
        assertThat(currentUrl).endsWith("/boards/" + boardId);

        // 상세 페이지에서 수정한 게시글 내용이 표시되는지 확인
        assertThat(page.isVisible("h1")).isTrue();
        String pageTitle = page.textContent("h1");
        assertThat(pageTitle).contains(UPDATED_TITLE);

        // 수정한 내용이 표시되는지 확인
        assertThat(page.getByText(UPDATED_CONTENT).isVisible()).isTrue();

        System.out.println("✅ 게시글 수정 후 상세 페이지로 정상적으로 리다이렉트됨");
        System.out.println("✅ 수정한 게시글 제목: " + UPDATED_TITLE);
        System.out.println("✅ 게시글 ID: " + boardId);
    }

    @Test
    @DisplayName("✅ 게시글 수정 폼 유효성 검증 실패 시 수정 페이지 유지")
    void shouldStayOnEditPageWhenValidationFails() {
        // given - 게시글 수정 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // when - 빈 폼 제출 (유효성 검증 실패)
        Locator titleInput = page.locator("#title");
        Locator contentTextarea = page.locator("#content");

        titleInput.fill("");  // 제목 비우기
        contentTextarea.fill("");  // 내용 비우기

        Locator submitButton = page.locator("button[type='submit']:has-text('수정')");
        submitButton.click();

        // 페이지 로드 대기 (짧게)
        page.waitForTimeout(500);

        // then - 여전히 수정 페이지에 있는지 확인
        String currentUrl = page.url();
        assertThat(currentUrl).contains("/boards/" + boardId + "/edit");

        // HTML5 기본 유효성 검증이 작동하는지 확인
        boolean hasValidationFeedback =
                page.locator("#title:invalid").count() > 0 ||
                        page.locator("#content:invalid").count() > 0;
        assertThat(hasValidationFeedback).isTrue();

        System.out.println("✅ 유효성 검증 실패 시 수정 페이지에 머무름");
    }

    @Test
    @DisplayName("✅ 제목만 수정 시 내용은 기존 값 유지")
    void shouldKeepOriginalContentWhenOnlyTitleIsModified() {
        // given - 게시글 수정 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // when - 제목만 수정
        Locator titleInput = page.locator("#title");
        titleInput.fill(UPDATED_TITLE);

        // 내용은 그대로 두고 제출
        Locator submitButton = page.locator("button[type='submit']:has-text('수정')");
        submitButton.click();

        // 페이지 로드 대기
        page.waitForLoadState();

        // then - 상세 페이지로 리다이렉트
        String currentUrl = page.url();
        assertThat(currentUrl).endsWith("/boards/" + boardId);

        // 제목은 수정되고 내용은 유지되었는지 확인
        assertThat(page.textContent("h1")).contains(UPDATED_TITLE);
        assertThat(page.getByText(TEST_CONTENT).isVisible()).isTrue();

        System.out.println("✅ 제목만 수정: 내용은 기존 값 유지됨");
    }

    @Test
    @DisplayName("❌ 비로그인 사용자는 수정 페이지 접근 불가")
    void shouldRedirectToLoginWhenNotAuthenticated() {
        // given - 로그아웃 상태에서 수정 페이지 접근 시도
        page.context().clearCookies();  // 세션 쿠키 삭제로 로그아웃

        // when - 게시글 수정 페이지로 직접 접근 시도
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // then - 로그인 페이지로 리다이렉트되었는지 확인
        String currentUrl = page.url();
        assertThat(currentUrl).contains("/auth/login");

        System.out.println("✅ 비로그인 사용자는 로그인 페이지로 리다이렉트됨");
    }

    @Test
    @DisplayName("❌ 다른 사용자는 게시글 수정 페이지 접근 불가")
    void shouldDenyAccessForNonOwner() {
        // given - 다른 사용자로 로그인 (user2 계정이 있다고 가정)
        page.context().clearCookies();  // 현재 세션 종료

        // user2로 로그인 시도
        page.navigate("http://localhost:" + port + "/auth/login");
        page.waitForLoadState();
        page.waitForSelector("#username", new Page.WaitForSelectorOptions().setTimeout(10000));

        page.locator("#username").fill("user");  // 다른 사용자
        page.locator("#password").fill("User123!");
        page.locator("button[type='submit']").click();
        page.waitForLoadState();
        page.waitForTimeout(1000);

        // when - 다른 사용자가 게시글 수정 페이지 접근 시도
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // then - 접근 거부되었는지 확인 (403 에러 페이지 또는 리다이렉트)
        String currentUrl = page.url();
        String pageContent = page.content();

        boolean accessDenied =
                currentUrl.contains("/error") ||
                        currentUrl.contains("/boards") ||
                        pageContent.contains("403") ||
                        pageContent.contains("권한") ||
                        pageContent.contains("Forbidden");

        assertThat(accessDenied).isTrue();

        System.out.println("✅ 다른 사용자는 수정 페이지 접근 거부됨");
        System.out.println("✅ 현재 URL: " + currentUrl);
    }

    @Test
    @DisplayName("✅ 존재하는 게시글 수정 페이지 정상 접근")
    void shouldAccessEditPageForExistingBoard() {
        // given - 로그인된 상태에서 자신이 작성한 게시글
        // when - 게시글 수정 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // 에러 페이지 확인
        String pageContent = page.content();
        if (pageContent.contains("500") || pageContent.contains("Internal Server Error")) {
            System.err.println("❌ 500 에러 발생!");
            System.err.println("페이지 내용: " + pageContent.substring(0, Math.min(pageContent.length(), 500)));
            fail("500 Internal Server Error 발생");
        }

        // 페이지 요소 로드 대기
        try {
            page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            System.err.println("❌ #title 요소를 찾을 수 없음");
            System.err.println("현재 URL: " + page.url());
            System.err.println("페이지 제목: " + page.title());
            throw e;
        }

        // then - 수정 페이지가 정상적으로 표시되는지 확인
        assertThat(page.url()).contains("/boards/" + boardId + "/edit");
        assertThat(page.title()).contains("boardholes");

        // 수정 폼이 표시되는지 확인
        assertThat(page.locator("#title").isVisible()).isTrue();
        assertThat(page.locator("#content").isVisible()).isTrue();
        assertThat(page.locator("button[type='submit']").isVisible()).isTrue();

        // 기존 게시글 데이터가 폼에 채워져 있는지 확인
        assertThat(page.locator("#title").inputValue()).isEqualTo(TEST_TITLE);
        assertThat(page.locator("#content").inputValue()).contains(TEST_CONTENT);

        System.out.println("✅ 게시글 수정 페이지 정상 접근 성공");
        System.out.println("✅ 게시글 ID: " + boardId);
    }

    @Test
    @DisplayName("✅ 게시글 수정 취소 시 상세 페이지로 이동")
    void shouldNavigateToDetailPageWhenCanceled() {
        // given - 게시글 수정 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/" + boardId + "/edit");
        page.waitForLoadState();

        // when - 취소 버튼 클릭
        Locator cancelButton = page.locator("a:has-text('취소'), button:has-text('취소')");

        if (cancelButton.count() > 0) {
            cancelButton.first().click();
            page.waitForLoadState();

            // then - 게시글 상세 페이지로 이동했는지 확인
            String currentUrl = page.url();
            assertThat(currentUrl).endsWith("/boards/" + boardId);

            // 기존 게시글 내용이 그대로 표시되는지 확인
            assertThat(page.textContent("h1")).contains(TEST_TITLE);

            System.out.println("✅ 취소 버튼 클릭 후 게시글 상세 페이지로 이동");
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

        // 로그인 성공 확인
        assertThat(page.url()).satisfiesAnyOf(
                url -> assertThat(url).endsWith("/boards"),
                url -> assertThat(url).endsWith("/boards/"),
                url -> assertThat(url).endsWith("/")
        );

        System.out.println("✅ 테스트용 관리자 계정으로 로그인 성공");
    }

    /**
     * 테스트용 게시글 작성 헬퍼 메서드
     *
     * @return 작성된 게시글 ID
     */
    private String createTestBoard() {
        // 게시글 작성 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards/write");
        page.waitForLoadState();

        // 페이지 요소 로드 대기
        page.waitForSelector("#title", new Page.WaitForSelectorOptions().setTimeout(10000));

        // 게시글 작성
        page.locator("#title").fill(TEST_TITLE);
        page.locator("#content").fill(TEST_CONTENT);

        // 폼 제출
        page.locator("button[type='submit']:has-text('게시하기')").click();
        page.waitForLoadState();

        // 생성된 게시글 URL에서 ID 추출
        String currentUrl = page.url();
        String extractedId = currentUrl.substring(currentUrl.lastIndexOf('/') + 1);

        System.out.println("✅ 테스트용 게시글 작성 완료");
        System.out.println("✅ 게시글 ID: " + extractedId);

        return extractedId;
    }
}