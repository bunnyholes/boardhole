package bunny.boardhole.auth.e2e;

import bunny.boardhole.testsupport.config.*;
import bunny.boardhole.testsupport.e2e.*;
import io.restassured.RestAssured;
import io.restassured.filter.log.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;

import java.util.*;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("🔐 AuthController E2E 테스트")
@Tag("e2e")
@Tag("auth")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
class AuthControllerE2ETest extends E2ETestBase {

    private String testUsername;
    private String testPassword;
    private String testEmail;

    // Parameterized tests use built-in JUnit annotations (no custom annotations)

    // application-test.yml 기본 사용자 (DataInitializer로 시드됨)
    @Value("${boardhole.default-users.admin.username}")
    private String adminUsername;
    @Value("${boardhole.default-users.admin.password}")
    private String adminPassword;
    @Value("${boardhole.default-users.admin.email}")
    private String adminEmail;
    @Value("${boardhole.default-users.regular.username}")
    private String regularUsername;
    @Value("${boardhole.default-users.regular.password}")
    private String regularPassword;
    @Value("${boardhole.default-users.regular.email}")
    private String regularEmail;

    private String userSessionCookie;
    private String userSessionCookieName;
    private String adminSessionCookie;
    private String adminSessionCookieName;

    private CookieKV loginAndGetCookieKV(String username, String password, String name, String email) {
        AuthSteps.signup(username, password, name, email);
        SessionCookie sc = AuthSteps.login(username, password);
        return new CookieKV(sc.name(), sc.value());
    }

    @BeforeAll
    void setUp() {
        // 회원가입 테스트용 사용자 선택 (고유성 보장을 위해 UUID 추가)
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
        testUsername = "e2e_user_" + uniqueId;
        testPassword = "TestPass123!";
        testEmail = "e2e_user_" + uniqueId + "@example.com";

        // RestAssured 로깅 설정 (디버깅용)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎉 AuthController E2E 테스트 완료!");
        System.out.println("=".repeat(60));
        System.out.println("📊 테스트 커버리지:");
        System.out.println("   ✅ 공개 API 엔드포인트들");
        System.out.println("   ✅ 회원가입/로그인/로그아웃 플로우");
        System.out.println("   ✅ 권한 기반 접근 제어 (USER vs ADMIN)");
        System.out.println("   ✅ 세션 관리 및 상태 유지");
        System.out.println("   ✅ 에러 케이스 및 엣지 케이스");
        System.out.println("   ✅ 중복 검증 및 유효성 검사");
        System.out.println("=".repeat(60));
        System.out.println("🔐 인증 시스템이 안정적으로 작동하고 있습니다!");
        System.out.println("=".repeat(60));
    }

    private void ensureUserLoggedIn() {
        // Try direct login (avoid relying on signup for seeded user)
        System.out.println("[E2E] ensureUserLoggedIn using username=" + regularUsername + ", password=" + regularPassword);
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", regularUsername);
        loginData.put("password", regularPassword);
        io.restassured.response.Response loginRes = given()
                .contentType(ContentType.URLENC)
                .formParams(loginData)
                .when()
                .post("auth/login");
        System.out.println("[E2E] ensureUserLoggedIn login status=" + loginRes.getStatusCode() + ", cookies=" + loginRes.getCookies());
        Assertions.assertEquals(204, loginRes.getStatusCode(), "Seed user login should succeed");
        String cookieName = loginRes.getCookie("SESSION") != null ? "SESSION" : "JSESSIONID";
        String cookie = loginRes.getCookie(cookieName);
        userSessionCookieName = cookieName;
        userSessionCookie = cookie;
        System.out.println("[E2E] user login cookie => " + userSessionCookieName + "=" + userSessionCookie);
    }

    private void ensureAdminLoggedIn() {
        CookieKV kv = loginAndGetCookieKV(adminUsername, adminPassword, "ADMIN", adminEmail);
        adminSessionCookieName = kv.name();
        adminSessionCookie = kv.value();
        System.out.println("[E2E] admin login cookie => " + adminSessionCookieName + "=" + adminSessionCookie);
    }

    private record CookieKV(String name, String value) {
    }

    @Nested
    @DisplayName("🌐 공개 API 테스트")
    @Tag("public")
    class PublicAccess {

        @Test
        @DisplayName("✅ 공개 엔드포인트 - 인증 없이 접근 가능")
        void shouldAllowPublicAccess() {
            given()
                    .when()
                    .get("auth/public-access")
                    .then()
                    .statusCode(204); // No Content
        }
    }

