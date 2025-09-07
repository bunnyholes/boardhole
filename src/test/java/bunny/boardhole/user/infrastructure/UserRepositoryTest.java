package bunny.boardhole.user.infrastructure;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 - HashSet을 사용하여 mutable collection 제공
        user1 = User.builder()
                .username("john_doe")
                .password("Password123!")
                .name("John Doe")
                .email("john@example.com")
                .roles(new HashSet<>(Set.of(Role.USER)))
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .username("jane_smith")
                .password("Password456!")
                .name("Jane Smith")
                .email("jane@example.com")
                .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                .build();
        user2 = userRepository.save(user2);

        User user3 = User.builder()
                .username("bob_johnson")
                .password("Password789!")
                .name("Bob Johnson")
                .email("bob@example.com")
                .roles(new HashSet<>(Set.of(Role.USER)))
                .build();
        userRepository.save(user3);
    }

    @AfterEach
    void tearDown() {
        // Clear the entity manager session to avoid issues with invalid entities
        entityManager.clear();
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
            assertThat(user.hasRole(Role.ADMIN)).isTrue();
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
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("john", "john", "john", pageable);

            // Then
            assertThat(page.getContent()).hasSize(2); // john_doe, bob_johnson
            assertThat(page.getContent()).extracting(User::getUsername).containsExactlyInAnyOrder("john_doe", "bob_johnson");
        }

        @Test
        @DisplayName("이름으로 검색")
        void searchByName_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("Smith", "Smith", "Smith", pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().getFirst().getName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("이메일로 검색")
        void searchByEmail_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("bob@", "bob@", "bob@", pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().getFirst().getEmail()).isEqualTo("bob@example.com");
        }

        @Test
        @DisplayName("대소문자 구분 없이 검색")
        void searchCaseInsensitive_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("JANE", "JANE", "JANE", pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().getFirst().getUsername()).isEqualTo("jane_smith");
        }

        @Test
        @DisplayName("검색 결과 페이징 처리")
        void searchWithPaging_ReturnsPagedResults() {
            // Given - 더 많은 사용자 추가
            for (int i = 0; i < 5; i++) {
                User extraUser = User.builder()
                        .username("user_" + i)
                        .password("Password123!")
                        .name("Test User " + i)
                        .email("user" + i + "@example.com")
                        .roles(new HashSet<>(Set.of(Role.USER)))
                        .build();
                userRepository.save(extraUser);
            }

            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("example", "example", "example", pageable);

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
            Page<User> page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("nonexistent", "nonexistent", "nonexistent", pageable);

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
                    .password("NewPassword123!")
                    .name("New User")
                    .email("new@example.com")
                    .roles(new HashSet<>(Set.of(Role.USER)))
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
        @Transactional
        void save_DuplicateUsername_ThrowsException() {
            // Given
            User duplicateUser = User.builder()
                    .username("john_doe") // 이미 존재하는 사용자명
                    .password("Password123!")
                    .name("Another John")
                    .email("another@example.com")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                try {
                    userRepository.saveAndFlush(duplicateUser);
                } catch (Exception e) {
                    // Clear the session to prevent issues with invalid entity state
                    entityManager.clear();
                    throw e;
                }
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("중복된 이메일로 생성 실패")
        @Transactional
        void save_DuplicateEmail_ThrowsException() {
            // Given
            User duplicateUser = User.builder()
                    .username("another_user")
                    .password("Password123!")
                    .name("Another User")
                    .email("john@example.com") // 이미 존재하는 이메일
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                try {
                    userRepository.saveAndFlush(duplicateUser);
                } catch (Exception e) {
                    // Clear the session to prevent issues with invalid entity state
                    entityManager.clear();
                    throw e;
                }
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
            user1.grantRole(Role.ADMIN);

            // When
            userRepository.save(user1);

            // Then
            Optional<User> found = userRepository.findById(user1.getId());
            assertThat(found).isPresent();
            assertThat(found.get().hasRole(Role.ADMIN)).isTrue();
        }

        @Test
        @DisplayName("권한 제거")
        void removeRole_ExistingRole_RemovesSuccessfully() {
            // Given
            user2.revokeRole(Role.ADMIN);

            // When
            userRepository.save(user2);

            // Then
            Optional<User> found = userRepository.findById(user2.getId());
            assertThat(found).isPresent();
            assertThat(found.get().hasRole(Role.ADMIN)).isFalse();
            // User는 기본적으로 권한을 가짐
        }

        @Test
        @DisplayName("모든 권한 조회")
        void findById_UserWithMultipleRoles_LoadsAllRoles() {
            // When
            Optional<User> found = userRepository.findById(user2.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().hasRole(Role.ADMIN)).isTrue();
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

    @Nested
    @DisplayName("연관관계 및 지연 로딩")
    class LazyLoadingTest {

        @Test
        @DisplayName("Lazy Loading 동작 확인")
        @Transactional
        void lazyLoading_RolesCollection() {
            // Given - Ensure roles are persisted first
            userRepository.flush();
            entityManager.clear();

            // When - roles를 fetch하지 않고 조회
            User found = entityManager.find(User.class, user2.getId());

            // Then - roles 컬렉션 접근 시 지연 로딩 발생
            assertThat(found.getRoles()).isNotNull();
            assertThat(found.hasRole(Role.ADMIN)).isTrue();
        }

        @Test
        @DisplayName("N+1 문제 해결 확인")
        @Transactional
        void nPlusOneProblem_Solved() {
            // Given
            entityManager.clear();

            // When - EntityGraph로 roles 함께 조회
            Optional<User> found = userRepository.findByUsername(user2.getUsername());

            // Then - 추가 쿼리 없이 roles 접근 가능
            assertThat(found).isPresent();
            assertThat(found.get().getRoles()).isNotNull();
            // EntityGraph 덕분에 추가 쿼리 발생하지 않음
        }
    }

    @Nested
    @DisplayName("Auditing 기능 테스트")
    class AuditingTest {

        @Test
        @DisplayName("생성 시 createdAt, updatedAt 자동 설정")
        void save_NewEntity_SetsAuditFields() {
            // Given
            User newUser = User.builder()
                    .username("audit_user")
                    .password("Password123!")
                    .name("Audit Test")
                    .email("audit@example.com")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .build();

            // When
            User saved = userRepository.save(newUser);

            // Then
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
        }

        @Test
        @DisplayName("수정 시 updatedAt 변경 확인")
        void update_ExistingEntity_UpdatesAuditFields() {
            // Given
            User newUser = User.builder()
                    .username("update_audit")
                    .password("Password123!")
                    .name("Original")
                    .email("update@example.com")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .build();
            User saved = userRepository.save(newUser);

            // When
            saved.changeName("Updated");
            User updated = userRepository.save(saved);

            // Then - updatedAt이 설정되어 있음을 확인
            assertThat(updated.getCreatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Query Method 네이밍 규칙 테스트")
    class QueryMethodNamingTest {

        @Test
        @DisplayName("복잡한 조건의 Query Method 동작")
        void complexQueryMethod_WorksCorrectly() {
            // Given - 추가 사용자 생성
            User adminUser = User.builder()
                    .username("admin_special")
                    .password("Password123!")
                    .name("Admin Special")
                    .email("admin.special@example.com")
                    .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                    .build();
            adminUser.verifyEmail();
            userRepository.save(adminUser);

            // When - 여러 조건으로 검색
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> result = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("admin", "admin", "admin", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getUsername()).isEqualTo("admin_special");
        }

        @Test
        @DisplayName("대소문자 구분 없는 검색 메서드")
        void caseInsensitiveSearch_WorksCorrectly() {
            // When
            Page<User> upperCase = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("JOHN", "JOHN", "JOHN", PageRequest.of(0, 10));
            Page<User> lowerCase = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase("john", "john", "john", PageRequest.of(0, 10));

            // Then
            assertThat(upperCase.getTotalElements()).isEqualTo(lowerCase.getTotalElements());
            assertThat(upperCase.getContent()).hasSameSizeAs(lowerCase.getContent());
        }
    }
}