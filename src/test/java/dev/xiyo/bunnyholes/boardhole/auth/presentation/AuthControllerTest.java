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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.auth.application.command.LoginCommand;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.dto.LoginRequest;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.mapper.AuthWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.config.ApiSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class)
        }
)
@AutoConfigureMockMvc
@Import({ApiSecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("AuthController MockMvc 테스트")
@Tag("unit")
@Tag("auth")
class AuthControllerTest {

    private static final String SIGNUP_URL = ApiPaths.AUTH + ApiPaths.AUTH_SIGNUP;
    private static final String LOGIN_URL = ApiPaths.AUTH + ApiPaths.AUTH_LOGIN;
    private static final String LOGOUT_URL = ApiPaths.AUTH + ApiPaths.AUTH_LOGOUT;

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

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
    class Signup {

        @Test
        @DisplayName("✅ 유효한 요청으로 회원가입 성공")
        void shouldCreateUserSuccessfully() throws Exception {
            UserCreateRequest request = new UserCreateRequest(
                    "testuser", "Password123!", "Password123!", "Test User", "test@example.com"
            );
            CreateUserCommand command = new CreateUserCommand(
                    request.username(), request.password(), request.name(), request.email()
            );
            UUID userId = UUID.randomUUID();
            UserResult signupResult = new UserResult(
                    userId,
                    request.username(),
                    request.name(),
                    request.email(),
                    LocalDateTime.now(),
                    null,
                    null,
                    Set.of(Role.USER)
            );

            given(userWebMapper.toCreateCommand(any(UserCreateRequest.class))).willReturn(command);
            given(userCommandService.create(command)).willReturn(signupResult);

            mockMvc.perform(post(AuthControllerTest.SIGNUP_URL)
                           .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                           .param("username", request.username())
                           .param("password", request.password())
                           .param("confirmPassword", request.confirmPassword())
                           .param("name", request.name())
                           .param("email", request.email())
                   )
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
            then(authCommandService).should().login(request.username());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    class Login {

        @Test
        @DisplayName("✅ 유효한 자격증명으로 로그인 성공")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
            LoginCommand loginCommand = new LoginCommand("testuser", "Password123!");

            given(authWebMapper.toLoginCommand(any(LoginRequest.class))).willReturn(loginCommand);

            mockMvc.perform(post(AuthControllerTest.LOGIN_URL)
                           .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                           .param("username", loginRequest.username())
                           .param("password", loginRequest.password())
                   )
                   .andExpect(status().isNoContent());

            ArgumentCaptor<LoginRequest> requestCaptor = ArgumentCaptor.forClass(LoginRequest.class);
            then(authWebMapper).should().toLoginCommand(requestCaptor.capture());
            LoginRequest captured = requestCaptor.getValue();
            assertThat(captured.username()).isEqualTo(loginRequest.username());
            assertThat(captured.password()).isEqualTo(loginRequest.password());

            then(authCommandService).should().login(loginCommand);
            then(userCommandService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("❌ 로그인 실패 시 401 ProblemDetail 응답")
        void shouldReturnUnauthorizedOnLoginFailure() throws Exception {
            LoginRequest loginRequest = new LoginRequest("testuser", "WrongPassword!");
            LoginCommand loginCommand = new LoginCommand("testuser", "WrongPassword!");

            given(authWebMapper.toLoginCommand(any(LoginRequest.class))).willReturn(loginCommand);
            willThrow(new UnauthorizedException("Invalid credentials"))
                    .given(authCommandService)
                    .login(loginCommand);

            mockMvc.perform(post(AuthControllerTest.LOGIN_URL)
                           .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                           .param("username", loginRequest.username())
                           .param("password", loginRequest.password())
                   )
                   .andExpect(status().isUnauthorized())
                   .andExpect(jsonPath("$.status").value(401));

            then(authCommandService).should().login(loginCommand);
            then(userCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - 로그아웃")
    class Logout {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("✅ 인증된 사용자가 로그아웃하면 서비스 호출")
        void shouldLogoutWithPrincipal() throws Exception {
            mockMvc.perform(post(AuthControllerTest.LOGOUT_URL))
                   .andExpect(status().isNoContent());

            then(authCommandService).should().logout();
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증 정보 없이 로그아웃 시 403 응답")
        void shouldNotLogoutWithoutPrincipal() throws Exception {
            mockMvc.perform(post(AuthControllerTest.LOGOUT_URL))
                   .andExpect(status().isForbidden())
                   .andExpect(jsonPath("$.status").value(403));

            then(authCommandService).shouldHaveNoInteractions();
        }
    }

}
