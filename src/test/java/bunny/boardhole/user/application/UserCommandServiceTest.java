package bunny.boardhole.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import bunny.boardhole.shared.exception.*;
import bunny.boardhole.shared.util.*;
import bunny.boardhole.user.application.command.*;
import bunny.boardhole.user.application.event.UserCreatedEvent;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.*;

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
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private VerificationCodeGenerator verificationCodeGenerator;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    private UserCommandService userCommandService;

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

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

        userCommandService = new UserCommandService(
                userRepository,
                emailVerificationRepository,
                passwordEncoder,
                userMapper,
                verificationCodeGenerator,
                eventPublisher
        );
    }

    @Nested
    @DisplayName("사용자 생성")
    @Tag("create")
    class CreateUser {

        @Test
        @DisplayName("✅ 신규 사용자 생성 성공")
        void shouldCreateUser() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(UserCommandServiceTest.USERNAME, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NAME, UserCommandServiceTest.EMAIL);

            lenient().when(userRepository.existsByUsername(UserCommandServiceTest.USERNAME)).thenReturn(false);
            lenient().when(userRepository.existsByEmail(UserCommandServiceTest.EMAIL)).thenReturn(false);
            lenient().when(passwordEncoder.encode(UserCommandServiceTest.RAW_PASSWORD)).thenReturn(UserCommandServiceTest.ENCODED_PASSWORD);

            User saved = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(saved, "id", UserCommandServiceTest.USER_ID);

            lenient().when(userRepository.save(any(User.class))).thenReturn(saved);

            UserResult expected = UserCommandServiceTest.userResult();
            lenient().when(userMapper.toResult(saved)).thenReturn(expected);

            // 이벤트 생성 메서드 mock 설정
            UserCreatedEvent mockEvent = new UserCreatedEvent(saved, "test-token", java.time.LocalDateTime.now());
            lenient().when(userMapper.toUserCreatedEvent(any(User.class), anyString(), any(java.time.LocalDateTime.class)))
                    .thenReturn(mockEvent);

            // when
            UserResult result = userCommandService.create(cmd);

            // then
            assertThat(result).isEqualTo(expected);
            verify(userRepository).existsByUsername(UserCommandServiceTest.USERNAME);
            verify(userRepository).existsByEmail(UserCommandServiceTest.EMAIL);

            // 이벤트 발행 검증
            ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            UserCreatedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.user()).isEqualTo(saved);
            assertThat(publishedEvent.verificationToken()).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(emailVerificationRepository).save(any(EmailVerification.class));
        }

        @Test
        @DisplayName("❌ 사용자명 중복 → DuplicateUsernameException")
        void shouldThrowWhenUsernameExists() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(UserCommandServiceTest.USERNAME, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NAME, UserCommandServiceTest.EMAIL);

            when(userRepository.existsByUsername(UserCommandServiceTest.USERNAME)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userCommandService.create(cmd))
                    .isInstanceOf(DuplicateUsernameException.class);
            verify(userRepository).existsByUsername(UserCommandServiceTest.USERNAME);
        }

        @Test
        @DisplayName("❌ 이메일 중복 → DuplicateEmailException")
        void shouldThrowWhenEmailExists() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(UserCommandServiceTest.USERNAME, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NAME, UserCommandServiceTest.EMAIL);

            when(userRepository.existsByUsername(UserCommandServiceTest.USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(UserCommandServiceTest.EMAIL)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userCommandService.create(cmd))
                    .isInstanceOf(DuplicateEmailException.class);
            verify(userRepository).existsByUsername(UserCommandServiceTest.USERNAME);
            verify(userRepository).existsByEmail(UserCommandServiceTest.EMAIL);
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
            User existing = UserCommandServiceTest.userWithName(UserCommandServiceTest.OLD_NAME);
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(existing);

            UserResult expected = UserCommandServiceTest.userResultWithName(UserCommandServiceTest.NEW_NAME);
            when(userMapper.toResult(existing)).thenReturn(expected);

            UpdateUserCommand cmd = new UpdateUserCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.NEW_NAME);

            // when
            UserResult result = userCommandService.update(cmd);

            // then
            assertThat(result.name()).isEqualTo(UserCommandServiceTest.NEW_NAME);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.empty());

            UpdateUserCommand cmd = new UpdateUserCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.NEW_NAME);

            // when & then
            assertThatThrownBy(() -> userCommandService.update(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
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
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));

            // when
            userCommandService.delete(UserCommandServiceTest.USER_ID);

            // then
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
            verify(userRepository).delete(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenDeletingMissingUser() {
            // given
            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.delete(UserCommandServiceTest.USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
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
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(existing)).thenReturn(existing);

            // when
            userCommandService.updateLastLogin(UserCommandServiceTest.USER_ID);

            // then
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUpdatingLastLoginMissingUser() {
            // given
            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.updateLastLogin(UserCommandServiceTest.USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
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
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD)).thenReturn(false);

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.NEW_PASSWORD);

            // when & then
            assertThatThrownBy(() -> userCommandService.updatePassword(cmd))
                    .isInstanceOf(UnauthorizedException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
            verify(passwordEncoder).matches(UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("✅ 비밀번호 변경 성공")
        void shouldUpdatePassword() {
            // given
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD)).thenReturn(true);
            when(passwordEncoder.encode(UserCommandServiceTest.NEW_PASSWORD)).thenReturn(UserCommandServiceTest.ENCODED_PASSWORD);
            when(userRepository.save(existing)).thenReturn(existing);

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NEW_PASSWORD);

            // when
            userCommandService.updatePassword(cmd);

            // then
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
            verify(passwordEncoder).matches(UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD);
            verify(passwordEncoder).encode(UserCommandServiceTest.NEW_PASSWORD);
            verify(userRepository).save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserNotFoundForPassword() {
            // given
            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.empty());

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NEW_PASSWORD);

            // when & then
            assertThatThrownBy(() -> userCommandService.updatePassword(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
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
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD)).thenReturn(false);

            RequestEmailVerificationCommand cmd = new RequestEmailVerificationCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.NEW_EMAIL);

            // when & then
            assertThatThrownBy(() -> userCommandService.requestEmailVerification(cmd))
                    .isInstanceOf(UnauthorizedException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
            verify(passwordEncoder).matches(UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("❌ 이메일 중복 → DuplicateEmailException")
        void shouldThrowWhenNewEmailExists() {
            // given
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(existing));
            when(passwordEncoder.matches(UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD)).thenReturn(true);
            when(userRepository.existsByEmail(UserCommandServiceTest.NEW_EMAIL)).thenReturn(true);

            RequestEmailVerificationCommand cmd = new RequestEmailVerificationCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NEW_EMAIL);

            // when & then
            assertThatThrownBy(() -> userCommandService.requestEmailVerification(cmd))
                    .isInstanceOf(DuplicateEmailException.class);
            verify(userRepository).existsByEmail(UserCommandServiceTest.NEW_EMAIL);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserMissingForVerification() {
            // given
            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.empty());

            RequestEmailVerificationCommand cmd = new RequestEmailVerificationCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.NEW_EMAIL);

            // when & then
            assertThatThrownBy(() -> userCommandService.requestEmailVerification(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
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
            User user = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(user, "id", UserCommandServiceTest.USER_ID);
            EmailVerification verification = UserCommandServiceTest.emailVerification();

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(user));
            when(emailVerificationRepository.findValidVerification(eq(UserCommandServiceTest.USER_ID), eq(UserCommandServiceTest.VERIFICATION_CODE), any()))
                    .thenReturn(Optional.of(verification));
            when(userRepository.save(user)).thenReturn(user);
            UserResult expected = UserCommandServiceTest.userResult();
            when(userMapper.toResult(user)).thenReturn(expected);

            UpdateEmailCommand cmd = new UpdateEmailCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.VERIFICATION_CODE);

            // when
            UserResult result = userCommandService.updateEmail(cmd);

            // then
            assertThat(result).isEqualTo(expected);
            assertThat(user.getEmail()).isEqualTo(UserCommandServiceTest.NEW_EMAIL);
            assertThat(verification.isUsed()).isTrue();
            verify(emailVerificationRepository).save(verification);
        }

        @Test
        @DisplayName("❌ 검증 코드 무효 → ValidationException")
        void shouldThrowWhenVerificationInvalid() {
            // given
            User user = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(user, "id", UserCommandServiceTest.USER_ID);

            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.of(user));
            when(emailVerificationRepository.findValidVerification(eq(UserCommandServiceTest.USER_ID), eq(UserCommandServiceTest.VERIFICATION_CODE), any()))
                    .thenReturn(Optional.empty());

            UpdateEmailCommand cmd = new UpdateEmailCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.VERIFICATION_CODE);

            // when & then
            assertThatThrownBy(() -> userCommandService.updateEmail(cmd))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserMissingForEmailUpdate() {
            // given
            when(userRepository.findById(UserCommandServiceTest.USER_ID)).thenReturn(Optional.empty());

            UpdateEmailCommand cmd = new UpdateEmailCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.VERIFICATION_CODE);

            // when & then
            assertThatThrownBy(() -> userCommandService.updateEmail(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository).findById(UserCommandServiceTest.USER_ID);
        }
    }
}
