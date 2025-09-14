package bunny.boardhole.user.application;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.exception.UnauthorizedException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.command.CreateUserCommand;
import bunny.boardhole.user.application.command.UpdatePasswordCommand;
import bunny.boardhole.user.application.command.UpdateUserCommand;
import bunny.boardhole.user.application.command.UserCommandService;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

@DisplayName("사용자 커맨드 서비스 단위 테스트")
@Tag("unit")
@Tag("user")
class UserCommandServiceTest {

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final String USERNAME = "john";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String NAME = "name";
    private static final String OLD_NAME = "old";
    private static final String NEW_NAME = "new";
    private static final String EMAIL = "john@example.com";
    private static final String WRONG_PASSWORD = "wrong";
    private static final String NEW_PASSWORD = "newPass123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserCommandService userCommandService;

    private AutoCloseable closeable;

    private static User user() {
        return userWithName(NAME);
    }

    private static User userWithName(String name) {
        User user = User.builder().username(USERNAME).password(ENCODED_PASSWORD).name(name).email(EMAIL).roles(Set.of(Role.USER)).build();
        user.verifyEmail();
        return user;
    }

    private static UserResult userResult() {
        return userResultWithName(NAME);
    }

    private static UserResult userResultWithName(String name) {
        // Suppress null warning: test record with null timestamps for testing purposes
        @SuppressWarnings("DataFlowIssue") UserResult result = new UserResult(USER_ID, USERNAME, name, EMAIL, null, null, null, Set.of(Role.USER));
        return result;
    }

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Spring LocaleContextHolder를 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREAN);

        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null)
            closeable.close();
    }

    @Nested
    @DisplayName("사용자 생성")
    @Tag("create")
    class CreateUser {

        @Test
        @DisplayName("✅ 신규 사용자 생성 성공")
        void shouldCreateUser() {
            // given
            CreateUserCommand cmd = new CreateUserCommand(UserCommandServiceTest.USERNAME, UserCommandServiceTest.RAW_PASSWORD,
                    UserCommandServiceTest.NAME, UserCommandServiceTest.EMAIL);

            given(passwordEncoder.encode(UserCommandServiceTest.RAW_PASSWORD)).willReturn(
                    UserCommandServiceTest.ENCODED_PASSWORD);

            User saved = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(saved, "id", UserCommandServiceTest.USER_ID);

            given(userRepository.save(any(User.class))).willReturn(saved);

            UserResult expected = UserCommandServiceTest.userResult();
            given(userMapper.toResult(saved)).willReturn(expected);

            // when
            UserResult result = userCommandService.create(cmd);

            // then
            assertThat(result).isEqualTo(expected);
            then(userRepository).should().save(any(User.class));
            then(userMapper).should().toResult(saved);
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

            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.of(existing));
            given(userRepository.save(any(User.class))).willReturn(existing);

            UserResult expected = UserCommandServiceTest.userResultWithName(UserCommandServiceTest.NEW_NAME);
            given(userMapper.toResult(existing)).willReturn(expected);

            UpdateUserCommand cmd = new UpdateUserCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.NEW_NAME);

            // when
            UserResult result = userCommandService.update(cmd);

            // then
            assertThat(result.name()).isEqualTo(UserCommandServiceTest.NEW_NAME);
            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(userRepository).should().save(existing);
            then(userMapper).should().toResult(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenUserNotFound() {
            // given
            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.empty());

            UpdateUserCommand cmd = new UpdateUserCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.NEW_NAME);

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.not-found.id", UserCommandServiceTest.USER_ID);

            // when & then
            assertThatThrownBy(() -> userCommandService.update(cmd))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용과 파라미터 치환 확인
            assertThat(expectedMessage).contains("사용자를 찾을 수 없습니다. ID:");

            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(userRepository).should(never()).save(any());
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

            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.of(existing));

            // when
            userCommandService.delete(UserCommandServiceTest.USER_ID);

            // then
            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(userRepository).should().delete(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenDeletingMissingUser() {
            // given
            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.not-found.id", UserCommandServiceTest.USER_ID);

            // when & then
            assertThatThrownBy(() -> userCommandService.delete(UserCommandServiceTest.USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용 확인
            assertThat(expectedMessage).contains("사용자를 찾을 수 없습니다. ID:");

            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(userRepository).should(never()).delete(any());
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

            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.of(existing));
            given(userRepository.save(existing)).willReturn(existing);

            // when
            userCommandService.updateLastLogin(UserCommandServiceTest.USER_ID);

            // then
            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(userRepository).should().save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenUpdatingLastLoginMissingUser() {
            // given
            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.not-found.id", UserCommandServiceTest.USER_ID);

            // when & then
            assertThatThrownBy(() -> userCommandService.updateLastLogin(UserCommandServiceTest.USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용 확인
            assertThat(expectedMessage).contains("사용자를 찾을 수 없습니다. ID:");

            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    @Tag("password")
    class UpdatePassword {

        @Test
        @DisplayName("❌ 현재 비밀번호 불일치 → UnauthorizedException with 국제화 메시지")
        void shouldThrowWhenCurrentPasswordMismatch() {
            // given
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.of(existing));
            given(passwordEncoder.matches(UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD)).willReturn(false);

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.WRONG_PASSWORD,
                    UserCommandServiceTest.NEW_PASSWORD, UserCommandServiceTest.NEW_PASSWORD);

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.password.current.mismatch");

            // when & then
            assertThatThrownBy(() -> userCommandService.updatePassword(cmd))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용 확인
            assertThat(expectedMessage).isEqualTo("현재 패스워드가 일치하지 않습니다");

            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(passwordEncoder).should().matches(UserCommandServiceTest.WRONG_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD);
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("✅ 비밀번호 변경 성공")
        void shouldUpdatePassword() {
            // given
            User existing = UserCommandServiceTest.user();
            ReflectionTestUtils.setField(existing, "id", UserCommandServiceTest.USER_ID);

            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.of(existing));
            given(passwordEncoder.matches(UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD)).willReturn(true);
            given(passwordEncoder.encode(UserCommandServiceTest.NEW_PASSWORD)).willReturn(UserCommandServiceTest.ENCODED_PASSWORD);
            given(userRepository.save(existing)).willReturn(existing);

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.RAW_PASSWORD,
                    UserCommandServiceTest.NEW_PASSWORD, UserCommandServiceTest.NEW_PASSWORD);

            // when
            userCommandService.updatePassword(cmd);

            // then
            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(passwordEncoder).should().matches(UserCommandServiceTest.RAW_PASSWORD, UserCommandServiceTest.ENCODED_PASSWORD);
            then(passwordEncoder).should().encode(UserCommandServiceTest.NEW_PASSWORD);
            then(userRepository).should().save(existing);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenUserNotFoundForPassword() {
            // given
            given(userRepository.findById(UserCommandServiceTest.USER_ID)).willReturn(Optional.empty());

            UpdatePasswordCommand cmd = new UpdatePasswordCommand(UserCommandServiceTest.USER_ID, UserCommandServiceTest.RAW_PASSWORD,
                    UserCommandServiceTest.NEW_PASSWORD, UserCommandServiceTest.NEW_PASSWORD);

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.not-found.id", UserCommandServiceTest.USER_ID);

            // when & then
            assertThatThrownBy(() -> userCommandService.updatePassword(cmd))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용 확인
            assertThat(expectedMessage).contains("사용자를 찾을 수 없습니다. ID:");

            then(userRepository).should().findById(UserCommandServiceTest.USER_ID);
            then(passwordEncoder).should(never()).matches(any(), any());
            then(userRepository).should(never()).save(any());
        }
    }

}
