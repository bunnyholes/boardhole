package dev.xiyo.bunnyholes.boardhole.auth.presentation;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.auth.application.command.LoginCommand;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.dto.LoginRequest;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.mapper.AuthWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.config.ApiSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateUsernameException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.user.application.command.CreateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserCreateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(ApiSecurityConfig.class)
@DisplayName("AuthController MockMvc 테스트")
@Tag("unit")
@Tag("auth")
class AuthControllerTest {

    private static final String SIGNUP_URL = ApiPaths.AUTH + ApiPaths.AUTH_SIGNUP;
    private static final String LOGIN_URL = ApiPaths.AUTH + ApiPaths.AUTH_LOGIN;
    private static final String LOGOUT_URL = ApiPaths.AUTH + ApiPaths.AUTH_LOGOUT;
    private static final String ADMIN_ONLY_URL = ApiPaths.AUTH + ApiPaths.AUTH_ADMIN_ONLY;
    private static final String USER_ACCESS_URL = ApiPaths.AUTH + ApiPaths.AUTH_USER_ACCESS;
    private static final String PUBLIC_ACCESS_URL = ApiPaths.AUTH + ApiPaths.AUTH_PUBLIC_ACCESS;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private AuthCommandService authCommandService;

    @MockitoBean
    private AuthWebMapper authWebMapper;

    @MockitoBean
    private UserWebMapper userWebMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private EntityManager entityManager;

