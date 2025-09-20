package dev.xiyo.bunnyholes.boardhole.auth.application.command;

import java.util.Set;
import java.util.UUID;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import dev.xiyo.bunnyholes.boardhole.auth.application.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.auth.application.mapper.AuthMapper;
import dev.xiyo.bunnyholes.boardhole.auth.application.result.AuthResult;
import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.shared.test.MessageSourceTestConfig;
import dev.xiyo.bunnyholes.boardhole.shared.test.ValidationEnabledTestConfig;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.UserValidationConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@Import({SessionAuthCommandService.class, ValidationEnabledTestConfig.class, MessageSourceTestConfig.class})
@Tag("unit")
class SessionAuthCommandServiceTest {
    private static final String VALID_USERNAME = "testuser"; // meets @ValidUsername (3-20 chars, not blank)

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthMapper authMapper;

    @Autowired
    private AuthCommandService service;

    private User user;
    private AuthResult authResult;
    private AppUserPrincipal principal;

    @BeforeEach
    void setUp() {
        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // 테스트 데이터 준비 - 이메일 인증 완료된 사용자
        user = User
                .builder()
                .username("testuser")
                .password("password")
                .name("Test User")
                .email("test@example.com")
                .roles(Set.of(Role.USER))
                .build();
        user.verifyEmail();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        // User는 기본적으로 권한을 가짐

        principal = new AppUserPrincipal(user);

        authResult = new AuthResult(UUID.randomUUID(), "testuser", "test@example.com", "Test User", "USER", true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("로그인 처리")
    class LoginTest {
        private static final String VALID_PASSWORD = "P@ssw0rd1!"; // meets pattern

        @Test
        @DisplayName("유효한 자격증명으로 로그인 성공")
        void login_ValidCredentials_Success() {
            // Given
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, VALID_PASSWORD);
            Authentication authRequest = new UsernamePasswordAuthenticationToken("testuser", "password");
            Authentication authentication = mock(Authentication.class);

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(principal);
            given(authMapper.toAuthResult(user)).willReturn(
                    authResult);

            // When
            AuthResult result = service.login(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.name()).isEqualTo("Test User");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.role()).isEqualTo("USER");

            // SecurityContext에 인증 정보가 저장되었는지 확인
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isEqualTo(authentication);

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(authMapper).toAuthResult(user);
        }

        @Test
        @DisplayName("잘못된 자격증명으로 로그인 실패")
        void login_InvalidCredentials_ThrowsUnauthorizedException() {
            // Given
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, VALID_PASSWORD);

            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining(MessageUtils.get("error.auth.invalid-credentials"));

            // SecurityContext가 비어있는지 확인
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNull();

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(authMapper, never()).toAuthResult(any());
        }

