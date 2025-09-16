package bunny.boardhole.web.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import bunny.boardhole.testsupport.e2e.E2ETestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Web E2E 테스트")
@Tag("e2e")
@Tag("web")
class WebE2ETest extends E2ETestBase {

    @BeforeAll
    void setupWebTestBasePath() {
        // 웹 페이지 테스트를 위해 basePath를 변경
        RestAssured.basePath = "";
    }

    @Nested
    @DisplayName("공개 페이지 접근 테스트")
    class PublicPageAccess {

        @Test
        @DisplayName("루트 경로(/) - 비인증 사용자는 로그인으로 리다이렉트")
        void indexPage_Unauthenticated_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }

        @Test
        @DisplayName("/index 경로 - 비인증 사용자는 로그인으로 리다이렉트")
        void indexPageWithPath_Unauthenticated_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/index")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }

        @Test
        @DisplayName("로그인 페이지(/login) HTTP 접근 테스트")
        void loginPage_HttpAccess_ShouldReturn200() {
            given()
                .when()
                    .get("/login")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML)
                    .body(containsString("로그인"));
        }

        @Test
        @DisplayName("회원가입 페이지(/signup) HTTP 접근 테스트")
        void signupPage_HttpAccess_ShouldReturn200() {
            given()
                .when()
                    .get("/signup")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML)
                    .body(containsString("회원가입"));
        }

        @Test
        @DisplayName("게시판 목록(/boards) - 비인증 사용자는 로그인으로 리다이렉트")
        void boardsPage_Unauthenticated_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/boards")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }
    }

    @Nested
    @DisplayName("인증이 필요한 페이지 접근 테스트")
    class AuthenticatedPageAccess {

        @Test
        @DisplayName("마이페이지(/mypage) - 인증 없이 접근 시 리다이렉트")
        void mypagePage_WithoutAuth_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/mypage")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }

        @Test
        @DisplayName("게시글 작성(/boards/write) - 인증 없이 접근 시 리다이렉트")
        void boardWritePage_WithoutAuth_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/boards/write")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }

        @Test
        @DisplayName("사용자 관리(/users) - 인증 없이 접근 시 리다이렉트")
        void usersPage_WithoutAuth_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/users")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }

        @Test
        @DisplayName("게시글 수정(/boards/1/edit) - 인증 없이 접근 시 리다이렉트")
        void boardEditPage_WithoutAuth_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/boards/1/edit")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }
    }

    @Nested
    @DisplayName("정적 리소스 접근 테스트")
    class StaticResourceAccess {

        @Test
        @DisplayName("favicon.ico 접근 테스트")
        void favicon_ShouldReturn200() {
            given()
                .when()
                    .get("/favicon.ico")
                .then()
                    .statusCode(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("존재하지 않는 정적 리소스 접근 시 404")
        void nonExistentStaticResource_ShouldReturn404() {
            given()
                .when()
                    .get("/nonexistent.css")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("타임리프 뷰 렌더링 검증")
    class ThymeleafViewRendering {

        @Test
        @DisplayName("비인증 사용자 홈페이지 접근 시 리다이렉트")
        void unauthenticatedHomePage_ShouldRedirectToLogin() {
            given()
                .redirects().follow(false)
            .when()
                .get("/")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }

        @Test
        @DisplayName("로그인 페이지 타임리프 렌더링 검증")
        void loginPage_ShouldRenderThymeleafCorrectly() {
            given()
                .when()
                    .get("/login")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML)
                    .body(containsString("Board Hole"))
                    .body(containsString("form"));
        }

        @Test
        @DisplayName("회원가입 페이지 타임리프 렌더링 검증")
        void signupPage_ShouldRenderThymeleafCorrectly() {
            given()
                .when()
                    .get("/signup")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.HTML)
                    .body(containsString("Board Hole"))
                    .body(containsString("form"));
        }

        @Test
        @DisplayName("게시판 목록 페이지 - 비인증은 리다이렉트")
        void boardsPage_Unauthenticated_ShouldRedirect() {
            given()
                .redirects().follow(false)
            .when()
                .get("/boards")
            .then()
                .statusCode(HttpStatus.FOUND.value());
        }
    }

    @Nested
    @DisplayName("CORS 및 보안 헤더 테스트")
    class SecurityHeaders {

        @Test
        @DisplayName("CORS 헤더 확인")
        void corsHeaders_ShouldBePresent() {
            given()
                .header("Origin", "http://localhost:3000")
                .when()
                    .get("/")
                .then()
                    .statusCode(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("보안 헤더 확인")
        void securityHeaders_ShouldBePresent() {
            given()
                .when()
                    .get("/")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .header("X-Content-Type-Options", notNullValue())
                    .header("X-Frame-Options", notNullValue());
        }
    }
}
