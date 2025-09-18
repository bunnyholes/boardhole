package bunny.boardhole.auth.e2e;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.e2e.E2ETestBase;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Auth E2E 테스트")
@Tag("e2e")
@Tag("auth")
class AuthE2ETest extends E2ETestBase {

    private static String testUsername;
    private static String testPassword;
    private static String testEmail;

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Signup {

        static Stream<Arguments> provideInvalidSignupData() {
            return Stream.of(Arguments.of("username 누락", "", "Password123!", "Test User", "test@example.com"),
                    Arguments.of("password 누락", "testuser", "", "Test User", "test@example.com"),
                    Arguments.of("name 누락", "testuser", "Password123!", "", "test@example.com"),
                    Arguments.of("email 누락", "testuser", "Password123!", "Test User", ""),
                    Arguments.of("잘못된 이메일 형식", "testuser", "Password123!", "Test User", "invalid-email"));
        }

        @BeforeEach
        void setupTestData() {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            AuthE2ETest.testUsername = "test_" + uniqueId;
            AuthE2ETest.testPassword = "Password123!";
            AuthE2ETest.testEmail = "test_" + uniqueId + "@example.com";
        }

        @Test
        @Order(1)
        @DisplayName("✅ 유효한 데이터로 회원가입 성공")
        void shouldCreateUserWithValidData() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", AuthE2ETest.testUsername)
                    .formParam("password", AuthE2ETest.testPassword)
                    .formParam("confirmPassword", AuthE2ETest.testPassword)
                    .formParam("name", "Test User")
                    .formParam("email", AuthE2ETest.testEmail)
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);
        }

        @Test
        @Order(2)
        @DisplayName("❌ 중복된 사용자명으로 회원가입 실패 → 409 Conflict")
        void shouldFailWhenUsernameDuplicated() {
            // First signup - should succeed
            String duplicateUsername = "dup_" + UUID.randomUUID().toString().substring(0, 8);
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", duplicateUsername)
                    .formParam("password", "Password123!")
                    .formParam("confirmPassword", "Password123!")
                    .formParam("name", "First User")
                    .formParam("email", "first_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Second signup with same username - should fail
            given().contentType(ContentType.URLENC)
                   .formParam("username", duplicateUsername)
                   .formParam("password", "Password456!")
                   .formParam("confirmPassword", "Password456!")
                   .formParam("name", "Second User")
                   .formParam("email", "second_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                   .when()
                   .post("/api/auth/signup")
                   .then()
                   .statusCode(409)
                   .body("type", equalTo("urn:problem-type:duplicate-username"))
                   .body("title", equalTo(MessageUtils.get("exception.title.duplicate-username")))
                   .body("detail", equalTo(MessageUtils.get("error.user.username.already-exists")))
                   .body("status", equalTo(409))
                   .body("code", equalTo("USER_DUPLICATE_USERNAME"))
                   .body("path", equalTo("/api/auth/signup"))
                   .body("method", equalTo("POST"))
                   .body("timestamp", notNullValue())
                   .body("instance", equalTo("/api/auth/signup"));
        }

        @Test
        @Order(3)
        @DisplayName("❌ 중복된 이메일로 회원가입 실패 → 409 Conflict")
        void shouldFailWhenEmailDuplicated() {
            String duplicateEmail = "dup_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

            // First signup - should succeed
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", "user1_" + UUID.randomUUID().toString().substring(0, 8))
                    .formParam("password", "Password123!")
                    .formParam("confirmPassword", "Password123!")
                    .formParam("name", "First User")
                    .formParam("email", duplicateEmail)
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Second signup with same email - should fail
            given().contentType(ContentType.URLENC)
                   .formParam("username", "user2_" + UUID.randomUUID().toString().substring(0, 8))
                   .formParam("password", "Password456!")
                   .formParam("confirmPassword", "Password456!")
                   .formParam("name", "Second User")
                   .formParam("email", duplicateEmail)
                   .when()
                   .post("/api/auth/signup")
                   .then()
                   .statusCode(409)
                   .body("type", equalTo("urn:problem-type:duplicate-email"))
                   .body("title", equalTo(MessageUtils.get("exception.title.duplicate-email")))
                   .body("detail", equalTo(MessageUtils.get("error.user.email.already-exists")))
                   .body("status", equalTo(409))
                   .body("code", equalTo("USER_DUPLICATE_EMAIL"))
                   .body("path", equalTo("/api/auth/signup"))
                   .body("method", equalTo("POST"))
                   .body("timestamp", notNullValue())
                   .body("instance", equalTo("/api/auth/signup"));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("provideInvalidSignupData")
        @Order(4)
        @DisplayName("❌ 유효성 검증 실패 → 422 Unprocessable Content")
        void shouldFailWhenValidationFails(String description, String username, String password, String name, String email) {
            given().contentType(ContentType.URLENC)
                   .formParam("username", username)
                   .formParam("password", password)
                   .formParam("confirmPassword", password)
                   .formParam("name", name)
                   .formParam("email", email)
                   .when().post("/api/auth/signup")
                   .then().statusCode(422)
                   .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                   .body("type", notNullValue())
                   .body("status", equalTo(422))
                   .body("code", equalTo("VALIDATION_ERROR"))
                   .body("errors", notNullValue());
        }

        @Test
        @Order(5)
        @DisplayName("❌ 비밀번호 확인 불일치 → 422 Unprocessable Content")
        void shouldFailWhenPasswordsDontMatch() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", "test_" + UUID.randomUUID().toString().substring(0, 8))
                    .formParam("password", "Password123!")
                    .formParam("confirmPassword", "DifferentPassword123!")
                    .formParam("name", "Test User")
                    .formParam("email", "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                    .body("status", equalTo(422))
                    .body("code", equalTo("VALIDATION_ERROR"))
                    .body("errors", notNullValue());
        }

        @Test
        @Order(6)
        @DisplayName("❌ 약한 비밀번호 → 422 Unprocessable Content")
        void shouldFailWithWeakPassword() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", "test_" + UUID.randomUUID().toString().substring(0, 8))
                    .formParam("password", "weak")
                    .formParam("confirmPassword", "weak")
                    .formParam("name", "Test User")
                    .formParam("email", "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                    .body("status", equalTo(422))
                    .body("code", equalTo("VALIDATION_ERROR"))
                    .body("errors", notNullValue());
        }

        @Test
        @Order(7)
        @DisplayName("❌ 너무 긴 사용자명 → 422 Unprocessable Content")
        void shouldFailWithTooLongUsername() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", "a".repeat(51))  // Assuming max length is 50
                    .formParam("password", "Password123!")
                    .formParam("confirmPassword", "Password123!")
                    .formParam("name", "Test User")
                    .formParam("email", "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                    .body("status", equalTo(422))
                    .body("code", equalTo("VALIDATION_ERROR"))
                    .body("errors", notNullValue());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Login {

        private String loginUsername;
        private String loginPassword;

        @BeforeEach
        void createUserForLogin() {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            loginUsername = "login_" + uniqueId;
            loginPassword = "Password123!";

            // Create user for login tests
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", loginUsername)
                    .formParam("password", loginPassword)
                    .formParam("confirmPassword", loginPassword)
                    .formParam("name", "Login Test User")
                    .formParam("email", "login_" + uniqueId + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);
        }

        @Test
        @Order(1)
        @DisplayName("✅ 유효한 자격증명으로 로그인 성공")
        void shouldLoginWithValidCredentials() {
            ValidatableResponse response = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", loginUsername)
                    .formParam("password", loginPassword)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204);

            // Verify session cookie is set (JSESSIONID for test environment without Redis)
            response.cookie("JSESSIONID", notNullValue());
        }

        @Test
        @Order(2)
        @DisplayName("❌ 잘못된 비밀번호로 로그인 실패 → 401 Unauthorized")
        void shouldFailWithWrongPassword() {
            given().contentType(ContentType.URLENC)
                   .formParam("username", loginUsername)
                   .formParam("password", "WrongPassword123!")
                   .when()
                   .post("/api/auth/login")
                   .then()
                   .statusCode(401)
                   .body("type", equalTo("urn:problem-type:unauthorized"))
                   .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                   .body("status", equalTo(401))
                   .body("code", equalTo("UNAUTHORIZED"))
                   .body("path", equalTo("/api/auth/login"))
                   .body("method", equalTo("POST"))
                   .body("timestamp", notNullValue())
                   .body("instance", equalTo("/api/auth/login"));
        }

        @Test
        @Order(3)
        @DisplayName("❌ 존재하지 않는 사용자로 로그인 실패 → 401 Unauthorized")
        void shouldFailWithNonExistentUser() {
            given().contentType(ContentType.URLENC)
                   .formParam("username", "nonexistent_" + UUID.randomUUID().toString().substring(0, 8))
                   .formParam("password", "AnyPassword123!")
                   .when()
                   .post("/api/auth/login")
                   .then()
                   .statusCode(401)
                   .body("type", equalTo("urn:problem-type:unauthorized"))
                   .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                   .body("status", equalTo(401))
                   .body("code", equalTo("UNAUTHORIZED"))
                   .body("path", equalTo("/api/auth/login"))
                   .body("method", equalTo("POST"))
                   .body("timestamp", notNullValue())
                   .body("instance", equalTo("/api/auth/login"));
        }

        @Test
        @Order(4)
        @DisplayName("❌ 빈 사용자명으로 로그인 시도 → 422 Unprocessable Content")
        void shouldFailWithEmptyUsername() {
            given().contentType(ContentType.URLENC)
                   .formParam("username", "")
                   .formParam("password", "Password123!")
                   .when()
                   .post("/api/auth/login")
                   .then()
                   .statusCode(422)
                   .body("type", equalTo("urn:problem-type:validation-error"))
                   .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                   .body("status", equalTo(422))
                   .body("code", equalTo("VALIDATION_ERROR"))
                   .body("errors", notNullValue());
        }

        @Test
        @Order(5)
        @DisplayName("❌ 빈 비밀번호로 로그인 시도 → 422 Unprocessable Content")
        void shouldFailWithEmptyPassword() {
            given().contentType(ContentType.URLENC)
                   .formParam("username", loginUsername)
                   .formParam("password", "")
                   .when()
                   .post("/api/auth/login")
                   .then()
                   .statusCode(422)
                   .body("type", equalTo("urn:problem-type:validation-error"))
                   .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                   .body("status", equalTo(422))
                   .body("code", equalTo("VALIDATION_ERROR"))
                   .body("errors", notNullValue());
        }

        @Test
        @Order(6)
        @DisplayName("❌ SQL Injection 시도 → 422 또는 401 (보안)")
        void shouldBeSecureAgainstSQLInjection() {
            // SQL Injection 시도는 유효성 검증(422) 또는 인증 실패(401)로 막힘
            ValidatableResponse response = given().contentType(ContentType.URLENC)
                                                  .formParam("username", "' OR '1'='1")
                                                  .formParam("password", "' OR '1'='1")
                                                  .when()
                                                  .post("/api/auth/login")
                                                  .then();

            // 422(Validation Error) 또는 401(Unauthorized) 둘 다 보안상 성공
            int statusCode = response.extract().statusCode();
            org.assertj.core.api.Assertions.assertThat(statusCode)
                                           .withFailMessage("SQL Injection should be blocked with 422 or 401, but got " + statusCode)
                                           .isIn(422, 401);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - 로그아웃")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Logout {

        private String sessionCookie;

        @BeforeEach
        void loginBeforeLogout() {
            // Create and login a user
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String username = "logout_" + uniqueId;
            final String password = "Password123!";

            // Signup
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .formParam("confirmPassword", password)
                    .formParam("name", "Logout Test User")
                    .formParam("email", "logout_" + uniqueId + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Login and get session cookie (JSESSIONID in test profile)
            sessionCookie = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");
        }

        @Test
        @Order(1)
        @DisplayName("✅ 로그인된 사용자 로그아웃 성공")
        void shouldLogoutWhenAuthenticated() {
            given().cookie("JSESSIONID", sessionCookie).when().post("/api/auth/logout").then().statusCode(204);

            // Verify session is invalidated by trying to access protected endpoint
            given().cookie("JSESSIONID", sessionCookie).when().get("/api/auth/user-access").then().statusCode(401);
        }

        @Test
        @Order(2)
        @DisplayName("❌ 인증되지 않은 상태에서 로그아웃 → 401 Unauthorized")
        void shouldFailWhenNotAuthenticated() {
            given()
                    .when()
                    .post("/api/auth/logout")
                    .then()
                    .statusCode(401)
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }
    }

    @Nested
    @DisplayName("권한별 접근 제어")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AccessControl {

        private String userSessionCookie;
        private String adminSessionCookie;

        @BeforeEach
        void setupUsersWithDifferentRoles() {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            // Create regular user
            String regularUsername = "user_" + uniqueId;
            final String regularPassword = "Password123!";
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", regularUsername)
                    .formParam("password", regularPassword)
                    .formParam("confirmPassword", regularPassword)
                    .formParam("name", "Regular User")
                    .formParam("email", "user_" + uniqueId + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Login as regular user
            userSessionCookie = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", regularUsername)
                    .formParam("password", regularPassword)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");

            // Note: Admin user creation would typically be done through database seeding
            // or special admin creation endpoint. For this test, we'll assume an admin
            // user already exists in the test database with username "admin" and known password

            // Try to login as admin (assuming test data includes an admin user)
            ValidatableResponse adminLoginResponse = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", "admin")
                    .formParam("password", "Admin123!")
                    .when()
                    .post("/api/auth/login")
                    .then();

            // If admin doesn't exist, create one (this would typically be handled differently)
            // For testing purposes, we'll skip admin-specific tests if admin doesn't exist
            if (adminLoginResponse.extract().statusCode() != 204)
                adminSessionCookie = null;
            else
                adminSessionCookie = adminLoginResponse.extract().cookie("JSESSIONID");
        }

        @Test
        @Order(1)
        @DisplayName("✅ 공개 엔드포인트 - 인증 없이 접근 가능")
        void shouldAllowPublicAccess() {
            given().when().get("/api/auth/public-access").then().statusCode(204);
        }

        @Test
        @Order(2)
        @DisplayName("❌ 사용자 전용 엔드포인트 - 인증 없이 접근 실패 → 401")
        void shouldDenyUserAccessWithoutAuth() {
            given().when().get("/api/auth/user-access").then().statusCode(401)
                   .body("type", equalTo("urn:problem-type:unauthorized"))
                   .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                   .body("status", equalTo(401))
                   .body("code", equalTo("UNAUTHORIZED"))
                   .body("path", equalTo("/api/auth/user-access"))
                   .body("method", equalTo("GET"))
                   .body("timestamp", notNullValue())
                   .body("instance", equalTo("/api/auth/user-access"));
        }

        @Test
        @Order(3)
        @DisplayName("✅ 사용자 전용 엔드포인트 - 일반 사용자 접근 성공")
        void shouldAllowUserAccessWithUserRole() {
            given().cookie("JSESSIONID", userSessionCookie).when().get("/api/auth/user-access").then().statusCode(204);
        }

        @Test
        @Order(4)
        @DisplayName("✅ 사용자 전용 엔드포인트 - 관리자 접근 성공")
        void shouldAllowUserAccessWithAdminRole() {
            // Skip if admin session is not available
            if (adminSessionCookie == null)
                return;

            given().cookie("JSESSIONID", adminSessionCookie).when().get("/api/auth/user-access").then().statusCode(204);
        }

        @Test
        @Order(5)
        @DisplayName("❌ 관리자 전용 엔드포인트 - 인증 없이 접근 → 401")
        void shouldDenyAdminAccessWithoutAuth() {
            given()
                    .when()
                    .get("/api/auth/admin-only")
                    .then()
                    .statusCode(401)
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @Order(6)
        @DisplayName("❌ 관리자 전용 엔드포인트 - 일반 사용자 접근 → 403")
        void shouldDenyAdminAccessWithUserRole() {
            given()
                    .cookie("JSESSIONID", userSessionCookie)
                    .when()
                    .get("/api/auth/admin-only")
                    .then()
                    .statusCode(403)
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @Order(7)
        @DisplayName("✅ 관리자 전용 엔드포인트 - 관리자 접근 성공")
        void shouldAllowAdminAccessWithAdminRole() {
            // Skip if admin session is not available
            if (adminSessionCookie == null)
                return;

            given().cookie("JSESSIONID", adminSessionCookie).when().get("/api/auth/admin-only").then().statusCode(204);
        }
    }

    @Nested
    @DisplayName("세션 관리")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SessionManagement {

        @Test
        @Order(1)
        @DisplayName("✅ 로그인 후 세션 쿠키로 인증 유지")
        void shouldMaintainAuthenticationWithSessionCookie() {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String username = "session_" + uniqueId;
            final String password = "Password123!";

            // Signup
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .formParam("confirmPassword", password)
                    .formParam("name", "Session Test User")
                    .formParam("email", "session_" + uniqueId + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Login and get session
            String sessionCookie = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");

            // Use session for multiple requests
            given().cookie("JSESSIONID", sessionCookie).when().get("/api/auth/user-access").then().statusCode(204);

            given().cookie("JSESSIONID", sessionCookie).when().get("/api/auth/user-access").then().statusCode(204);
        }

        @Test
        @Order(2)
        @DisplayName("❌ 잘못된 세션 쿠키로 접근 실패")
        void shouldFailWithInvalidSessionCookie() {
            given().cookie("JSESSIONID", "invalid-session-id")
                   .when()
                   .get("/api/auth/user-access")
                   .then()
                   .statusCode(401)
                   .body("type", equalTo("urn:problem-type:unauthorized"))
                   .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                   .body("code", equalTo("UNAUTHORIZED"))
                   .body("path", equalTo("/api/auth/user-access"))
                   .body("method", equalTo("GET"))
                   .body("timestamp", notNullValue())
                   .body("instance", equalTo("/api/auth/user-access"));
        }

        @Test
        @Order(3)
        @DisplayName("❌ 만료된 세션으로 접근 시도")
        void shouldFailWithExpiredSession() {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String username = "expired_" + uniqueId;
            final String password = "Password123!";

            // Signup
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .formParam("confirmPassword", password)
                    .formParam("name", "Expired Test User")
                    .formParam("email", "expired_" + uniqueId + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Login
            String sessionCookie = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");

            // Logout to invalidate session
            given().cookie("JSESSIONID", sessionCookie)
                   .when()
                   .post("/api/auth/logout")
                   .then()
                   .statusCode(204);

            // Try to use expired session
            given().cookie("JSESSIONID", sessionCookie)
                   .when()
                   .get("/api/auth/user-access")
                   .then()
                   .statusCode(401)
                   .body("type", equalTo("urn:problem-type:unauthorized"))
                   .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                   .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @Order(4)
        @DisplayName("✅ 여러 세션 동시 관리")
        void shouldHandleMultipleSessions() {
            // Create two users
            String uniqueId1 = UUID.randomUUID().toString().substring(0, 8);
            String uniqueId2 = UUID.randomUUID().toString().substring(0, 8);
            String username1 = "multi1_" + uniqueId1;
            String username2 = "multi2_" + uniqueId2;
            final String password = "Password123!";

            // Signup first user
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username1)
                    .formParam("password", password)
                    .formParam("confirmPassword", password)
                    .formParam("name", "Multi User 1")
                    .formParam("email", "multi1_" + uniqueId1 + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Signup second user
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username2)
                    .formParam("password", password)
                    .formParam("confirmPassword", password)
                    .formParam("name", "Multi User 2")
                    .formParam("email", "multi2_" + uniqueId2 + "@example.com")
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Login both users
            String session1 = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username1)
                    .formParam("password", password)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");

            String session2 = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username2)
                    .formParam("password", password)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");

            // Both sessions should work
            given().cookie("JSESSIONID", session1)
                   .when()
                   .get("/api/auth/user-access")
                   .then()
                   .statusCode(204);

            given().cookie("JSESSIONID", session2)
                   .when()
                   .get("/api/auth/user-access")
                   .then()
                   .statusCode(204);
        }
    }

    @Nested
    @DisplayName("완전한 인증 플로우")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CompleteAuthFlow {

        @Test
        @Order(1)
        @DisplayName("✅ 회원가입 → 로그인 → 인증된 요청 → 로그아웃 전체 플로우")
        void shouldCompleteFullAuthenticationFlow() {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String username = "flow_" + uniqueId;
            final String password = "Password123!";
            String email = "flow_" + uniqueId + "@example.com";

            // Step 1: Signup
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .formParam("confirmPassword", password)
                    .formParam("name", "Flow Test User")
                    .formParam("email", email)
                    .when()
                    .post("/api/auth/signup")
                    .then()
                    .statusCode(204);

            // Step 2: Login
            String sessionCookie = given()
                    .contentType(ContentType.URLENC)
                    .formParam("username", username)
                    .formParam("password", password)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(204)
                    .extract()
                    .cookie("JSESSIONID");

            // Step 3: Access protected endpoint with session
            given().cookie("JSESSIONID", sessionCookie).when().get("/api/auth/user-access").then().statusCode(204);

            // Step 4: Logout
            given().cookie("JSESSIONID", sessionCookie).when().post("/api/auth/logout").then().statusCode(204);

            // Step 5: Verify session is invalidated
            given().cookie("JSESSIONID", sessionCookie).when().get("/api/auth/user-access").then().statusCode(401);
        }
    }
}