        @Test
        @DisplayName("계정이 잠긴 경우 로그인 실패")
        void login_AccountLocked_ThrowsAuthenticationException() {
            // Given
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, VALID_PASSWORD);

            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new LockedException("Account is locked"));

            // When & Then
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(LockedException.class).hasMessageContaining("Account is locked");

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(authMapper, never()).toAuthResult(any());
        }

        @Test
        @DisplayName("계정이 비활성화된 경우 로그인 실패")
        void login_AccountDisabled_ThrowsAuthenticationException() {
            // Given
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, VALID_PASSWORD);

            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new DisabledException("Account is disabled"));

            // When & Then
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(DisabledException.class).hasMessageContaining("Account is disabled");

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(authMapper, never()).toAuthResult(any());
        }

        @Test
        @DisplayName("빈 사용자명으로 로그인 시도")
        void login_EmptyUsername_HandledByValidation() {
            // Given
            LoginCommand command = new LoginCommand("", "password");

            // When & Then: 메서드 레벨 검증 실패 기대
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(ConstraintViolationException.class);

            // 검증 단계에서 차단되므로 authenticate 호출되지 않음
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("빈 비밀번호로 로그인 시도")
        void login_EmptyPassword_HandledByValidation() {
            // Given
            LoginCommand command = new LoginCommand("testuser", "");

            // When & Then: 메서드 레벨 검증 실패 기대
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(ConstraintViolationException.class);

            // 검증 단계에서 차단되므로 authenticate 호출되지 않음
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("패스워드가 규칙(대/소문자,숫자,특수) 미충족")
        void login_InvalidPasswordPattern_ValidationFails() {
            // Given: 소문자만
            LoginCommand cmd1 = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, "lowercaseonly");
            // 숫자/특수만
            LoginCommand cmd2 = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, "12345678!");
            // 대문자/소문자만
            LoginCommand cmd3 = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, "NoSpecial1");

            // Then
            assertThatThrownBy(() -> service.login(cmd1)).isInstanceOf(ConstraintViolationException.class);
            assertThatThrownBy(() -> service.login(cmd2)).isInstanceOf(ConstraintViolationException.class);
            assertThatThrownBy(() -> service.login(cmd3)).isInstanceOf(ConstraintViolationException.class);
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("패스워드가 8자 미만")
        void login_ShortPassword_ValidationFails() {
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, "P@ss1!");
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(ConstraintViolationException.class);
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("사용자명이 3자 미만")
        void login_UsernameTooShort_ValidationFails() {
            String shortUsername = "a".repeat(UserValidationConstants.USER_USERNAME_MIN_LENGTH - 1);
            LoginCommand command = new LoginCommand(shortUsername, VALID_PASSWORD);
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(ConstraintViolationException.class);
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("사용자명이 20자 초과")
        void login_UsernameTooLong_ValidationFails() {
            String longUsername = "a".repeat(UserValidationConstants.USER_USERNAME_MAX_LENGTH + 1);
            LoginCommand command = new LoginCommand(longUsername, VALID_PASSWORD);
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(ConstraintViolationException.class);
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("사용자명이 공백")
        void login_UsernameBlank_ValidationFails() {
            LoginCommand command = new LoginCommand("   ", VALID_PASSWORD);
            assertThatThrownBy(() -> service.login(command)).isInstanceOf(ConstraintViolationException.class);
            verify(authenticationManager, never()).authenticate(any(Authentication.class));
        }
    }

    @Nested
    @DisplayName("로그아웃 처리")
    class LogoutTest {

        @Test
        @DisplayName("정상적인 로그아웃 처리")
        void logout_ClearsSecurityContext() {
            // Given
            LogoutCommand command = new LogoutCommand(UUID.randomUUID());

            // SecurityContext에 인증 정보 설정
            Authentication authentication = mock(Authentication.class);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // 인증 정보가 설정되었는지 확인
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

            // When
            service.logout(command);

            // Then
            // SecurityContext가 비워졌는지 확인
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("이미 로그아웃 상태에서 로그아웃 시도")
        void logout_AlreadyLoggedOut_NoException() {
            // Given
            LogoutCommand command = new LogoutCommand(UUID.randomUUID());

            // SecurityContext가 이미 비어있는 상태
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

            // When
            service.logout(command);

            // Then
            // 여전히 비어있는지 확인
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("보안 컨텍스트 관리")
    class SecurityContextManagementTest {

        @Test
        @DisplayName("로그인 성공 후 SecurityContext 상태 확인")
        void login_Success_SecurityContextProperlySet() {
            // Given
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, LoginTest.VALID_PASSWORD);
            Authentication authentication = mock(Authentication.class);

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(principal);
            given(authMapper.toAuthResult(user)).willReturn(
                    authResult);

            // When
            service.login(command);

            // Then
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context).isNotNull();
            assertThat(context.getAuthentication()).isNotNull();
            assertThat(context.getAuthentication()).isEqualTo(authentication);
        }

        @Test
        @DisplayName("로그인 실패 시 SecurityContext가 비어있음")
        void login_Failure_SecurityContextRemainsClear() {
            // Given
            LoginCommand command = new LoginCommand(SessionAuthCommandServiceTest.VALID_USERNAME, LoginTest.VALID_PASSWORD);

            given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new BadCredentialsException("Invalid"));

            // When
            try {
                service.login(command);
            } catch (UnauthorizedException e) {
                // Expected exception
            }

            // Then
            SecurityContext context = SecurityContextHolder.getContext();
            assertThat(context.getAuthentication()).isNull();
        }
    }
}
