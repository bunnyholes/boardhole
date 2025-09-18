package bunny.boardhole.web.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.ViewE2ETestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 회원가입 뷰 E2E 테스트
 * <p>
 * 회원가입 페이지의 UI/UX와 HTML5 validation, 회원가입 플로우를 검증합니다.
 * Pico CSS 기반 디자인과 접근성, 클라이언트 사이드 validation을 확인합니다.
 * </p>
 */
@DisplayName("회원가입 뷰 E2E 테스트")
@Tag("e2e")
@Tag("view")
class SignupViewE2ETest extends ViewE2ETestBase {

    @Test
    @DisplayName("✅ 회원가입 페이지 로드 및 UI 구조 검증")
    void shouldLoadSignupPageWithCorrectStructure() {
        // 회원가입 페이지로 이동
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 페이지 제목 확인
        assertThat(page.title()).contains("Board Hole");

        // Pico CSS 카드 구조 확인 (article > header/main/footer)
        assertThat(page.locator("article").count()).isGreaterThan(0);
        assertThat(page.locator("article header").count()).isGreaterThan(0);
        assertThat(page.locator("article main").count()).isGreaterThan(0);

        // 헤더 영역 확인
        assertThat(page.textContent("article header h1")).contains("회원가입");
        assertThat(page.textContent("article header p")).contains("Board Hole");

        // 폼 필드 요소들 확인
        assertThat(page.locator("input[name='email']").count()).isGreaterThan(0);
        assertThat(page.locator("input[name='name']").count()).isGreaterThan(0);
        assertThat(page.locator("input[name='username']").count()).isGreaterThan(0);
        assertThat(page.locator("input[name='password']").count()).isGreaterThan(0);
        assertThat(page.locator("input[name='confirmPassword']").count()).isGreaterThan(0);
        assertThat(page.locator("input[name='agree']").count()).isGreaterThan(0);
        assertThat(page.locator("button[type='submit'], input[type='submit']").count()).isGreaterThan(0);

        // fieldset으로 그룹화되었는지 확인
        assertThat(page.locator("fieldset").count()).isGreaterThan(0);

        // 로그인 링크 확인
        assertThat(page.locator("a[href='/auth/login']").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("✅ HTML5 validation 동작 검증")
    void shouldValidateFormWithHTML5() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 빈 폼으로 제출 시도 (HTML5 validation이 막아야 함)
        page.click("button[type='submit'], input[type='submit']");

        // 페이지가 변경되지 않았는지 확인 (validation이 작동함)
        assertThat(page.url()).contains("/auth/signup");

        // 이메일 형식 검증
        page.fill("input[name='email']", "invalid-email");
        page.fill("input[name='name']", "테스트사용자");
        page.fill("input[name='username']", "testuser");
        page.fill("input[name='password']", "Test123!");
        page.fill("input[name='confirmPassword']", "Test123!");
        page.check("input[name='agree']");
        page.click("button[type='submit'], input[type='submit']");

        // 잘못된 이메일로 인해 제출이 막혔는지 확인
        assertThat(page.url()).contains("/auth/signup");

        // 사용자명 패턴 검증 (특수문자 포함)
        page.fill("input[name='email']", "test@example.com");
        page.fill("input[name='username']", "test@user"); // 특수문자 포함으로 validation 실패해야 함
        page.click("button[type='submit'], input[type='submit']");

        // 패턴 위반으로 제출이 막혔는지 확인
        assertThat(page.url()).contains("/auth/signup");
    }

    @Test
    @DisplayName("✅ 접근성 속성 확인")
    void shouldHaveAccessibilityAttributes() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // aria-invalid 속성이 초기에는 없어야 함
        assertThat(page.getAttribute("input[name='email']", "aria-invalid")).isNull();

        // required 속성 확인
        assertThat(page.getAttribute("input[name='email']", "required")).isNotNull();
        assertThat(page.getAttribute("input[name='name']", "required")).isNotNull();
        assertThat(page.getAttribute("input[name='username']", "required")).isNotNull();
        assertThat(page.getAttribute("input[name='password']", "required")).isNotNull();
        assertThat(page.getAttribute("input[name='confirmPassword']", "required")).isNotNull();

        // autocomplete 속성 확인
        assertThat(page.getAttribute("input[name='email']", "autocomplete")).isEqualTo("email");
        assertThat(page.getAttribute("input[name='name']", "autocomplete")).isEqualTo("name");
        assertThat(page.getAttribute("input[name='username']", "autocomplete")).isEqualTo("username");
        assertThat(page.getAttribute("input[name='password']", "autocomplete")).isEqualTo("new-password");
        assertThat(page.getAttribute("input[name='confirmPassword']", "autocomplete")).isEqualTo("new-password");

        // 도움말 텍스트 확인 (각 필드별로)
        String allText = page.locator("small").allTextContents().toString();
        assertThat(allText).contains("로그인 확인 및 알림에 사용됩니다");
        assertThat(allText).contains("영문, 숫자 조합으로 3-20자");
        assertThat(allText).contains("8자 이상이며 대/소문자");
    }

    @Test
    @DisplayName("✅ 성공적인 회원가입 플로우")
    void shouldCompleteSignupSuccessfully() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 유효한 회원가입 정보 입력
        String uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
        String uniqueUsername = "testuser" + System.currentTimeMillis();