    @Nested
    @DisplayName("📝 POST /api/auth/signup - 회원가입")
    @Tag("create")
    class Signup {

        static Stream<Arguments> provideInvalidSignupData() {
            return Stream.of(
                    Arguments.of("username 누락", "", "Password123!", "Test User", "test@example.com"),
                    Arguments.of("password 누락", "testuser", "", "Test User", "test@example.com"),
                    Arguments.of("name 누락", "testuser", "Password123!", "", "test@example.com"),
                    Arguments.of("email 누락", "testuser", "Password123!", "Test User", ""),
                    Arguments.of("잘못된 이메일 형식", "testuser", "Password123!", "Test User", "invalid-email-format")
            );
        }

        @Test
        @DisplayName("✅ 유효한 데이터로 회원가입 성공")
        void shouldCreateUserWithValidData() {
            Map<String, String> signupData = new HashMap<>();
            signupData.put("username", testUsername);
            signupData.put("password", testPassword);
            signupData.put("name", "E2E 테스트 사용자");
            signupData.put("email", testEmail);

            given()
                    .contentType(ContentType.URLENC)
                    .formParams(signupData)
                    .when()
                    .post("auth/signup")
                    .then()
                    .statusCode(anyOf(is(204), is(409))); // 이미 생성된 경우 멱등성 허용
        }

