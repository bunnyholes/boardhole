package bunny.boardhole.email.application;

import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService service;

    private User user;
    private EmailVerification emailVerification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // MessageUtils 초기화
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

        // Service 설정값 초기화
        ReflectionTestUtils.setField(service, "verificationExpirationMs", 3600000L); // 1시간

        // 테스트 데이터 준비
        user = User.builder()
                .username("testuser")
                .password("password")
                .name("Test User")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "emailVerified", false);

        emailVerification = EmailVerification.builder()
                .code("test-token-123")
                .userId(1L)
                .newEmail("test@example.com")
                .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1))
                .verificationType(EmailVerificationType.SIGNUP)
                .build();
        ReflectionTestUtils.setField(emailVerification, "used", false);
    }

    @Nested
    @DisplayName("이메일 인증 처리")
    class VerifyEmailTest {

        @Test
        @DisplayName("회원가입 이메일 인증 성공")
        void verifyEmail_SignupVerification_Success() {
            // Given
            final Long userId = 1L;
            final String token = "test-token-123";

            // User와 EmailVerification을 spy로 생성
            User spyUser = spy(user);
            EmailVerification spyVerification = spy(emailVerification);

            given(emailVerificationRepository.findByCodeAndUsedFalse(token))
                    .willReturn(Optional.of(spyVerification));
            given(userRepository.findById(userId)).willReturn(Optional.of(spyUser));
            given(userRepository.save(any(User.class))).willReturn(spyUser);
            given(emailVerificationRepository.save(any(EmailVerification.class)))
                    .willReturn(spyVerification);

            // When
            String result = service.verifyEmail(userId, token);

            // Then
            assertThat(result).contains("이메일 인증이 완료되었습니다");

            verify(emailVerificationRepository).findByCodeAndUsedFalse(token);
            verify(userRepository).findById(userId);
            verify(emailService).sendWelcomeEmail(spyUser);
            verify(userRepository).save(spyUser);
            verify(emailVerificationRepository).save(spyVerification);

            // 사용자 이메일 인증 상태 확인 (spy 객체에 대한 메서드 호출 검증)
            verify(spyUser).verifyEmail();
            verify(spyUser).changeEmail("test@example.com");

            // EmailVerification 사용 처리 확인
            verify(spyVerification).markAsUsed();
        }

        @Test
        @DisplayName("이메일 변경 인증 성공")
        void verifyEmail_ChangeEmailVerification_Success() {
            // Given
            final Long userId = 1L;
            final String token = "change-token-456";
            final String newEmail = "newemail@example.com";

            EmailVerification changeVerification = EmailVerification.builder()
                    .code(token)
                    .userId(userId)
                    .newEmail(newEmail)
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1))
                    .verificationType(EmailVerificationType.CHANGE_EMAIL)
                    .build();
            ReflectionTestUtils.setField(changeVerification, "used", false);

            // User와 EmailVerification을 spy로 생성
            User spyUser = spy(user);
            EmailVerification spyVerification = spy(changeVerification);

            given(emailVerificationRepository.findByCodeAndUsedFalse(token))
                    .willReturn(Optional.of(spyVerification));
            given(userRepository.findById(userId)).willReturn(Optional.of(spyUser));
            given(userRepository.save(any(User.class))).willReturn(spyUser);
            given(emailVerificationRepository.save(any(EmailVerification.class)))
                    .willReturn(spyVerification);

            // When
            String result = service.verifyEmail(userId, token);

            // Then
            assertThat(result).contains("이메일 인증이 완료되었습니다");

            verify(emailVerificationRepository).findByCodeAndUsedFalse(token);
            verify(userRepository).findById(userId);
            verify(emailService).sendEmailChangedNotification(spyUser, newEmail);
            verify(spyUser).changeEmail(newEmail);
            verify(spyVerification).markAsUsed();
        }

        @Test
        @DisplayName("잘못된 토큰으로 인증 실패")
        void verifyEmail_InvalidToken_ThrowsResourceNotFoundException() {
            // Given
            final Long userId = 1L;
            final String invalidToken = "invalid-token";

            given(emailVerificationRepository.findByCodeAndUsedFalse(invalidToken))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.verifyEmail(userId, invalidToken))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("유효하지 않은 인증 토큰");

            verify(emailVerificationRepository).findByCodeAndUsedFalse(invalidToken);
            verify(userRepository, never()).findById(anyLong());
            verify(emailService, never()).sendWelcomeEmail(any());
        }

        @Test
        @DisplayName("만료된 토큰으로 인증 실패")
        void verifyEmail_ExpiredToken_ThrowsIllegalArgumentException() {
            // Given
            final Long userId = 1L;
            final String token = "expired-token";

            EmailVerification expiredVerification = EmailVerification.builder()
                    .code(token)
                    .userId(userId)
                    .newEmail("test@example.com")
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1)) // 만료됨
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build();
            ReflectionTestUtils.setField(expiredVerification, "used", false);

            given(emailVerificationRepository.findByCodeAndUsedFalse(token))
                    .willReturn(Optional.of(expiredVerification));

            // When & Then
            assertThatThrownBy(() -> service.verifyEmail(userId, token))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("만료된 인증 토큰");

            verify(emailVerificationRepository).findByCodeAndUsedFalse(token);
            verify(userRepository, never()).findById(anyLong());
            verify(emailService, never()).sendWelcomeEmail(any());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없는 경우 인증 실패")
        void verifyEmail_UserNotFound_ThrowsResourceNotFoundException() {
            // Given
            final Long userId = 999L;
            final String token = "test-token-123";

            given(emailVerificationRepository.findByCodeAndUsedFalse(token))
                    .willReturn(Optional.of(emailVerification));
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.verifyEmail(userId, token))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(emailVerificationRepository).findByCodeAndUsedFalse(token);
            verify(userRepository).findById(1L);
            verify(emailService, never()).sendWelcomeEmail(any());
        }

        @Test
        @DisplayName("이미 사용된 토큰으로 인증 실패")
        void verifyEmail_AlreadyUsedToken_ThrowsResourceNotFoundException() {
            // Given
            final Long userId = 1L;
            final String token = "used-token";

            // findByCodeAndUsedFalse는 사용되지 않은 토큰만 반환하므로 빈 결과
            given(emailVerificationRepository.findByCodeAndUsedFalse(token))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.verifyEmail(userId, token))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("유효하지 않은 인증 토큰");

            verify(emailVerificationRepository).findByCodeAndUsedFalse(token);
            verify(userRepository, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("인증 이메일 재발송")
    class ResendVerificationEmailTest {

        @Test
        @DisplayName("인증 이메일 재발송 성공")
        void resendVerificationEmail_Success() {
            // Given
            final Long userId = 1L;
            EmailVerification spyVerification = spy(emailVerification);
            List<EmailVerification> existingVerifications = List.of(spyVerification);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(emailVerificationRepository.findByUserIdAndUsedFalse(userId))
                    .willReturn(existingVerifications);
            given(emailVerificationRepository.save(any(EmailVerification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            String result = service.resendVerificationEmail(userId);

            // Then
            assertThat(result).contains("인증 이메일이 재발송되었습니다");

            verify(userRepository).findById(userId);
            verify(emailVerificationRepository).findByUserIdAndUsedFalse(userId);

            // 기존 토큰 만료 처리 확인
            verify(spyVerification).markAsUsed();

            // 새 토큰 생성 및 저장 확인
            verify(emailVerificationRepository, times(2)).save(any(EmailVerification.class));

            // 이메일 발송 확인
            verify(emailService).sendSignupVerificationEmail(eq(user), anyString());
        }

        @Test
        @DisplayName("이미 인증된 사용자에게 재발송 실패")
        void resendVerificationEmail_AlreadyVerified_ThrowsIllegalArgumentException() {
            // Given
            final Long userId = 1L;
            User verifiedUser = User.builder()
                    .username("verified")
                    .password("password")
                    .name("Verified User")
                    .email("verified@example.com")
                    .build();
            ReflectionTestUtils.setField(verifiedUser, "id", userId);
            ReflectionTestUtils.setField(verifiedUser, "emailVerified", true);

            given(userRepository.findById(userId)).willReturn(Optional.of(verifiedUser));

            // When & Then
            assertThatThrownBy(() -> service.resendVerificationEmail(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 인증된 사용자");

            verify(userRepository).findById(userId);
            verify(emailVerificationRepository, never()).findByUserIdAndUsedFalse(anyLong());
            verify(emailService, never()).sendSignupVerificationEmail(any(), anyString());
        }

        @Test
        @DisplayName("존재하지 않는 사용자에게 재발송 실패")
        void resendVerificationEmail_UserNotFound_ThrowsResourceNotFoundException() {
            // Given
            final Long userId = 999L;

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.resendVerificationEmail(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(userId);
            verify(emailVerificationRepository, never()).findByUserIdAndUsedFalse(anyLong());
            verify(emailService, never()).sendSignupVerificationEmail(any(), anyString());
        }

        @Test
        @DisplayName("기존 토큰이 없는 경우에도 재발송 성공")
        void resendVerificationEmail_NoExistingTokens_Success() {
            // Given
            final Long userId = 1L;
            List<EmailVerification> emptyList = List.of();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(emailVerificationRepository.findByUserIdAndUsedFalse(userId))
                    .willReturn(emptyList);
            given(emailVerificationRepository.save(any(EmailVerification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            String result = service.resendVerificationEmail(userId);

            // Then
            assertThat(result).contains("인증 이메일이 재발송되었습니다");

            verify(userRepository).findById(userId);
            verify(emailVerificationRepository).findByUserIdAndUsedFalse(userId);

            // 새 토큰 생성 및 저장 확인 (기존 토큰 만료 처리 없음)
            verify(emailVerificationRepository, times(1)).save(any(EmailVerification.class));

            // 이메일 발송 확인
            verify(emailService).sendSignupVerificationEmail(eq(user), anyString());
        }

        @Test
        @DisplayName("여러 개의 기존 토큰 만료 처리")
        void resendVerificationEmail_MultipleExistingTokens_AllMarkedAsUsed() {
            // Given
            final Long userId = 1L;

            EmailVerification verification1 = mock(EmailVerification.class);
            EmailVerification verification2 = mock(EmailVerification.class);
            EmailVerification verification3 = mock(EmailVerification.class);

            List<EmailVerification> existingVerifications = List.of(
                    verification1, verification2, verification3
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(emailVerificationRepository.findByUserIdAndUsedFalse(userId))
                    .willReturn(existingVerifications);
            given(emailVerificationRepository.save(any(EmailVerification.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // When
            String result = service.resendVerificationEmail(userId);

            // Then
            assertThat(result).contains("인증 이메일이 재발송되었습니다");

            // 모든 기존 토큰이 만료 처리되었는지 확인
            verify(verification1).markAsUsed();
            verify(verification2).markAsUsed();
            verify(verification3).markAsUsed();

            // 3개의 기존 토큰 만료 + 1개의 새 토큰 = 총 4번 save 호출
            verify(emailVerificationRepository, times(4)).save(any(EmailVerification.class));
        }
    }
}