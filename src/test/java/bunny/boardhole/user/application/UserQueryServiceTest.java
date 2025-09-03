package bunny.boardhole.user.application;

import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.query.UserQueryService;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.infrastructure.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("사용자 조회 서비스 단위 테스트")
@Tag("unit")
@Tag("user")
class UserQueryServiceTest {

    private static final long USER_ID = 1L;
    private static final String USERNAME = "john";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String NAME = "name";
    private static final String NEW_NAME = "new";
    private static final String EMAIL = "john@example.com";
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

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MessageUtils messageUtils;

    private UserQueryService userQueryService;

    @BeforeEach
    void setUp() {
        userQueryService = new UserQueryService(userRepository, userMapper, messageUtils);
    }

    @Nested
    @DisplayName("사용자 단일 조회")
    @Tag("read")
    class GetUser {

        @Test
        @DisplayName("✅ ID로 사용자 조회 성공")
        void shouldGetUserById() {
            // given
            User user = user();
            ReflectionTestUtils.setField(user, "id", USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            UserResult expected = userResult();
            when(userMapper.toResult(user)).thenReturn(expected);

            // when
            UserResult result = userQueryService.get(USER_ID);

            // then
            assertThat(result).isEqualTo(expected);
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → ResourceNotFoundException")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
            when(messageUtils.getMessage(anyString(), any())).thenReturn(MESSAGE);

            // when & then
            assertThatThrownBy(() -> userQueryService.get(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
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
            User user = user();
            ReflectionTestUtils.setField(user, "id", USER_ID);
            User another = userWithName(NEW_NAME);
            ReflectionTestUtils.setField(another, "id", USER_ID + 1);

            Page<User> page = new PageImpl<>(List.of(user, another));
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.toResult(user)).thenReturn(userResult());
            when(userMapper.toResult(another)).thenReturn(userResultWithName(NEW_NAME));

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
            User user = user();
            ReflectionTestUtils.setField(user, "id", USER_ID);

            Page<User> page = new PageImpl<>(List.of(user));
            when(userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    USERNAME, USERNAME, USERNAME, pageable)).thenReturn(page);
            UserResult mapped = userResult();
            when(userMapper.toResult(user)).thenReturn(mapped);

            // when
            Page<UserResult> result = userQueryService.listWithPaging(pageable, USERNAME);

            // then
            assertThat(result.getContent()).containsExactly(mapped);
            verify(userRepository).findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    USERNAME, USERNAME, USERNAME, pageable);
        }

        @Test
        @DisplayName("🔍 검색 결과 없음 → 빈 페이지")
        void shouldReturnEmptyPageWhenNoSearchResults() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    USERNAME, USERNAME, USERNAME, pageable)).thenReturn(Page.empty(pageable));

            // when
            Page<UserResult> result = userQueryService.listWithPaging(pageable, USERNAME);

            // then
            assertThat(result).isEmpty();
            verify(userRepository).findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    USERNAME, USERNAME, USERNAME, pageable);
        }
    }
}