        page.fill("input[name='email']", uniqueEmail);
        page.fill("input[name='name']", "테스트 사용자");
        page.fill("input[name='username']", uniqueUsername);
        page.fill("input[name='password']", "Test123!");
        page.fill("input[name='confirmPassword']", "Test123!");
        page.check("input[name='agree']");

        // 회원가입 제출
        page.click("button[type='submit'], input[type='submit']");
        page.waitForLoadState();

        // 회원가입 성공 후 메인 페이지로 리디렉션되는지 확인 (자동 로그인 시)
        assertThat(page.url()).containsAnyOf("/auth/login", "/boards");

        // 성공 메시지가 표시되는지 확인 (쿼리 파라미터나 메시지로)
        // 성공 메시지 확인
        if (page.url().contains("signup"))
            assertThat(page.locator("ins").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("✅ 비밀번호 불일치 검증")
    void shouldValidatePasswordMismatch() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 비밀번호와 확인 비밀번호를 다르게 입력
        page.fill("input[name='email']", "test@example.com");
        page.fill("input[name='name']", "테스트사용자");
        page.fill("input[name='username']", "testuser123");
        page.fill("input[name='password']", "Test123!");
        page.fill("input[name='confirmPassword']", "DifferentPassword!");
        page.check("input[name='agree']");

        // 회원가입 시도
        page.click("button[type='submit'], input[type='submit']");
        page.waitForLoadState();

        // 여전히 회원가입 페이지에 있는지 확인 (서버 사이드 validation)
        assertThat(page.url()).contains("/auth/signup");

        // 페이지 내용 출력 (디버깅용)
        System.out.println("Response body: " + page.content());

        // HTML 폼이 여전히 존재하는지 확인
        assertThat(page.locator("form").count()).isGreaterThan(0);

        // aria-invalid 속성 확인
        // confirmPassword 필드가 에러 상태임을 표시해야 함
        String ariaInvalid = page.getAttribute("input[name='confirmPassword']", "aria-invalid");
        assertThat(ariaInvalid).isEqualTo("true");

        // 에러 메시지 확인 - 비밀번호가 일치하지 않는다는 메시지
        if (page.locator("#confirm-password-help").count() > 0) {
            String errorMessage = page.locator("#confirm-password-help").textContent();
            System.out.println("Error message found: " + errorMessage);
            assertThat(errorMessage).containsAnyOf("비밀번호가 일치하지 않습니다", "Passwords do not match");
        } else
            System.out.println("Error message element not found!");

        // 에러 메시지 확인 (footer 영역)
        if (page.locator("article footer").count() > 0)
            assertThat(page.locator("article footer del").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("✅ 중복 이메일/사용자명 검증")
    void shouldValidateDuplicateEmailOrUsername() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 이미 존재하는 admin 계정 정보로 회원가입 시도
        page.fill("input[name='email']", "admin@example.com");
        page.fill("input[name='name']", "관리자");
        page.fill("input[name='username']", "admin");
        page.fill("input[name='password']", "Test123!");
        page.fill("input[name='confirmPassword']", "Test123!");
        page.check("input[name='agree']");

        // 회원가입 시도
        page.click("button[type='submit'], input[type='submit']");
        page.waitForLoadState();

        // 중복으로 인해 회원가입 페이지에 머물러 있는지 확인
        assertThat(page.url()).contains("/auth/signup");

        // 에러 메시지 확인
        if (page.locator("article footer del").count() > 0) {
            String errorMessage = page.textContent("article footer del");
            assertThat(errorMessage).containsAnyOf("이미 존재", "중복", "사용 중");
        }
    }

    @Test
    @DisplayName("✅ 약관 동의 체크박스 검증")
    void shouldRequireAgreementCheckbox() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 약관 동의 없이 모든 필드 입력
        page.fill("input[name='email']", "test@example.com");
        page.fill("input[name='name']", "테스트사용자");
        page.fill("input[name='username']", "testuser123");
        page.fill("input[name='password']", "Test123!");
        page.fill("input[name='confirmPassword']", "Test123!");
        // 약관 동의 체크박스는 체크하지 않음

        // 회원가입 시도
        page.click("button[type='submit'], input[type='submit']");

        // HTML5 validation에 의해 제출이 막혔는지 확인
        assertThat(page.url()).contains("/auth/signup");
    }

    @Test
    @DisplayName("✅ 비밀번호 복잡성 검증")
    void shouldValidatePasswordComplexity() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 복잡성이 부족한 비밀번호로 시도
        page.fill("input[name='email']", "test@example.com");
        page.fill("input[name='name']", "테스트사용자");
        page.fill("input[name='username']", "testuser123");
        page.fill("input[name='password']", "simple"); // 복잡성 부족
        page.fill("input[name='confirmPassword']", "simple");
        page.check("input[name='agree']");

        // 회원가입 시도
        page.click("button[type='submit'], input[type='submit']");

        // HTML5 pattern validation에 의해 막혔는지 확인
        assertThat(page.url()).contains("/auth/signup");
    }

    @Test
    @DisplayName("✅ 로그인 페이지로 이동 링크 검증")
    void shouldNavigateToLoginPage() {
        page.navigate("http://localhost:" + port + "/auth/signup");
        page.waitForLoadState();

        // 로그인 링크 클릭
        page.click("a[href='/auth/login']");
        page.waitForLoadState();

        // 로그인 페이지로 이동했는지 확인
        assertThat(page.url()).contains("/auth/login");
        assertThat(page.textContent("h1")).contains("로그인");
    }

}
