package bunny.boardhole.user.application;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.shared.config.properties.ValidationProperties;
import bunny.boardhole.shared.exception.*;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.shared.util.VerificationCodeGenerator;
import bunny.boardhole.user.application.command.*;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.EmailVerification;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.EmailVerificationType;
import bunny.boardhole.user.infrastructure.EmailVerificationRepository;
import bunny.boardhole.user.infrastructure.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("사용자 커맨드 서비스 단위 테스트")
@Tag("unit")
@Tag("user")
class UserCommandServiceTest {

    private static final long USER_ID = 1L;
    private static final String USERNAME = "john";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String NAME = "name";
    private static final String OLD_NAME = "old";
    private static final String NEW_NAME = "new";
    private static final String EMAIL = "john@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String WRONG_PASSWORD = "wrong";
    private static final String NEW_PASSWORD = "newPass123";
    private static final String VERIFICATION_CODE = "verify";
    private static final String MESSAGE = "msg";

    private static User user() {
        return userWithName(NAME);
    }

    private static User userWithName(String name) {
        return User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .name(name)
                .email(EMAIL)
                .roles(Set.of(Role.USER))
                .build();
    }

    private static UserResult userResult() {
        return userResultWithName(NAME);
    }

    private static UserResult userResultWithName(String name) {
        return new UserResult(USER_ID, USERNAME, name, EMAIL, null, null, null, Set.of(Role.USER));
    }

