package dev.xiyo.bunnyholes.boardhole.auth.application.command;

import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.shared.test.MessageSourceTestConfig;
import dev.xiyo.bunnyholes.boardhole.shared.test.ValidationEnabledTestConfig;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(SpringExtension.class)
@Import({AuthCommandService.class, ValidationEnabledTestConfig.class, MessageSourceTestConfig.class})
@Tag("unit")
class AuthCommandServiceTest {

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "P@ssw0rd1!";

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private UserCommandService userCommandService;

    @Autowired
    private AuthCommandService service;

    private User user;
    private AppUserPrincipal principal;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));

        user = User.builder()
                   .username(VALID_USERNAME)
                   .password("encoded")
                   .name("Test User")
                   .email("test@example.com")
                   .roles(Set.of(Role.USER))
                   .build();
        user.verifyEmail();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        principal = new AppUserPrincipal(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Nested
    @DisplayName("login(LoginCommand)")
    class LoginWithCommand {

        @Test
        @DisplayName("성공 시 SecurityContext 저장")
        void success() {
            LoginCommand command = new LoginCommand(AuthCommandServiceTest.VALID_USERNAME, AuthCommandServiceTest.VALID_PASSWORD);
            Authentication authentication = mock(Authentication.class);

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(principal);

            service.login(command);
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNotNull();
            assertThat(context.getAuthentication().getPrincipal()).isEqualTo(principal);

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(userCommandService).updateLastLogin(user.getId());
            verify(userRepository, never()).findById(any());
            verify(securityContextRepository).saveContext(eq(context), any(HttpServletRequest.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("잘못된 자격증명 시 UnauthorizedException")
        void invalidCredentials() {
            LoginCommand command = new LoginCommand(AuthCommandServiceTest.VALID_USERNAME, AuthCommandServiceTest.VALID_PASSWORD);
            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new BadCredentialsException("Invalid"));

            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining(MessageUtils.get("error.auth.invalid-credentials"));

            verify(authenticationManager).authenticate(any(Authentication.class));
            verifyNoInteractions(userCommandService);
            verifyNoInteractions(userRepository);
            verifyNoInteractions(securityContextRepository);
        }

        @Test
        @DisplayName("계정 잠김/비활성화 예외를 그대로 전달")
        void propagatesLockStates() {
            LoginCommand command = new LoginCommand(AuthCommandServiceTest.VALID_USERNAME, AuthCommandServiceTest.VALID_PASSWORD);
            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new LockedException("locked"));
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(LockedException.class);

            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new DisabledException("disabled"));
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(DisabledException.class);
        }

        @Test
        @DisplayName("입력 검증 실패 시 authenticate 호출 안 함")
        void validationFailure() {
            LoginCommand invalid = new LoginCommand(" ", AuthCommandServiceTest.VALID_PASSWORD);
            assertThatThrownBy(() -> service.login(invalid)).isInstanceOf(ConstraintViolationException.class);
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }
    }

    @Nested
    @DisplayName("login(UUID)")
    class LoginWithUserId {

        @Test
        @DisplayName("사용자 ID로 자동 로그인")
        void loginByUserId() {
            UUID userId = user.getId();
            given(userRepository.findById(userId)).willReturn(java.util.Optional.of(user));

            service.login(userId);

            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNotNull();
            verify(userRepository).findById(userId);
            verify(securityContextRepository).saveContext(eq(context), any(HttpServletRequest.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 IllegalStateException")
        void userNotFound() {
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(java.util.Optional.empty());
            assertThatThrownBy(() -> service.login(userId)).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("인증 정보 초기화 및 컨텍스트 저장")
        void logoutClearsContext() {
            // Set existing authentication
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(mock(Authentication.class));
            SecurityContextHolder.setContext(context);

            service.logout();

            SecurityContext cleared = SecurityContextHolder.getContext();
            assertThat(cleared.getAuthentication()).isNull();
            verify(securityContextRepository).saveContext(eq(cleared), any(HttpServletRequest.class), any(HttpServletResponse.class));
        }
    }
}
