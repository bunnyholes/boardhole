package dev.xiyo.bunnyholes.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Page;

import dev.xiyo.bunnyholes.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 루트 페이지(/) E2E 테스트
 * <p>
 * 루트 페이지의 3개 주요 버튼들이 올바르게 작동하는지 검증합니다:
 * 1. 로그인 버튼 → /auth/login
 * 2. 회원가입 버튼 → /auth/signup
 * 3. 게시판 바로가기 버튼 → /boards
 * </p>
 */
@DisplayName("루트 페이지 E2E 테스트")
@Tag("e2e")
@Tag("view")
class IndexViewE2ETest extends ViewE2ETestBase {

    @Test
    @DisplayName("✅ 루트 페이지 로드 검증")
    void shouldLoadIndexPage() {
        // 페이지 제목 확인
        assertThat(page.title()).contains("Boardholes");

        // 메인 콘텐츠 확인
        assertThat(page.textContent("h1.hero-title")).contains("Boardholes");
        assertThat(page.textContent("p.hero-subtitle")).contains("실무형 게시판");

        // 게시판 바로가기 버튼이 존재하는지 확인
        assertThat(page.isVisible("a[href='/boards'].primary")).isTrue(); // 게시판 바로가기 버튼

        // 헤더에서 로그인과 회원가입 링크 확인  
        assertThat(page.isVisible("nav a[href='/auth/login']")).isTrue(); // 로그인 링크
        assertThat(page.isVisible("nav a[href='/auth/signup']")).isTrue(); // 회원가입 링크
    }

    @Test
    @DisplayName("✅ 로그인 버튼 클릭 → /auth/login 이동")
    void shouldNavigateToLoginPage() {
        // 로그인 링크 찾기 (헤더에 있음)
        page.click("a[href='/auth/login']");

        // 로그인 페이지로 이동했는지 확인
        page.waitForURL("**/auth/login", new Page.WaitForURLOptions().setTimeout(5000));
        assertThat(page.url()).endsWith("/auth/login");

        // 로그인 페이지 콘텐츠 확인
        assertThat(page.textContent("h1")).contains("로그인");
        assertThat(page.isVisible("input[name='username']")).isTrue();
        assertThat(page.isVisible("input[name='password']")).isTrue();
        assertThat(page.isVisible("button[type='submit'], input[type='submit']")).isTrue();
    }

    @Test
    @DisplayName("✅ 회원가입 버튼 클릭 → /auth/signup 이동")
    void shouldNavigateToSignupPage() {
        // 회원가입 링크 찾기 (헤더에 있음)
        page.click("a[href='/auth/signup']");

        // 회원가입 페이지로 이동했는지 확인
        page.waitForURL("**/auth/signup", new Page.WaitForURLOptions().setTimeout(5000));
        assertThat(page.url()).endsWith("/auth/signup");

        // 회원가입 페이지 콘텐츠 확인
        assertThat(page.textContent("h1")).contains("회원가입");
        assertThat(page.isVisible("input[name='username']")).isTrue();
        assertThat(page.isVisible("input[name='password']")).isTrue();
        assertThat(page.isVisible("input[name='confirmPassword']")).isTrue();
        assertThat(page.isVisible("input[name='name']")).isTrue();
        assertThat(page.isVisible("input[name='email']")).isTrue();
        assertThat(page.isVisible("button[type='submit'], input[type='submit']")).isTrue();
    }

    @Test
    @DisplayName("✅ 게시판 바로가기 버튼 클릭 → /boards 이동")
    void shouldNavigateToBoardsPage() {
        // 게시판 바로가기 버튼 클릭 (primary 클래스를 가진 버튼)
        page.click("a[href='/boards'].primary");

        // 게시판 페이지로 이동 시도 확인
        // 비인증 상태에서는 로그인 페이지로 리다이렉트될 수 있음
        page.waitForLoadState();

        // URL이 /boards 또는 /auth/login (리다이렉트)인지 확인
        String currentUrl = page.url();
        assertThat(currentUrl)
                .satisfiesAnyOf(
                        url -> assertThat(url).endsWith("/boards"),
                        url -> assertThat(url).contains("/auth/login")
                );

        // 만약 로그인 페이지로 리다이렉트된 경우
        if (currentUrl.contains("/auth/login"))
            assertThat(page.textContent("h1")).contains("로그인");
        else {
            // 게시판 페이지에 도달한 경우
            String title = page.title();
            assertThat(title.contains("게시판") || title.contains("Board")).isTrue();
        }
    }

    @Test
    @DisplayName("✅ 네비게이션 검증 - 모든 링크가 올바르게 작동")
    void shouldHaveWorkingNavigation() {
        // 헤더의 Boardholes 로고 클릭으로 홈으로 돌아가기
        page.click("a[href='/']");
        page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(5000));
        assertThat(page.url()).endsWith("/");

        // 헤더의 게시판 링크 테스트 (네비게이션 안에 있는지 확인)
        if (page.isVisible("a[href='/boards']")) {
            page.click("a[href='/boards']");
            page.waitForLoadState();

            // 게시판 또는 로그인 페이지로 이동 확인
            String url = page.url();
            assertThat(url).satisfiesAnyOf(
                    u -> assertThat(u).endsWith("/boards"),
                    u -> assertThat(u).contains("/auth/login")
            );
        }
    }

    @Test
    @DisplayName("✅ 반응형 디자인 검증 - 모바일 뷰포트")
    void shouldWorkOnMobileViewport() {
        // 모바일 뷰포트로 변경
        page.setViewportSize(375, 667); // iPhone SE 크기

        // 페이지 새로고침
        page.reload();
        page.waitForLoadState();

        // 메인 게시판 버튼이 여전히 보이는지 확인
        assertThat(page.isVisible("a[href='/boards'].primary")).isTrue();

        // 모바일에서도 로그인 버튼이 정상 작동하는지 테스트
        page.click("a[href='/auth/login']");
        page.waitForURL("**/auth/login", new Page.WaitForURLOptions().setTimeout(5000));
        assertThat(page.url()).endsWith("/auth/login");
    }
}
