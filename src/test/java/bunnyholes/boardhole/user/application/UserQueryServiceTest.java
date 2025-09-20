package dev.xiyo.bunnyholes.boardhole.user.application;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.test.FixedKoreanLocaleExtension;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.mapper.UserMapper;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, FixedKoreanLocaleExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("사용자 조회 서비스 단위 테스트")
@Tag("unit")
@Tag("user")
class UserQueryServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USERNAME = "john";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String NAME = "name";
    private static final String NEW_NAME = "new";
    private static final String EMAIL = "john@example.com";
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    private UserQueryService userQueryService;

    private static User user() {
        return userWithName(NAME);
    }

    private static User userWithName(String name) {
        User user = User.builder().username(USERNAME).password(ENCODED_PASSWORD).name(name).email(EMAIL).roles(Set.of(Role.USER)).build();
        user.verifyEmail();
        // JPA에서 @GeneratedValue는 저장시에 생성되므로 테스트에서는 수동으로 ID 설정
        ReflectionTestUtils.setField(user, "id", USER_ID);
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
        // Spring LocaleContextHolder를 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREAN);

        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        MessageUtils.setMessageSource(ms);

        userQueryService = new UserQueryService(userRepository, userMapper);
    }

    @Nested
    @DisplayName("사용자 단일 조회")
    @Tag("read")
    class GetUser {

        @Test
        @DisplayName("✅ ID로 사용자 조회 성공")
        void shouldGetUserById() {
            // given
            User user = UserQueryServiceTest.user();
            ReflectionTestUtils.setField(user, "id", UserQueryServiceTest.USER_ID);

            when(userRepository.findById(UserQueryServiceTest.USER_ID)).thenReturn(Optional.of(user));
            UserResult expected = UserQueryServiceTest.userResult();
            when(userMapper.toResult(user)).thenReturn(expected);

            // when
            UserResult result = userQueryService.get(UserQueryServiceTest.USER_ID);

            // then
            assertThat(result).isEqualTo(expected);
            verify(userRepository).findById(UserQueryServiceTest.USER_ID);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(UserQueryServiceTest.USER_ID)).thenReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.not-found.id", UserQueryServiceTest.USER_ID);

            // when & then
            assertThatThrownBy(() -> userQueryService.get(UserQueryServiceTest.USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용 확인
            assertThat(expectedMessage).contains("사용자를 찾을 수 없습니다. ID:");
        }

        @Test
        @DisplayName("🌍 다국어 메시지 검증 - 한국어/영어")
        void shouldReturnCorrectMessageByLocale() {
            // given
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // 한국어 테스트
            LocaleContextHolder.setLocale(Locale.KOREAN);
            ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
            ms.setBasename("messages");
            ms.setDefaultEncoding("UTF-8");
            ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

            String koreanMessage = MessageUtils.get("error.user.not-found.id", userId);
            assertThat(koreanMessage).contains("사용자를 찾을 수 없습니다. ID:");

            assertThatThrownBy(() -> userQueryService.get(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(koreanMessage);

            // 영어 테스트
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            ms = new ResourceBundleMessageSource();
            ms.setBasename("messages");
            ms.setDefaultEncoding("UTF-8");
            ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

            String englishMessage = MessageUtils.get("error.user.not-found.id", userId);
            assertThat(englishMessage).contains("User not found. ID:");

            assertThatThrownBy(() -> userQueryService.get(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(englishMessage);
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회")
    @Tag("list")
    class ListUsers {

        @Test
        @DisplayName("📄 페이지네이션으로 전체 사용자 조회")
        void shouldListUsersWithPaging() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            User user = UserQueryServiceTest.user();
            ReflectionTestUtils.setField(user, "id", UserQueryServiceTest.USER_ID);
            User another = UserQueryServiceTest.userWithName(UserQueryServiceTest.NEW_NAME);
            ReflectionTestUtils.setField(another, "id", UUID.randomUUID());

            Page<User> page = new PageImpl<>(List.of(user, another));
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.toResult(user)).thenReturn(UserQueryServiceTest.userResult());
            when(userMapper.toResult(another)).thenReturn(UserQueryServiceTest.userResultWithName(UserQueryServiceTest.NEW_NAME));

            // when
            Page<UserResult> result = userQueryService.listWithPaging(pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(userRepository).findAll(pageable);
        }

        @Test
        @DisplayName("📄 결과 없음 시 빈 페이지 반환")
        void shouldReturnEmptyPageWhenNoUsers() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

            // when
            Page<UserResult> result = userQueryService.listWithPaging(pageable);

            // then
            assertThat(result).isEmpty();
            verify(userRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("사용자 검색")
    @Tag("search")
    class SearchUsers {

        @Test
        @DisplayName("🔍 검색어로 사용자 목록 조회 성공")
        void shouldSearchUsersWithPaging() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            User user = UserQueryServiceTest.user();
            ReflectionTestUtils.setField(user, "id", UserQueryServiceTest.USER_ID);

            Page<User> page = new PageImpl<>(List.of(user));
            when(userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(UserQueryServiceTest.USERNAME,
                    UserQueryServiceTest.USERNAME, UserQueryServiceTest.USERNAME, pageable)).thenReturn(page);
            UserResult mapped = UserQueryServiceTest.userResult();
            when(userMapper.toResult(user)).thenReturn(mapped);

            // when
            Page<UserResult> result = userQueryService.listWithPaging(pageable, UserQueryServiceTest.USERNAME);

            // then
            assertThat(result.getContent()).containsExactly(mapped);
            verify(userRepository).findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    UserQueryServiceTest.USERNAME, UserQueryServiceTest.USERNAME, UserQueryServiceTest.USERNAME, pageable);
        }

        @Test
        @DisplayName("🔍 검색 결과 없음 → 빈 페이지")
        void shouldReturnEmptyPageWhenNoSearchResults() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(UserQueryServiceTest.USERNAME,
                    UserQueryServiceTest.USERNAME, UserQueryServiceTest.USERNAME, pageable)).thenReturn(Page.empty(pageable));

            // when
            Page<UserResult> result = userQueryService.listWithPaging(pageable, UserQueryServiceTest.USERNAME);

            // then
            assertThat(result).isEmpty();
            verify(userRepository).findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    UserQueryServiceTest.USERNAME, UserQueryServiceTest.USERNAME, UserQueryServiceTest.USERNAME, pageable);
        }
    }
}
