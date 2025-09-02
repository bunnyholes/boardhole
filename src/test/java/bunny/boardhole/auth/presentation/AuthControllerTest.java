package bunny.boardhole.auth.presentation;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("인증 API 통합 테스트")
@Tag("integration")
@Tag("auth")
class AuthControllerTest extends ControllerTestBase {

    @MockBean
    private EmailService emailService;

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
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
        void shouldCreateUserWithValidData() throws Exception {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "new_" + uniqueId)
                            .param("password", "Password123!")
                            .param("name", "New User")
                            .param("email", "newuser_" + uniqueId + "@example.com"))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("provideInvalidSignupData")
        @DisplayName("❌ 필수 필드 누락 → 400 Bad Request")
        void shouldFailWhenRequiredFieldMissing(String displayName, String username, String password, String name, String email) throws Exception {
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", username)
                            .param("password", password)
                            .param("name", name)
                            .param("email", email))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").exists())
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andDo(print());
        }

        @Nested
        @DisplayName("중복 검증")
        class DuplicateValidation {

            @Test
            @DisplayName("❌ 중복된 사용자명 → 409 Conflict")
            void shouldFailWhenUsernameDuplicated() throws Exception {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                String username = "dup_" + uniqueId;
                String email = "dup_" + uniqueId + "@example.com";

                // 첫 번째 회원가입 (성공)
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", username)
                                .param("password", "Password123!")
                                .param("name", "First User")
                                .param("email", email))
                        .andExpect(status().isNoContent());

                // 같은 사용자명으로 두 번째 회원가입 시도 (실패)
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", username)
                                .param("password", "Password456!")
                                .param("name", "Second User")
                                .param("email", "different_" + uniqueId + "@example.com"))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.code").value("USER_DUPLICATE_USERNAME"))
                        .andDo(print());
            }

            @Test
            @DisplayName("❌ 중복된 이메일 → 409 Conflict")
            void shouldFailWhenEmailDuplicated() throws Exception {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                String email = "dup_email_" + uniqueId + "@example.com";

                // 첫 번째 회원가입 (성공)
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "user1_" + uniqueId)
                                .param("password", "Password123!")
                                .param("name", "First User")
                                .param("email", email))
                        .andExpect(status().isNoContent());

                // 같은 이메일로 두 번째 회원가입 시도 (실패)
                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "user2_" + uniqueId)
                                .param("password", "Password456!")
                                .param("name", "Second User")
                                .param("email", email))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.code").value("USER_DUPLICATE_EMAIL"))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    @Tag("auth")
    class Login {

        static Stream<Arguments> provideInvalidLoginData() {
            String validUsername = "validuser";
            String nonExistentUser = "nonexistent_" + UUID.randomUUID().toString().substring(0, 8);

            return Stream.of(
                    Arguments.of("잘못된 비밀번호", validUsername, "WrongPass123!"),
                    Arguments.of("존재하지 않는 사용자", nonExistentUser, "AnyPass123!")
            );
        }

        @Test
        @DisplayName("✅ 유효한 자격증명으로 로그인 성공")
        void shouldLoginWithValidCredentials() throws Exception {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String username = "usr_" + uniqueId;
            String password = "Password123!";

            // 먼저 회원가입
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", username)
                            .param("password", password)
                            .param("name", "Login User")
                            .param("email", "loginuser_" + uniqueId + "@example.com"))
                    .andExpect(status().isNoContent());

            // 로그인 시도
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", username)
                            .param("password", password))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("provideInvalidLoginData")
        @DisplayName("❌ 잘못된 자격증명 → 401 Unauthorized")
        void shouldFailWithInvalidCredentials(String displayName, String username, String password) throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", username)
                            .param("password", password))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andDo(print());
        }

        @BeforeEach
        void setupValidUser() throws Exception {
            // 유효한 사용자 생성 (테스트용)
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "validuser")
                            .param("password", "Password123!")
                            .param("name", "Valid User")
                            .param("email", "valid@example.com"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/check - 로그인 상태 확인")
    @Tag("auth")
    class AuthCheck {

        @Test
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/auth/check"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("권한별 접근 제어")
    @Tag("security")
    class AccessControl {

        @Test
        @DisplayName("✅ 공개 엔드포인트 - 인증 없이 접근 가능")
        void shouldAllowPublicAccess() throws Exception {
            mockMvc.perform(get("/api/auth/public-access"))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 사용자 전용 엔드포인트 - 인증 없이 접근 실패")
        void shouldDenyUserAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/auth/user-access"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("✅ 사용자 전용 엔드포인트 - 일반 사용자 접근 성공")
        @WithUserDetails
        void shouldAllowUserAccess() throws Exception {
            mockMvc.perform(get("/api/auth/user-access"))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @Test
        @DisplayName("✅ 사용자 전용 엔드포인트 - 관리자 접근 성공")
        @WithUserDetails("admin")
        void shouldAllowAdminAccessToUserEndpoint() throws Exception {
            mockMvc.perform(get("/api/auth/user-access"))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @Nested
        @DisplayName("관리자 전용 엔드포인트")
        class AdminOnly {

            @Test
            @DisplayName("❌ 인증 없이 접근 → 401 Unauthorized")
            void shouldReturn401WhenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/api/auth/admin-only"))
                        .andExpect(status().isUnauthorized())
                        .andDo(print());
            }

            @Test
            @DisplayName("❌ 일반 사용자 접근 → 403 Forbidden")
            @WithUserDetails
            void shouldReturn403WhenRegularUser() throws Exception {
                mockMvc.perform(get("/api/auth/admin-only"))
                        .andExpect(status().isForbidden())
                        .andDo(print());
            }

            @Test
            @DisplayName("✅ 관리자 접근 성공")
            @WithUserDetails("admin")
            void shouldAllowAdminAccess() throws Exception {
                mockMvc.perform(get("/api/auth/admin-only"))
                        .andExpect(status().isNoContent())
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - 로그아웃")
    @Tag("auth")
    class Logout {

        @Test
        @DisplayName("✅ 로그인된 사용자 로그아웃 성공")
        @WithUserDetails
        void shouldLogoutWhenAuthenticated() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 인증되지 않은 상태에서 로그아웃 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}