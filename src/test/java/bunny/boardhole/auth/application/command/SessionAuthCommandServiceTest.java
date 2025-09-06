package bunny.boardhole.auth.application.command;

import bunny.boardhole.auth.application.mapper.AuthMapper;
import bunny.boardhole.auth.application.result.AuthResult;
import bunny.boardhole.shared.exception.UnauthorizedException;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
class SessionAuthCommandServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private SessionAuthCommandService service;

    private User user;
    private AuthResult authResult;
    private AppUserPrincipal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // MessageUtils 초기화
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // 테스트 데이터 준비
        user = User.builder()
                .username("testuser")
                .password("password")
                .name("Test User")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        // User는 기본적으로 권한을 가짐

        principal = new AppUserPrincipal(user);

        authResult = new AuthResult(
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                "USER",
                true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("로그인 처리")
    class LoginTest {

        @Test
        @DisplayName("유효한 자격증명으로 로그인 성공")
        void login_ValidCredentials_Success() {
            // Given
            LoginCommand command = new LoginCommand("testuser", "password");
            Authentication authRequest = new UsernamePasswordAuthenticationToken("testuser", "password");
            Authentication authentication = mock(Authentication.class);

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(principal);
            given(authMapper.toAuthResult(user)).willReturn(authResult);

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
            LoginCommand command = new LoginCommand("testuser", "wrongpassword");

            given(authenticationManager.authenticate(any(Authentication.class)))
                    .willThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid username or password");

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
            LoginCommand command = new LoginCommand("testuser", "password");

            given(authenticationManager.authenticate(any(Authentication.class)))
                    .willThrow(new LockedException("Account is locked"));

            // When & Then
            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("Account is locked");

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(authMapper, never()).toAuthResult(any());
        }

        @Test
        @DisplayName("계정이 비활성화된 경우 로그인 실패")
        void login_AccountDisabled_ThrowsAuthenticationException() {
            // Given
            LoginCommand command = new LoginCommand("testuser", "password");

            given(authenticationManager.authenticate(any(Authentication.class)))
                    .willThrow(new DisabledException("Account is disabled"));

            // When & Then
            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(DisabledException.class)
                    .hasMessageContaining("Account is disabled");

            verify(authenticationManager).authenticate(any(Authentication.class));
            verify(authMapper, never()).toAuthResult(any());
        }

        @Test
        @DisplayName("빈 사용자명으로 로그인 시도")
        void login_EmptyUsername_HandledByValidation() {
            // Given
            LoginCommand command = new LoginCommand("", "password");

            given(authenticationManager.authenticate(any(Authentication.class)))
                    .willThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(UnauthorizedException.class);

            verify(authenticationManager).authenticate(any(Authentication.class));
        }

        @Test
        @DisplayName("빈 비밀번호로 로그인 시도")
        void login_EmptyPassword_HandledByValidation() {
            // Given
            LoginCommand command = new LoginCommand("testuser", "");

            given(authenticationManager.authenticate(any(Authentication.class)))
                    .willThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> service.login(command))
                    .isInstanceOf(UnauthorizedException.class);

            verify(authenticationManager).authenticate(any(Authentication.class));
        }
    }

    @Nested
    @DisplayName("로그아웃 처리")
    class LogoutTest {

        @Test
        @DisplayName("정상적인 로그아웃 처리")
        void logout_ClearsSecurityContext() {
            // Given
            LogoutCommand command = new LogoutCommand(1L);

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
            LogoutCommand command = new LogoutCommand(1L);

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
            LoginCommand command = new LoginCommand("testuser", "password");
            Authentication authentication = mock(Authentication.class);

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(principal);
            given(authMapper.toAuthResult(user)).willReturn(authResult);

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
            LoginCommand command = new LoginCommand("testuser", "wrongpassword");

            given(authenticationManager.authenticate(any(Authentication.class)))
                    .willThrow(new BadCredentialsException("Invalid"));

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