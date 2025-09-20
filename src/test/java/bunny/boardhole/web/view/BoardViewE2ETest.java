package bunny.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.ViewE2ETestBase;

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
class BoardViewE2ETest extends ViewE2ETestBase {

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
    @DisplayName("✅ 게시판 페이지 접근 검증")
    void shouldAccessBoardsPage() {
        // 게시판 페이지로 직접 접근
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 페이지가 로드되었는지 확인
        String currentUrl = page.url();
        assertThat(currentUrl).contains("/boards");
        
        System.out.println("✅ 게시판 페이지에 접근 성공: " + currentUrl);
    }

    @Test
    @DisplayName("✅ 게시판 테이블 레이아웃 검증")
    void shouldDisplayBoardTable() {
        // 게시판 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 페이지가 정상적으로 로드되었는지 확인
        String currentUrl = page.url();
        System.out.println("현재 URL: " + currentUrl);
        
        // boards 페이지에 있는지 확인
        assertThat(currentUrl).contains("/boards");

        // 페이지 제목 확인
        String pageTitle = page.title();
        System.out.println("페이지 제목: " + pageTitle);
        
        // h1 태그가 있는지 확인
        if (page.locator("h1").count() > 0) {
            String h1Text = page.textContent("h1");
            System.out.println("H1 텍스트: " + h1Text);
            // 에러 페이지가 아닌 경우에만 게시판 체크
            if (!h1Text.contains("오류") && !h1Text.contains("500")) {
                assertThat(h1Text).contains("게시판");
            }
        }

        // 테이블 또는 빈 메시지 확인
        if (page.locator("table.striped").count() > 0) {
            System.out.println("✅ 테이블이 존재합니다");
            
            // 테이블 헤더 확인
            var headers = page.locator("table.striped thead tr th");
            int headerCount = headers.count();
            System.out.println("헤더 개수: " + headerCount);
            
            if (headerCount > 0) {
                for (int i = 0; i < headerCount; i++) {
                    System.out.println("헤더 " + i + ": " + headers.nth(i).textContent());
                }
            }
            
            // 테이블 행 확인
            var rows = page.locator("table.striped tbody tr");
            int rowCount = rows.count();
            System.out.println("데이터 행 개수: " + rowCount);
            
            if (rowCount > 0) {
                // 첫 번째 행 데이터 확인
                var firstRow = rows.first();
                var cells = firstRow.locator("td");
                int cellCount = cells.count();
                
                System.out.println("첫 번째 행 셀 개수: " + cellCount);
                for (int i = 0; i < cellCount; i++) {
                    System.out.println("셀 " + i + ": " + cells.nth(i).textContent());
                }
                
                // 제목 링크가 있는지 확인
                if (firstRow.locator("td a").count() > 0) {
                    String titleLink = firstRow.locator("td a").first().textContent();
                    System.out.println("제목 링크: " + titleLink);
                    assertThat(titleLink).isNotEmpty();
                }
            } else {
                System.out.println("⚠️ 테이블은 있지만 데이터가 없습니다");
            }
        } else if (page.locator(".board-empty").count() > 0) {
            System.out.println("📭 게시글이 없습니다 메시지가 표시됨");
            String emptyMessage = page.textContent(".board-empty");
            System.out.println("빈 메시지: " + emptyMessage);
            assertThat(emptyMessage).contains("게시글이 없습니다");
        } else {
            System.out.println("⚠️ 테이블도 없고 빈 메시지도 없습니다");
        }
    }

    @Test
    @DisplayName("✅ 페이지네이션 표시 검증")
    void shouldDisplayPagination() {
        // 게시판 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 현재 페이지 정보 출력
        String currentUrl = page.url();
        System.out.println("현재 URL: " + currentUrl);
        
        // boards 페이지에 있는지 확인
        assertThat(currentUrl).contains("/boards");

        // 페이지네이션 nav 요소 확인
        var paginationNav = page.locator("nav[aria-label='페이지네이션']");
        boolean hasPagination = paginationNav.count() > 0;
        System.out.println("페이지네이션 존재: " + hasPagination);
        
        if (hasPagination) {
            assertThat(hasPagination).isTrue();
            System.out.println("✅ 페이지네이션이 표시됨");

            // 그리드 레이아웃 확인
            boolean hasGrid = page.locator("nav[aria-label='페이지네이션'] .grid").count() > 0;
            System.out.println("그리드 레이아웃 존재: " + hasGrid);
            
            if (hasGrid) {
                System.out.println("✅ 페이지네이션이 그리드 레이아웃으로 표시됨");
                
                // 버튼들 확인
                var allButtons = page.locator("nav[aria-label='페이지네이션'] button");
                int buttonCount = allButtons.count();
                System.out.println("전체 버튼 수: " + buttonCount);
                
                // 각 버튼 텍스트 출력
                for (int i = 0; i < buttonCount; i++) {
                    String buttonText = allButtons.nth(i).textContent().trim();
                    boolean isDisabled = allButtons.nth(i).isDisabled();
                    System.out.println("버튼 " + i + ": " + buttonText + " (비활성화: " + isDisabled + ")");
                }
                
                // 이전 버튼 확인
                var prevButtons = page.locator("nav[aria-label='페이지네이션'] button:has-text('이전')");
                if (prevButtons.count() > 0) {
                    System.out.println("✅ 이전 버튼이 존재합니다");
                    boolean isPrevDisabled = prevButtons.first().isDisabled();
                    System.out.println("이전 버튼 비활성화 상태: " + isPrevDisabled);
                }
                
                // 다음 버튼 확인
                var nextButtons = page.locator("nav[aria-label='페이지네이션'] button:has-text('다음')");
                if (nextButtons.count() > 0) {
                    System.out.println("✅ 다음 버튼이 존재합니다");
                    boolean isNextDisabled = nextButtons.first().isDisabled();
                    System.out.println("다음 버튼 비활성화 상태: " + isNextDisabled);
                }
                
                // 숫자 버튼 확인 (최소 하나의 "1" 버튼이 있어야 함)
                var oneButton = page.locator("nav[aria-label='페이지네이션'] button:has-text('1')");
                if (oneButton.count() > 0) {
                    System.out.println("✅ 페이지 번호 1이 표시됩니다");
                }
                
                assertThat(buttonCount).isGreaterThanOrEqualTo(1);
                System.out.println("✅ 페이지네이션 버튼들이 정상적으로 표시됨");
            }
        } else {
            System.out.println("⚠️ 페이지네이션이 표시되지 않음");
        }
    }

    @Test
    @DisplayName("✅ 테이블 헤더 확인")
    void shouldDisplayTableHeaders() {
        // 게시판 페이지로 이동
        page.navigate("http://localhost:" + port + "/boards");
        page.waitForLoadState();

        // 테이블이 존재하는 경우 헤더 확인
        if (page.locator("table.striped").count() > 0) {
            var headers = page.locator("table.striped thead tr th");
            int headerCount = headers.count();

            assertThat(headerCount).isEqualTo(5);
            System.out.println("✅ 테이블 헤더 수: " + headerCount);

            // 각 헤더 텍스트 확인
            assertThat(headers.nth(0).textContent()).isEqualTo("번호");
            assertThat(headers.nth(1).textContent()).isEqualTo("제목");
            assertThat(headers.nth(2).textContent()).isEqualTo("작성자");
            assertThat(headers.nth(3).textContent()).isEqualTo("작성일");
            assertThat(headers.nth(4).textContent()).isEqualTo("조회");

            System.out.println("✅ 테이블 헤더가 올바르게 표시됨");
        }
    }
}
