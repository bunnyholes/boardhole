package bunny.boardhole.user.infrastructure;

import bunny.boardhole.shared.config.TestJpaConfig;
import bunny.boardhole.user.domain.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@Tag("integration")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        user1 = User.builder()
                .username("john_doe")
                .password("password123")
                .name("John Doe")
                .email("john@example.com")
                .build();
        // User는 기본적으로 USER 권한을 가짐
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .username("jane_smith")
                .password("password456")
                .name("Jane Smith")
                .email("jane@example.com")
                .build();
        user2.grantAdminRole(); // ADMIN 권한 추가
        user2 = userRepository.save(user2);

        user3 = User.builder()
                .username("bob_johnson")
                .password("password789")
                .name("Bob Johnson")
                .email("bob@example.com")
                .build();
        // User는 기본적으로 USER 권한을 가짐
        user3 = userRepository.save(user3);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("사용자 조회")
    class FindUserTest {

        @Test
        @DisplayName("ID로 사용자 조회 시 권한 정보 포함")
        void findById_ExistingUser_IncludesRoles() {
            // When
            Optional<User> found = userRepository.findById(user2.getId());

            // Then
            assertThat(found).isPresent();
            User user = found.get();
            assertThat(user.getUsername()).isEqualTo("jane_smith");
            assertThat(user.getName()).isEqualTo("Jane Smith");
            assertThat(user.hasAdminRole()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 결과")
        void findById_NonExistingUser_ReturnsEmpty() {
            // When
            Optional<User> found = userRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("사용자명으로 조회")
        void findByUsername_ExistingUser_ReturnsUser() {
            // When
            Optional<User> found = userRepository.findByUsername("john_doe");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("John Doe");
            assertThat(found.get().getEmail()).isEqualTo("john@example.com");
            // User는 기본적으로 권한을 가짐
        }

        @Test
        @DisplayName("존재하지 않는 사용자명으로 조회 시 빈 결과")
        void findByUsername_NonExistingUser_ReturnsEmpty() {
            // When
            Optional<User> found = userRepository.findByUsername("nonexistent");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Optional 사용자명으로 조회")
        void findOptionalByUsername_ExistingUser_ReturnsUser() {
            // When
            Optional<User> found = userRepository.findOptionalByUsername("jane_smith");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("이메일로 조회")
        void findByEmail_ExistingUser_ReturnsUser() {
            // When
            Optional<User> found = userRepository.findByEmail("bob@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("bob_johnson");
            assertThat(found.get().getName()).isEqualTo("Bob Johnson");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회 시 빈 결과")
        void findByEmail_NonExistingEmail_ReturnsEmpty() {
            // When
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("중복 확인")
    class ExistenceCheckTest {

        @Test
        @DisplayName("존재하는 사용자명 중복 확인")
        void existsByUsername_ExistingUsername_ReturnsTrue() {
            // When
            boolean exists = userRepository.existsByUsername("john_doe");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자명 중복 확인")
        void existsByUsername_NonExistingUsername_ReturnsFalse() {
            // When
            boolean exists = userRepository.existsByUsername("new_user");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("존재하는 이메일 중복 확인")
        void existsByEmail_ExistingEmail_ReturnsTrue() {
            // When
            boolean exists = userRepository.existsByEmail("jane@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이메일 중복 확인")
        void existsByEmail_NonExistingEmail_ReturnsFalse() {
            // When
            boolean exists = userRepository.existsByEmail("new@example.com");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 검색")
    class SearchUserTest {

        @Test
        @DisplayName("사용자명으로 검색")
        void searchByUsername_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    "john", "john", "john", pageable);

            // Then
            assertThat(page.getContent()).hasSize(2); // john_doe, bob_johnson
            assertThat(page.getContent())
                    .extracting(User::getUsername)
                    .containsExactlyInAnyOrder("john_doe", "bob_johnson");
        }

        @Test
        @DisplayName("이름으로 검색")
        void searchByName_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    "Smith", "Smith", "Smith", pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("이메일로 검색")
        void searchByEmail_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    "bob@", "bob@", "bob@", pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getEmail()).isEqualTo("bob@example.com");
        }

        @Test
        @DisplayName("대소문자 구분 없이 검색")
        void searchCaseInsensitive_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    "JANE", "JANE", "JANE", pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getUsername()).isEqualTo("jane_smith");
        }

        @Test
        @DisplayName("검색 결과 페이징 처리")
        void searchWithPaging_ReturnsPagedResults() {
            // Given - 더 많은 사용자 추가
            for (int i = 0; i < 5; i++) {
                User extraUser = User.builder()
                        .username("user_" + i)
                        .password("password")
                        .name("Test User " + i)
                        .email("user" + i + "@example.com")
                        .build();
                userRepository.save(extraUser);
            }

            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    "example", "example", "example", pageable);

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(8); // 기존 3개 + 추가 5개
            assertThat(page.getTotalPages()).isEqualTo(4);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void searchNoMatch_ReturnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    "nonexistent", "nonexistent", "nonexistent", pageable);

            // Then
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("사용자 CRUD 작업")
    class CrudOperationsTest {

        @Test
        @DisplayName("사용자 생성")
        void save_NewUser_CreatesSuccessfully() {
            // Given
            User newUser = User.builder()
                    .username("new_user")
                    .password("newpassword")
                    .name("New User")
                    .email("new@example.com")
                    .build();

            // When
            User saved = userRepository.save(newUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("new_user");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("사용자 수정")
        void save_ExistingUser_UpdatesSuccessfully() {
            // Given
            user1.changeName("John Updated");
            user1.changeEmail("john.updated@example.com");

            // When
            User updated = userRepository.save(user1);

            // Then
            assertThat(updated.getName()).isEqualTo("John Updated");
            assertThat(updated.getEmail()).isEqualTo("john.updated@example.com");
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
        }

        @Test
        @DisplayName("사용자 삭제")
        void delete_ExistingUser_RemovesSuccessfully() {
            // Given
            Long userId = user1.getId();

            // When
            userRepository.delete(user1);

            // Then
            Optional<User> found = userRepository.findById(userId);
            assertThat(found).isEmpty();
            assertThat(userRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("중복된 사용자명으로 생성 실패")
        void save_DuplicateUsername_ThrowsException() {
            // Given
            User duplicateUser = User.builder()
                    .username("john_doe") // 이미 존재하는 사용자명
                    .password("password")
                    .name("Another John")
                    .email("another@example.com")
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                userRepository.save(duplicateUser);
                userRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("중복된 이메일로 생성 실패")
        void save_DuplicateEmail_ThrowsException() {
            // Given
            User duplicateUser = User.builder()
                    .username("another_user")
                    .password("password")
                    .name("Another User")
                    .email("john@example.com") // 이미 존재하는 이메일
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                userRepository.save(duplicateUser);
                userRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("권한 관리")
    class RoleManagementTest {

        @Test
        @DisplayName("권한 추가")
        void addRole_NewRole_AddsSuccessfully() {
            // Given
            user1.grantAdminRole();

            // When
            User updated = userRepository.save(user1);

            // Then
            Optional<User> found = userRepository.findById(user1.getId());
            assertThat(found).isPresent();
            assertThat(found.get().hasAdminRole()).isTrue();
        }

        @Test
        @DisplayName("권한 제거")
        void removeRole_ExistingRole_RemovesSuccessfully() {
            // Given
            user2.revokeAdminRole();

            // When
            User updated = userRepository.save(user2);

            // Then
            Optional<User> found = userRepository.findById(user2.getId());
            assertThat(found).isPresent();
            assertThat(found.get().hasAdminRole()).isFalse();
            // User는 기본적으로 권한을 가짐
        }

        @Test
        @DisplayName("모든 권한 조회")
        void findById_UserWithMultipleRoles_LoadsAllRoles() {
            // When
            Optional<User> found = userRepository.findById(user2.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().hasAdminRole()).isTrue();
        }
    }

    @Nested
    @DisplayName("이메일 인증")
    class EmailVerificationTest {

        @Test
        @DisplayName("이메일 인증 처리")
        void verifyEmail_UpdatesStatus() {
            // Given
            user1.verifyEmail();

            // When
            User updated = userRepository.save(user1);

            // Then
            assertThat(updated.isEmailVerified()).isTrue();
        }
    }
}