    private static EmailVerification emailVerification() {
        return EmailVerification.builder()
                .code(VERIFICATION_CODE)
                .userId(USER_ID)
                .newEmail(NEW_EMAIL)
                .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(10))
                .verificationType(EmailVerificationType.CHANGE_EMAIL)
                .build();
    }

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MessageUtils messageUtils;
    @Mock
    private VerificationCodeGenerator verificationCodeGenerator;
    @Mock
    private EmailService emailService;

    private ValidationProperties validationProperties;
    private UserCommandService userCommandService;

    @BeforeEach
    void setUp() {
        validationProperties = new ValidationProperties();
        userCommandService = new UserCommandService(
                userRepository,
                emailVerificationRepository,
                passwordEncoder,
                userMapper,
                messageUtils,
                validationProperties,
                verificationCodeGenerator,
                emailService
        );

        when(messageUtils.getMessage(anyString(), any())).thenReturn(MESSAGE);
    }

    @Nested
    @DisplayName("사용자 생성")
    @Tag("create")
    class CreateUser {

        @Test
        @DisplayName("✅ 신규 사용자 생성 성공")
        void shouldCreateUser() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(USERNAME, RAW_PASSWORD, NAME, EMAIL);

            lenient().when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            lenient().when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            lenient().when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            User saved = user();
            ReflectionTestUtils.setField(saved, "id", USER_ID);

            lenient().when(userRepository.save(any(User.class))).thenReturn(saved);

            UserResult expected = userResult();
            lenient().when(userMapper.toResult(saved)).thenReturn(expected);

            // when
            UserResult result = userCommandService.create(cmd);

            // then
            assertThat(result).isEqualTo(expected);
            verify(userRepository).existsByUsername(USERNAME);
            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository).save(any(User.class));
            verify(emailVerificationRepository).save(any(EmailVerification.class));
            verify(emailService).sendSignupVerificationEmail(eq(saved), anyString());
        }

        @Test
        @DisplayName("❌ 사용자명 중복 → DuplicateUsernameException")
        void shouldThrowWhenUsernameExists() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(USERNAME, RAW_PASSWORD, NAME, EMAIL);

            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userCommandService.create(cmd))
                    .isInstanceOf(DuplicateUsernameException.class);
            verify(userRepository).existsByUsername(USERNAME);
        }

        @Test
        @DisplayName("❌ 이메일 중복 → DuplicateEmailException")
        void shouldThrowWhenEmailExists() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(USERNAME, RAW_PASSWORD, NAME, EMAIL);

            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userCommandService.create(cmd))
                    .isInstanceOf(DuplicateEmailException.class);
            verify(userRepository).existsByUsername(USERNAME);
            verify(userRepository).existsByEmail(EMAIL);
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정")
    @Tag("update")
    class UpdateUser {

        @Test
        @DisplayName("✅ 이름 변경 성공")
        void shouldUpdateName() {
            // given
            User existing = userWithName(OLD_NAME);
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(existing);

            UserResult expected = userResultWithName(NEW_NAME);
            when(userMapper.toResult(existing)).thenReturn(expected);

            UpdateUserCommand cmd = new UpdateUserCommand(USER_ID, NEW_NAME);

            // when
            UserResult result = userCommandService.update(cmd);

            // then
            assertThat(result.name()).isEqualTo(NEW_NAME);
            verify(userRepository).findById(USER_ID);
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            UpdateUserCommand cmd = new UpdateUserCommand(USER_ID, NEW_NAME);

            // when & then
            assertThatThrownBy(() -> userCommandService.update(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    @Tag("delete")
    class DeleteUser {

        @Test
        @DisplayName("✅ 사용자 삭제 성공")
        void shouldDeleteUser() {
            // given
            User existing = user();
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));

            // when
            userCommandService.delete(USER_ID);

            // then
            verify(userRepository).findById(USER_ID);
            verify(userRepository).delete(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenDeletingMissingUser() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.delete(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("마지막 로그인 업데이트")
    @Tag("login")
    class UpdateLastLogin {

        @Test
        @DisplayName("✅ 마지막 로그인 시간 갱신 성공")
        void shouldUpdateLastLogin() {
            // given
            User existing = user();
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(existing)).thenReturn(existing);

            // when
            userCommandService.updateLastLogin(USER_ID);

            // then
            verify(userRepository).findById(USER_ID);
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUpdatingLastLoginMissingUser() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.updateLastLogin(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    @Tag("password")
    class UpdatePassword {

        @Test
        @DisplayName("❌ 현재 비밀번호 불일치 → Unauthorized")
        void shouldThrowWhenCurrentPasswordMismatch() {
            // given
            User existing = user();
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(USER_ID, WRONG_PASSWORD, NEW_PASSWORD);

            // when & then
            assertThatThrownBy(() -> userCommandService.updatePassword(cmd))
                    .isInstanceOf(UnauthorizedException.class);
            verify(userRepository).findById(USER_ID);
            verify(passwordEncoder).matches(WRONG_PASSWORD, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("✅ 비밀번호 변경 성공")
        void shouldUpdatePassword() {
            // given
            User existing = user();
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(existing)).thenReturn(existing);

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(USER_ID, RAW_PASSWORD, NEW_PASSWORD);

            // when
            userCommandService.updatePassword(cmd);

            // then
            verify(userRepository).findById(USER_ID);
            verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
            verify(passwordEncoder).encode(NEW_PASSWORD);
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserNotFoundForPassword() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(USER_ID, RAW_PASSWORD, NEW_PASSWORD);

            // when & then
            assertThatThrownBy(() -> userCommandService.updatePassword(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("이메일 변경 검증 요청")
    @Tag("email")
    class RequestEmailVerification {

        @Test
        @DisplayName("❌ 현재 비밀번호 불일치 → Unauthorized")
        void shouldThrowWhenPasswordMismatch() {
            // given
            User existing = user();
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            RequestEmailVerificationCommand cmd = new RequestEmailVerificationCommand(USER_ID, WRONG_PASSWORD, NEW_EMAIL);

            // when & then
            assertThatThrownBy(() -> userCommandService.requestEmailVerification(cmd))
                    .isInstanceOf(UnauthorizedException.class);
            verify(userRepository).findById(USER_ID);
            verify(passwordEncoder).matches(WRONG_PASSWORD, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("❌ 이메일 중복 → DuplicateEmailException")
        void shouldThrowWhenNewEmailExists() {
            // given
            User existing = user();
            ReflectionTestUtils.setField(existing, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

            RequestEmailVerificationCommand cmd = new RequestEmailVerificationCommand(USER_ID, RAW_PASSWORD, NEW_EMAIL);

            // when & then
            assertThatThrownBy(() -> userCommandService.requestEmailVerification(cmd))
                    .isInstanceOf(DuplicateEmailException.class);
            verify(userRepository).existsByEmail(NEW_EMAIL);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserMissingForVerification() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            RequestEmailVerificationCommand cmd = new RequestEmailVerificationCommand(USER_ID, RAW_PASSWORD, NEW_EMAIL);

            // when & then
            assertThatThrownBy(() -> userCommandService.requestEmailVerification(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("이메일 변경")
    @Tag("email")
    class UpdateEmail {

        @Test
        @DisplayName("✅ 이메일 변경 성공")
        void shouldUpdateEmail() {
            // given
            User user = user();
            ReflectionTestUtils.setField(user, "id", USER_ID);
            EmailVerification verification = emailVerification();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(emailVerificationRepository.findValidVerification(eq(USER_ID), eq(VERIFICATION_CODE), any()))
                    .thenReturn(Optional.of(verification));
            when(userRepository.save(user)).thenReturn(user);
            UserResult expected = userResult();
            when(userMapper.toResult(user)).thenReturn(expected);

            UpdateEmailCommand cmd = new UpdateEmailCommand(USER_ID, VERIFICATION_CODE);

            // when
            UserResult result = userCommandService.updateEmail(cmd);

            // then
            assertThat(result).isEqualTo(expected);
            assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(verification.isUsed()).isTrue();
            verify(emailVerificationRepository).save(verification);
        }

        @Test
        @DisplayName("❌ 검증 코드 무효 → ValidationException")
        void shouldThrowWhenVerificationInvalid() {
            // given
            User user = user();
            ReflectionTestUtils.setField(user, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(emailVerificationRepository.findValidVerification(eq(USER_ID), eq(VERIFICATION_CODE), any()))
                    .thenReturn(Optional.empty());

            UpdateEmailCommand cmd = new UpdateEmailCommand(USER_ID, VERIFICATION_CODE);

            // when & then
            assertThatThrownBy(() -> userCommandService.updateEmail(cmd))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserMissingForEmailUpdate() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            UpdateEmailCommand cmd = new UpdateEmailCommand(USER_ID, VERIFICATION_CODE);

            // when & then
            assertThatThrownBy(() -> userCommandService.updateEmail(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(USER_ID);
        }
    }
}