    private static MockHttpServletRequestBuilder form(MockHttpServletRequestBuilder builder) {
        return builder.contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf());
    }

    private static UserCreateRequest validSignupRequest() {
        return new UserCreateRequest("testuser", "Password123!", "Password123!", "테스터", "tester@example.com");
    }

    private static LoginRequest validLoginRequest() {
        return new LoginRequest("testuser", "Password123!");
    }

    private static UserResult userResult(UserCreateRequest request) {
        UUID userId = UUID.randomUUID();
        return new UserResult(
                userId,
                request.username(),
                request.name(),
                request.email(),
                LocalDateTime.now(),
                null,
                null,
                Set.of(Role.USER),
                false
        );
    }

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
    class Signup {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("✅ 유효한 요청으로 회원가입 성공")
            void shouldCreateUserSuccessfully() throws Exception {
                UserCreateRequest request = validSignupRequest();
                CreateUserCommand command = new CreateUserCommand(
                        request.username(), request.password(), request.name(), request.email()
                );
                UserResult signupResult = userResult(request);

                given(userWebMapper.toCreateCommand(any(UserCreateRequest.class))).willReturn(command);
                given(userCommandService.create(command)).willReturn(signupResult);

                mockMvc.perform(form(post(SIGNUP_URL))
                                .param("username", request.username())
                                .param("password", request.password())
                                .param("confirmPassword", request.confirmPassword())
                                .param("name", request.name())
                                .param("email", request.email()))
                        .andExpect(status().isNoContent());

                ArgumentCaptor<UserCreateRequest> requestCaptor = ArgumentCaptor.forClass(UserCreateRequest.class);
                then(userWebMapper).should().toCreateCommand(requestCaptor.capture());
                UserCreateRequest captured = requestCaptor.getValue();
                assertThat(captured.username()).isEqualTo(request.username());
                assertThat(captured.password()).isEqualTo(request.password());
                assertThat(captured.confirmPassword()).isEqualTo(request.confirmPassword());
                assertThat(captured.name()).isEqualTo(request.name());
                assertThat(captured.email()).isEqualTo(request.email());

                then(userCommandService).should().create(command);
                then(authCommandService).should().login(signupResult.username());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @DisplayName("❌ 중복 사용자명은 409 ProblemDetail을 반환한다")
                void shouldReturnConflictWhenUsernameDuplicated() throws Exception {
                    UserCreateRequest request = validSignupRequest();
                    CreateUserCommand command = new CreateUserCommand(
                            request.username(), request.password(), request.name(), request.email()
                    );

                    given(userWebMapper.toCreateCommand(any(UserCreateRequest.class))).willReturn(command);
                    willThrow(new DuplicateUsernameException("이미 사용 중인 사용자명입니다."))
                            .given(userCommandService)
                            .create(command);

                    mockMvc.perform(form(post(SIGNUP_URL))
                                    .param("username", request.username())
                                    .param("password", request.password())
                                    .param("confirmPassword", request.confirmPassword())
                                    .param("name", request.name())
                                    .param("email", request.email()))
                            .andExpect(status().isConflict())
                            .andExpect(jsonPath("$.status").value(409))
                            .andExpect(jsonPath("$.type").value("urn:problem-type:duplicate-username"));

                    then(userCommandService).should().create(command);
                    then(authCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @DisplayName("❌ 패스워드 불일치 시 422 ProblemDetail을 반환한다")
                void shouldValidatePasswordConfirmation() throws Exception {
                    UserCreateRequest request = validSignupRequest();

                    mockMvc.perform(form(post(SIGNUP_URL))
                                    .param("username", request.username())
                                    .param("password", request.password())
                                    .param("confirmPassword", "Mismatch123!")
                                    .param("name", request.name())
                                    .param("email", request.email()))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                            .andExpect(jsonPath("$.errors").isArray());

                    then(userCommandService).shouldHaveNoInteractions();
                    then(authCommandService).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    class Login {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("✅ 유효한 자격증명으로 로그인 성공")
            void shouldLoginSuccessfully() throws Exception {
                LoginRequest loginRequest = validLoginRequest();
                LoginCommand loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());

                given(authWebMapper.toLoginCommand(any(LoginRequest.class))).willReturn(loginCommand);

                mockMvc.perform(form(post(LOGIN_URL))
                                .param("username", loginRequest.username())
                                .param("password", loginRequest.password()))
                        .andExpect(status().isNoContent());

                ArgumentCaptor<LoginRequest> requestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
                then(authWebMapper).should().toLoginCommand(requestCaptor.capture());
                LoginRequest captured = requestCaptor.getValue();
                assertThat(captured.username()).isEqualTo(loginRequest.username());
                assertThat(captured.password()).isEqualTo(loginRequest.password());

                then(authCommandService).should().login(loginCommand);
                then(userCommandService).shouldHaveNoInteractions();
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @DisplayName("❌ 잘못된 자격증명은 401 ProblemDetail을 반환한다")
                void shouldReturnUnauthorizedOnLoginFailure() throws Exception {
                    LoginRequest loginRequest = validLoginRequest();
                    LoginCommand loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());

                    given(authWebMapper.toLoginCommand(any(LoginRequest.class))).willReturn(loginCommand);
                    willThrow(new UnauthorizedException("Invalid credentials"))
                            .given(authCommandService)
                            .login(loginCommand);

                    mockMvc.perform(form(post(LOGIN_URL))
                                    .param("username", loginRequest.username())
                                    .param("password", loginRequest.password()))
                            .andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.status").value(401))
                            .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"));

                    then(authCommandService).should().login(loginCommand);
                    then(userCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @DisplayName("❌ 내부 오류 발생 시 500 ProblemDetail을 반환한다")
                void shouldHandleUnexpectedFailure() throws Exception {
                    LoginRequest loginRequest = validLoginRequest();
                    LoginCommand loginCommand = new LoginCommand(loginRequest.username(), loginRequest.password());

                    given(authWebMapper.toLoginCommand(any(LoginRequest.class))).willReturn(loginCommand);
                    willThrow(new IllegalStateException("세션 생성 실패"))
                            .given(authCommandService)
                            .login(loginCommand);

                    mockMvc.perform(form(post(LOGIN_URL))
                                    .param("username", loginRequest.username())
                                    .param("password", loginRequest.password()))
                            .andExpect(status().isInternalServerError())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:internal-error"));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @DisplayName("❌ 사용자명을 비우면 422 ProblemDetail을 반환한다")
                void shouldValidateBlankUsername() throws Exception {
                    mockMvc.perform(form(post(LOGIN_URL))
                                    .param("username", " ")
                                    .param("password", "Password123!"))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                            .andExpect(jsonPath("$.errors").isArray());

                    then(authCommandService).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - 로그아웃")
    class Logout {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "testuser", roles = "USER")
            @DisplayName("✅ 인증된 사용자가 로그아웃하면 서비스 호출")
            void shouldLogoutWithPrincipal() throws Exception {
                mockMvc.perform(form(post(LOGOUT_URL)))
                        .andExpect(status().isNoContent());

                then(authCommandService).should().logout();
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증 정보 없이 로그아웃 시 401 응답")
                void shouldNotLogoutWithoutPrincipal() throws Exception {
                    mockMvc.perform(form(post(LOGOUT_URL)))
                            .andExpect(status().isUnauthorized());

                    then(authCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "testuser", roles = "USER")
                @DisplayName("❌ 로그아웃 처리 중 오류 발생 시 500 ProblemDetail을 반환한다")
                void shouldHandleLogoutFailure() throws Exception {
                    willThrow(new IllegalStateException("이미 로그아웃되었습니다."))
                            .given(authCommandService)
                            .logout();

                    mockMvc.perform(form(post(LOGOUT_URL)))
                            .andExpect(status().isInternalServerError())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:internal-error"));
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/auth/admin-only - 관리자 전용")
    class AdminOnly {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "admin", roles = "ADMIN")
            @DisplayName("✅ 관리자 권한으로 접근 시 204 응답")
            void shouldAllowAdminAccess() throws Exception {
                mockMvc.perform(get(ADMIN_ONLY_URL))
                        .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 비인증 사용자는 401 응답을 받는다")
                void shouldRejectAnonymousAccess() throws Exception {
                    mockMvc.perform(get(ADMIN_ONLY_URL))
                            .andExpect(status().isUnauthorized());
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 관리자 권한이 없으면 403 응답을 반환한다")
                void shouldRejectWithoutAdminRole() throws Exception {
                    mockMvc.perform(get(ADMIN_ONLY_URL))
                            .andExpect(status().isForbidden());
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/auth/user-access - 사용자 접근")
    class UserAccess {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "tester", roles = "USER")
            @DisplayName("✅ USER 권한으로 접근 시 204 응답")
            void shouldAllowUserRole() throws Exception {
                mockMvc.perform(get(USER_ACCESS_URL))
                        .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않으면 401 응답을 반환한다")
                void shouldRequireAuthentication() throws Exception {
                    mockMvc.perform(get(USER_ACCESS_URL))
                            .andExpect(status().isUnauthorized());
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "GUEST")
                @DisplayName("❌ 허용되지 않은 권한이면 403 응답을 반환한다")
                void shouldRejectUnexpectedRole() throws Exception {
                    mockMvc.perform(get(USER_ACCESS_URL))
                            .andExpect(status().isForbidden());
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/auth/public-access - 공개 접근")
    class PublicAccess {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithAnonymousUser
            @DisplayName("✅ 누구나 접근 가능하다")
            void shouldAllowAnonymous() throws Exception {
                mockMvc.perform(get(PUBLIC_ACCESS_URL))
                        .andExpect(status().isNoContent());
            }
        }
    }
}
