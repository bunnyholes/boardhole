package dev.xiyo.bunnyholes.boardhole.auth.presentation;

import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import dev.xiyo.bunnyholes.boardhole.auth.application.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.auth.application.command.LoginCommand;
import dev.xiyo.bunnyholes.boardhole.auth.application.command.LogoutCommand;
import dev.xiyo.bunnyholes.boardhole.auth.application.mapper.AuthMapper;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.dto.LoginRequest;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.mapper.AuthWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.user.application.command.CreateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserCreateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 단위 테스트")
@Tag("unit")
@Tag("auth")
class AuthControllerTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private AuthCommandService authCommandService;

    @Mock
    private AuthWebMapper authWebMapper;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private UserWebMapper userWebMapper;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private AppUserPrincipal testPrincipal;

    @BeforeEach
    void setUp() throws Exception {
        testUser = User.builder()
                       .username("testuser")
                       .password("encoded_password")
                       .name("Test User")
                       .email("test@example.com")
                       .roles(Set.of(Role.USER))
                       .build();

        // Use reflection to set the UUID ID for testing
        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, UUID.randomUUID());

        testPrincipal = new AppUserPrincipal(testUser);
    }

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
    class Signup {

        @Test
        @DisplayName("✅ 유효한 요청으로 회원가입 성공")
        void shouldCreateUserSuccessfully() {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "testuser", "Password123!", "Password123!", "Test User", "test@example.com"
            );
            CreateUserCommand command = new CreateUserCommand(
                    "testuser", "Password123!", "Test User", "test@example.com"
            );

            given(userWebMapper.toCreateCommand(request)).willReturn(command);
            given(userCommandService.create(command)).willReturn(new dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult(
                    UUID.randomUUID(), "testuser", "Test User", "test@example.com",
                    java.time.LocalDateTime.now(), null, null, Set.of(dev.xiyo.bunnyholes.boardhole.user.domain.Role.USER)
            ));

            // when
            authController.signup(request);

            // then
            then(userWebMapper).should().toCreateCommand(request);
            then(userCommandService).should().create(command);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    class Login {

        @Test
        @DisplayName("✅ 유효한 자격증명으로 로그인 성공 - SecurityContext 저장")
        void shouldLoginSuccessfullyWithSecurityContext() {
            // given
            LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
            LoginCommand loginCommand = new LoginCommand("testuser", "Password123!");

            given(authWebMapper.toLoginCommand(loginRequest)).willReturn(loginCommand);
            given(authCommandService.login(loginCommand)).willReturn(new dev.xiyo.bunnyholes.boardhole.auth.application.result.AuthResult(
                    UUID.randomUUID(), "testuser", "test@example.com", "Test User", "USER", true
            ));

            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                                           .thenReturn(securityContext);
                given(securityContext.getAuthentication()).willReturn(authentication);
                given(authentication.getPrincipal()).willReturn(testPrincipal);
                willDoNothing().given(securityContextRepository).saveContext(securityContext,
                        request, response);
                willDoNothing().given(userCommandService).updateLastLogin(testUser.getId());

                // when
                authController.login(loginRequest, request, response);

                // then
                then(authWebMapper).should().toLoginCommand(loginRequest);
                then(authCommandService).should().login(loginCommand);
                then(securityContextRepository).should().saveContext(securityContext,
                        request, response);
                then(userCommandService).should().updateLastLogin(testUser.getId());
            }
        }

        @Test
        @DisplayName("✅ 인증 없이 로그인 - SecurityContext 저장 안함")
        void shouldLoginWithoutAuthentication() {
            // given
            LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
            LoginCommand loginCommand = new LoginCommand("testuser", "Password123!");

            given(authWebMapper.toLoginCommand(loginRequest)).willReturn(loginCommand);
            given(authCommandService.login(loginCommand)).willReturn(new dev.xiyo.bunnyholes.boardhole.auth.application.result.AuthResult(
                    UUID.randomUUID(), "testuser", "test@example.com", "Test User", "USER", true
            ));

            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                                           .thenReturn(securityContext);
                given(securityContext.getAuthentication()).willReturn(null);

                // when
                authController.login(loginRequest, request, response);

                // then
                then(authWebMapper).should().toLoginCommand(loginRequest);
                then(authCommandService).should().login(loginCommand);
                then(securityContextRepository).should(never()).saveContext(any(), any(), any());
                then(userCommandService).should(never()).updateLastLogin(any(UUID.class));
            }
        }

        @Test
        @DisplayName("✅ updateLastLogin 예외 발생 시 로그인 성공 유지")
        void shouldContinueLoginWhenUpdateLastLoginFails() {
            // given
            LoginRequest loginRequest = new LoginRequest("testuser", "Password123!");
            LoginCommand loginCommand = new LoginCommand("testuser", "Password123!");

            given(authWebMapper.toLoginCommand(loginRequest)).willReturn(loginCommand);
            given(authCommandService.login(loginCommand)).willReturn(new dev.xiyo.bunnyholes.boardhole.auth.application.result.AuthResult(
                    UUID.randomUUID(), "testuser", "test@example.com", "Test User", "USER", true
            ));

            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                                           .thenReturn(securityContext);
                given(securityContext.getAuthentication()).willReturn(authentication);
                given(authentication.getPrincipal()).willReturn(testPrincipal);
                willDoNothing().given(securityContextRepository).saveContext(securityContext,
                        request, response);
                willThrow(new UnsupportedOperationException("Test exception"))
                        .given(userCommandService).updateLastLogin(testUser.getId());

                // when
                authController.login(loginRequest, request, response);

                // then
                then(authWebMapper).should().toLoginCommand(loginRequest);
                then(authCommandService).should().login(loginCommand);
                then(securityContextRepository).should().saveContext(securityContext,
                        request, response);
                then(userCommandService).should().updateLastLogin(testUser.getId());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - 로그아웃")
    class Logout {

        @Test
        @DisplayName("✅ 인증된 사용자 로그아웃 성공")
        void shouldLogoutAuthenticatedUser() {
            // given
            LogoutCommand logoutCommand = new LogoutCommand(testUser.getId());

            given(authMapper.toLogoutCommand(testUser.getId())).willReturn(logoutCommand);
            willDoNothing().given(authCommandService).logout(logoutCommand);
            given(request.getSession(false)).willReturn(session);
            willDoNothing().given(session).invalidate();

            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecurityContextHolder.when(SecurityContextHolder::clearContext).then(invocation -> null);
                mockedSecurityContextHolder.when(SecurityContextHolder::createEmptyContext).thenReturn(securityContext);
                willDoNothing().given(securityContextRepository).saveContext(securityContext,
                        request, response);

                // when
                authController.logout(request, response,
                        testPrincipal);

                // then
                then(authMapper).should().toLogoutCommand(testUser.getId());
                then(authCommandService).should().logout(logoutCommand);
                then(session).should().invalidate();
                then(securityContextRepository).should().saveContext(securityContext,
                        request, response);
            }
        }

        @Test
        @DisplayName("✅ 세션이 없는 상태에서 로그아웃")
        void shouldLogoutWithoutSession() {
            // given
            LogoutCommand logoutCommand = new LogoutCommand(testUser.getId());

            given(authMapper.toLogoutCommand(testUser.getId())).willReturn(logoutCommand);
            willDoNothing().given(authCommandService).logout(logoutCommand);
            given(request.getSession(false)).willReturn(null);

            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecurityContextHolder.when(SecurityContextHolder::clearContext).then(invocation -> null);
                mockedSecurityContextHolder.when(SecurityContextHolder::createEmptyContext).thenReturn(securityContext);
                willDoNothing().given(securityContextRepository).saveContext(securityContext,
                        request, response);

                // when
                authController.logout(request, response,
                        testPrincipal);

                // then
                then(authMapper).should().toLogoutCommand(testUser.getId());
                then(authCommandService).should().logout(logoutCommand);
                then(securityContextRepository).should().saveContext(securityContext,
                        request, response);
            }
        }
    }

    @Nested
    @DisplayName("권한 제어 엔드포인트")
    class AccessControl {

        @Test
        @DisplayName("✅ 관리자 전용 엔드포인트 호출")
        void shouldCallAdminOnlyEndpoint() throws Exception {
            // given
            User adminUser = User.builder()
                                 .username("admin")
                                 .password("password")
                                 .name("Admin User")
                                 .email("admin@example.com")
                                 .roles(Set.of(Role.ADMIN))
                                 .build();

            // Use reflection to set the UUID ID for testing
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(adminUser, UUID.randomUUID());

            AppUserPrincipal adminPrincipal = new AppUserPrincipal(adminUser);

            // when
            authController.adminOnly(adminPrincipal);

            // then - 예외 없이 정상 실행됨을 검증 (로깅만 수행하므로 별도 검증 없음)
        }

        @Test
        @DisplayName("✅ 일반 사용자 접근 엔드포인트 호출")
        void shouldCallUserAccessEndpoint() {
            // when
            authController.userAccess(testPrincipal);

            // then - 예외 없이 정상 실행됨을 검증 (로깅만 수행하므로 별도 검증 없음)
        }

        @Test
        @DisplayName("✅ 공개 엔드포인트 호출")
        void shouldCallPublicAccessEndpoint() {
            // when
            authController.publicAccess();

            // then - 예외 없이 정상 실행됨을 검증 (로깅만 수행하므로 별도 검증 없음)
        }
    }
}