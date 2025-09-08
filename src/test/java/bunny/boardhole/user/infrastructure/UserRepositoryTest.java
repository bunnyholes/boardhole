package bunny.boardhole.user.infrastructure;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import bunny.boardhole.shared.config.TestJpaConfig;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@Tag("unit")
@Tag("repository")
class UserRepositoryTest {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder().username("test_user").password(passwordEncoder.encode("Password123!")).name("Test User").email("test@example.com").roles(Set.of(Role.USER)).build());

        adminUser = userRepository.save(User.builder().username("admin_user").password(passwordEncoder.encode("Admin123!")).name("Admin User").email("admin@example.com").roles(Set.of(Role.USER, Role.ADMIN)).build());
    }

    // =====================================
    // CREATE 테스트
    // =====================================
    @Nested
    @DisplayName("CREATE - 사용자 생성")
    class CreateTest {

        @Test
        @DisplayName("새 사용자 생성 성공")
        void save_NewUser_CreatesSuccessfully() {
            // Given
            User newUser = User.builder().username("new_user").password(UserRepositoryTest.passwordEncoder.encode("Password123!")).name("New User").email("new@example.com").roles(Set.of(Role.USER)).build();

            // When
            User saved = userRepository.save(newUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("new_user");
            assertThat(saved.getEmail()).isEqualTo("new@example.com");
            assertThat(saved.hasRole(Role.USER)).isTrue();
        }

        @Test
        @DisplayName("중복 사용자명으로 생성 실패")
        @Transactional
        void save_DuplicateUsername_ThrowsException() {
            // Given
            User duplicate = User.builder().username(testUser.getUsername()).password(UserRepositoryTest.passwordEncoder.encode("Password123!")).name("Duplicate").email("duplicate@example.com").roles(Set.of(Role.USER)).build();

            // When & Then
            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate)).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("중복 이메일로 생성 실패")
        @Transactional
        void save_DuplicateEmail_ThrowsException() {
            // Given
            User duplicate = User.builder().username("unique_user").password(UserRepositoryTest.passwordEncoder.encode("Password123!")).name("Duplicate Email").email(testUser.getEmail()).roles(Set.of(Role.USER)).build();

            // When & Then
            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate)).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    // =====================================
    // READ 테스트
    // =====================================
    @Nested
    @DisplayName("READ - 사용자 조회")
    class ReadTest {

        @Test
        @DisplayName("ID로 사용자 조회")
        void findById_ExistingUser_ReturnsUser() {
            // When
            Optional<User> found = userRepository.findById(testUser.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("test_user");
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
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
            Optional<User> found = userRepository.findByUsername("admin_user");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Admin User");
            assertThat(found.get().hasRole(Role.ADMIN)).isTrue();
        }

        @Test
        @DisplayName("이메일로 조회")
        void findByEmail_ExistingUser_ReturnsUser() {
            // When
            Optional<User> found = userRepository.findByEmail("admin@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("admin_user");
        }

        @Test
        @DisplayName("전체 사용자 조회")
        void findAll_ReturnsAllUsers() {
            // When
            var users = userRepository.findAll();

            // Then
            assertThat(users).hasSize(2);
            assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("test_user", "admin_user");
        }

        @Test
        @DisplayName("사용자명 존재 여부 확인")
        void existsByUsername_ExistingUsername_ReturnsTrue() {
            // When & Then
            assertThat(userRepository.existsByUsername("test_user")).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("이메일 존재 여부 확인")
        void existsByEmail_ExistingEmail_ReturnsTrue() {
            // When & Then
            assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }
    }

    // =====================================
    // UPDATE 테스트
    // =====================================
    @Nested
    @DisplayName("UPDATE - 사용자 수정")
    class UpdateTest {

        @Test
        @DisplayName("사용자 정보 수정")
        void save_ExistingUser_UpdatesSuccessfully() {
            // Given
            testUser.changeName("Updated Name");
            testUser.changeEmail("updated@example.com");

            // When
            User updated = userRepository.save(testUser);

            // Then
            assertThat(updated.getName()).isEqualTo("Updated Name");
            assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        }

        @Test
        @DisplayName("권한 추가")
        void grantRole_NewRole_AddsSuccessfully() {
            // Given
            testUser.grantRole(Role.ADMIN);

            // When
            userRepository.save(testUser);
            Optional<User> found = userRepository.findById(testUser.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().hasRole(Role.ADMIN)).isTrue();
            assertThat(found.get().hasRole(Role.USER)).isTrue();
        }

        @Test
        @DisplayName("권한 제거")
        void revokeRole_ExistingRole_RemovesSuccessfully() {
            // Given
            adminUser.revokeRole(Role.ADMIN);

            // When
            userRepository.save(adminUser);
            Optional<User> found = userRepository.findById(adminUser.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().hasRole(Role.ADMIN)).isFalse();
            assertThat(found.get().hasRole(Role.USER)).isTrue();
        }

        @Test
        @DisplayName("이메일 검증 상태 변경")
        void verifyEmail_UpdatesVerificationStatus() {
            // Given
            testUser.verifyEmail();

            // When
            userRepository.save(testUser);
            Optional<User> found = userRepository.findById(testUser.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().isEmailVerified()).isTrue();
        }
    }

    // =====================================
    // DELETE 테스트
    // =====================================
    @Nested
    @DisplayName("DELETE - 사용자 삭제")
    class DeleteTest {

        @Test
        @DisplayName("사용자 삭제")
        void delete_ExistingUser_RemovesSuccessfully() {
            // Given
            Long userId = testUser.getId();
            long countBefore = userRepository.count();

            // When
            userRepository.delete(testUser);

            // Then
            assertThat(userRepository.findById(userId)).isEmpty();
            assertThat(userRepository.count()).isEqualTo(countBefore - 1);
        }

        @Test
        @DisplayName("ID로 사용자 삭제")
        void deleteById_ExistingUser_RemovesSuccessfully() {
            // Given
            Long userId = adminUser.getId();
            long countBefore = userRepository.count();

            // When
            userRepository.deleteById(userId);

            // Then
            assertThat(userRepository.findById(userId)).isEmpty();
            assertThat(userRepository.count()).isEqualTo(countBefore - 1);
        }

        @Test
        @DisplayName("전체 사용자 삭제")
        void deleteAll_RemovesAllUsers() {
            // When
            userRepository.deleteAll();

            // Then
            assertThat(userRepository.count()).isEqualTo(0);
            assertThat(userRepository.findAll()).isEmpty();
        }
    }

    // =====================================
    // 페이징 및 검색 테스트
    // =====================================
    @Nested
    @DisplayName("페이징 및 검색")
    class PagingAndSearchTest {

        @BeforeEach
        void setUpAdditionalUsers() {
            for (int i = 1; i <= 5; i++)
                userRepository.save(User.builder().username("user_" + i).password(UserRepositoryTest.passwordEncoder.encode("Password123!")).name("User " + i).email("user" + i + "@example.com").roles(Set.of(Role.USER)).build());
        }

        @Test
        @DisplayName("페이징 처리된 사용자 목록 조회")
        void findAll_WithPaging_ReturnsPagedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 3);

            // When
            Page<User> page = userRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getTotalElements()).isEqualTo(7);
            assertThat(page.getTotalPages()).isEqualTo(3);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("복합 조건 검색")
        void search_MultipleConditions_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("user", "user", "user", pageable);

            // Then
            assertThat(result.getContent()).hasSize(7); // test_user, admin_user, user_1~5
            assertThat(result.getTotalElements()).isEqualTo(7);
        }

        @Test
        @DisplayName("대소문자 구분 없는 검색")
        void search_CaseInsensitive_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> result = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("ADMIN", "ADMIN", "ADMIN", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getUsername()).isEqualTo("admin_user");
        }
    }

    // =====================================
    // Auditing 테스트
    // =====================================
    @Nested
    @DisplayName("Auditing 기능")
    class AuditingTest {

        @Test
        @DisplayName("생성 시 createdAt, updatedAt 자동 설정")
        void save_NewEntity_SetsAuditFields() {
            // Given
            User newUser = User.builder().username("audit_test").password(UserRepositoryTest.passwordEncoder.encode("Password123!")).name("Audit Test").email("audit@example.com").roles(Set.of(Role.USER)).build();

            // When
            User saved = userRepository.save(newUser);

            // Then
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
        }

        @Test
        @DisplayName("수정 시 updatedAt 변경")
        void update_ExistingEntity_UpdatesAuditFields() {
            // Given
            var createdAt = testUser.getCreatedAt();
            testUser.changeName("Modified");

            // When
            User updated = userRepository.save(testUser);

            // Then
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
            assertThat(updated.getUpdatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(createdAt);
        }
    }
}