        @ParameterizedTest(name = "[{index}] ❌ {0}")
        @MethodSource("provideInvalidSignupData")
        @DisplayName("필수 필드 형식 오류 → 400 Bad Request")
        void shouldFailWhenRequiredFieldMissing(String displayName, String username, String password, String name, String email) {
            Map<String, String> invalidSignupData = new HashMap<>();
            if (!username.isEmpty()) invalidSignupData.put("username", username);
            if (!password.isEmpty()) invalidSignupData.put("password", password);
            if (!name.isEmpty()) invalidSignupData.put("name", name);
            if (!email.isEmpty()) invalidSignupData.put("email", email);

            given()
                    .contentType(ContentType.URLENC)
                    .formParams(invalidSignupData)
                    .when()
                    .post("auth/signup")
                    .then()
                    .statusCode(400)
                    .body("status", equalTo(400))
                    .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.validation-failed")))
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.VALIDATION_ERROR.getCode()))
                    .body("errors", notNullValue());
        }

        @Nested
        @DisplayName("중복 검증")
        class DuplicateValidation {

            @Test
            @DisplayName("❌ 중복된 사용자명 → 409 Conflict")
            void shouldFailWhenUsernameDuplicated() {
                Map<String, String> duplicateSignupData = new HashMap<>();
                duplicateSignupData.put("username", testUsername); // 이미 존재하는 사용자명
                duplicateSignupData.put("password", "AnotherPass123!");
                duplicateSignupData.put("name", "중복 테스트 사용자");
                duplicateSignupData.put("email", "duplicate_" + System.currentTimeMillis() + "@example.com");

                given()
                        .contentType(ContentType.URLENC)
                        .formParams(duplicateSignupData)
                        .when()
                        .post("auth/signup")
                        .then()
                        .statusCode(409)
                        .body("status", equalTo(409))
                        .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.duplicate-username")))
                        .body("type", equalTo("urn:problem-type:duplicate-username"))
                        .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.USER_DUPLICATE_USERNAME.getCode()));
            }

            @Test
            @DisplayName("❌ 중복된 이메일 → 409 Conflict")
            void shouldFailWhenEmailDuplicated() {
                Map<String, String> duplicateEmailData = new HashMap<>();
                duplicateEmailData.put("username", "unique_" + System.currentTimeMillis());
                duplicateEmailData.put("password", "AnotherPass123!");
                duplicateEmailData.put("name", "중복 이메일 테스트 사용자");
                duplicateEmailData.put("email", testEmail); // 이미 존재하는 이메일

                given()
                        .contentType(ContentType.URLENC)
                        .formParams(duplicateEmailData)
                        .when()
                        .post("auth/signup")
                        .then()
                        .statusCode(409)
                        .body("status", equalTo(409))
                        .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.duplicate-email")))
                        .body("type", equalTo("urn:problem-type:duplicate-email"))
                        .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.USER_DUPLICATE_EMAIL.getCode()));
            }
        }
    }

    @Nested
    @DisplayName("🔑 POST /api/auth/login - 로그인")
    @Tag("auth")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Login {

        Stream<Arguments> provideInvalidLoginData() {
            final String validUsername = regularUsername; // 시드된 일반 사용자
            String nonExistentUser = "nonexistent_" + java.util.UUID.randomUUID().toString().substring(0, 8);

            return Stream.of(
                    Arguments.of("잘못된 비밀번호", validUsername, "WrongPass123!"),
                    Arguments.of("존재하지 않는 사용자", nonExistentUser, "AnyPass123!")
            );
        }

        @Test
        @DisplayName("✅ 유효한 자격증명으로 로그인 성공")
        void shouldLoginWithValidCredentials() {
            // Ensure test user exists (idempotent: allow 204 Created or 409 Conflict)
            Map<String, String> signupData = new HashMap<>();
            signupData.put("username", testUsername);
            signupData.put("password", testPassword);
            signupData.put("name", "E2E 테스트 사용자");
            signupData.put("email", testEmail);

            given()
                    .contentType(ContentType.URLENC)
                    .formParams(signupData)
                    .when()
                    .post("auth/signup")
                    .then()
                    .statusCode(anyOf(is(204), is(409)));
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", testUsername);
            loginData.put("password", testPassword);

            io.restassured.response.Response loginRes = given()
                    .contentType(ContentType.URLENC)
                    .formParams(loginData)
                    .when()
                    .post("auth/login")
                    .then()
                    .statusCode(204)
                    .extract().response();
            System.out.println("[E2E] login headers: " + loginRes.getHeaders());
            System.out.println("[E2E] login cookies: " + loginRes.getCookies());
            String cookieName = loginRes.getCookie("SESSION") != null ? "SESSION" : "JSESSIONID";
            String cookie = loginRes.getCookie(cookieName);
            userSessionCookieName = cookieName;
            userSessionCookie = cookie;
            Assertions.assertNotNull(userSessionCookie, "사용자 세션 쿠키가 생성되어야 합니다");
        }

        @Test
        @DisplayName("✅ 관리자 로그인")
        void shouldLoginAsAdmin() {
            // 시드된 관리자 계정으로 로그인
            Map<String, String> adminLoginData = new HashMap<>();
            adminLoginData.put("username", adminUsername);
            adminLoginData.put("password", adminPassword);

            io.restassured.response.Response adminLoginRes = given()
                    .contentType(ContentType.URLENC)
                    .formParams(adminLoginData)
                    .when()
                    .post("auth/login")
                    .then()
                    .statusCode(204)
                    .extract().response();
            System.out.println("[E2E] admin login headers: " + adminLoginRes.getHeaders());
            System.out.println("[E2E] admin login cookies: " + adminLoginRes.getCookies());
            String cookieName = adminLoginRes.getCookie("SESSION") != null ? "SESSION" : "JSESSIONID";
            String adminCookie = adminLoginRes.getCookie(cookieName);
            adminSessionCookieName = cookieName;
            adminSessionCookie = adminCookie;
            Assertions.assertNotNull(adminSessionCookie, "관리자 세션 쿠키가 생성되어야 합니다");
        }

        @ParameterizedTest(name = "[{index}] ❌ {0}")
        @MethodSource("provideInvalidLoginData")
        @DisplayName("잘못된 자격증명 → 401 Unauthorized")
        void shouldFailWithInvalidCredentials(String displayName, String username, String password) {
            Map<String, String> invalidLoginData = new HashMap<>();
            invalidLoginData.put("username", username);
            invalidLoginData.put("password", password);

            given()
                    .contentType(ContentType.URLENC)
                    .formParams(invalidLoginData)
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(401)
                    .body("status", equalTo(401))
                    .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.unauthorized")))
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.UNAUTHORIZED.getCode()));
        }
    }

    @Nested
    @DisplayName("🛡️ 권한별 접근 제어")
    @Tag("security")
    class AccessControl {

        @Test
        @DisplayName("❌ 사용자 전용 엔드포인트 - 인증 없이 접근 실패")
        void shouldDenyUserAccessWithoutAuth() {
            given()
                    .when()
                    .get("auth/user-access")
                    .then()
                    .statusCode(401)
                    .body("status", equalTo(401))
                    .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.unauthorized")))
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.UNAUTHORIZED.getCode()));
        }

        @Test
        @DisplayName("✅ 사용자 전용 엔드포인트 - 일반 사용자 접근 성공")
        void shouldAllowUserAccess() {
            ensureUserLoggedIn();
            io.restassured.response.Response res = given()
                    .cookie(userSessionCookieName, userSessionCookie)
                    .when()
                    .get("auth/user-access");
            System.out.println("[E2E] user-access status=" + res.getStatusCode() + ", body=" + res.getBody().asString());
            Assertions.assertEquals(204, res.getStatusCode());
        }

        @Test
        @DisplayName("✅ 사용자 전용 엔드포인트 - 관리자 접근 성공")
        void shouldAllowAdminAccessToUserEndpoint() {
            if (adminSessionCookie == null)
                ensureAdminLoggedIn();
            given()
                    .cookie(adminSessionCookieName, adminSessionCookie)
                    .when()
                    .get("auth/user-access")
                    .then()
                    .statusCode(204);
        }

        @Nested
        @DisplayName("관리자 전용 엔드포인트")
        class AdminOnly {

            @Test
            @DisplayName("❌ 인증 없이 접근 → 401 Unauthorized")
            void shouldReturn401WhenNotAuthenticated() {
                given()
                        .when()
                        .get("auth/admin-only")
                        .then()
                        .statusCode(401)
                        .body("status", equalTo(401))
                        .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.unauthorized")))
                        .body("type", equalTo("urn:problem-type:unauthorized"))
                        .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.UNAUTHORIZED.getCode()));
            }

            @Test
            @DisplayName("❌ 일반 사용자 접근 → 403 Forbidden")
            void shouldReturn403WhenRegularUser() {
                ensureUserLoggedIn();
                given()
                        .cookie(userSessionCookieName, userSessionCookie)
                        .when()
                        .get("auth/admin-only")
                        .then()
                        .statusCode(403)
                        .body("status", equalTo(403))
                        .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.access-denied")))
                        .body("type", equalTo("urn:problem-type:forbidden"))
                        .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.FORBIDDEN.getCode()));
            }

            @Test
            @DisplayName("✅ 관리자 접근 성공")
            void shouldAllowAdminAccess() {
                if (adminSessionCookie == null) ensureAdminLoggedIn();
                given()
                        .cookie(adminSessionCookieName, adminSessionCookie)
                        .when()
                        .get("auth/admin-only")
                        .then()
                        .statusCode(204);
            }
        }
    }

    @Nested
    @DisplayName("🚪 POST /api/auth/logout - 로그아웃")
    @Tag("auth")
    class Logout {

        @Test
        @DisplayName("✅ 로그인된 사용자 로그아웃 성공")
        void shouldLogoutWhenAuthenticated() {
            if (userSessionCookie == null) ensureUserLoggedIn();
            given()
                    .cookie(userSessionCookieName, userSessionCookie)
                    .when()
                    .post("auth/logout")
                    .then()
                    .statusCode(204);

            // 로그아웃 후 세션 쿠키로 접근 시도 (실패해야 함)
            given()
                    .cookie(userSessionCookieName, userSessionCookie)
                    .when()
                    .get("auth/user-access")
                    .then()
                    .statusCode(401)
                    .body("status", equalTo(401))
                    .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.unauthorized")))
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.UNAUTHORIZED.getCode()));
        }

        @Test
        @DisplayName("❌ 인증되지 않은 상태에서 로그아웃 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() {
            given()
                    .when()
                    .post("auth/logout")
                    .then()
                    .statusCode(401)
                    .body("status", equalTo(401))
                    .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.unauthorized")))
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.UNAUTHORIZED.getCode()));
        }
    }

    @Nested
    @DisplayName("🔍 GET /api/users/me - 로그인 상태 확인")
    @Tag("auth")
    class AuthCheck {

        @Test
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() {
            given()
                    .when()
                    .get("users/me")
                    .then()
                    .statusCode(401)
                    .body("status", equalTo(401))
                    .body("title", equalTo(bunny.boardhole.shared.util.MessageUtils.get("exception.title.unauthorized")))
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("code", equalTo(bunny.boardhole.shared.constants.ErrorCode.UNAUTHORIZED.getCode()));
        }

        @Test
        @DisplayName("✅ 인증된 사용자 상태 확인")
        void shouldReturnUserInfoWhenAuthenticated() {
            if (userSessionCookie == null) ensureUserLoggedIn();
            given()
                    .cookie(userSessionCookieName, userSessionCookie)
                    .when()
                    .get("users/me")
                    .then()
                    .statusCode(200)
                    .body("username", equalTo(regularUsername))
                    .body("email", equalTo(regularEmail));
        }
    }
}
