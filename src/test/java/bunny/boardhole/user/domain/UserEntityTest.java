package bunny.boardhole.user.domain;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.jpa.EntityTestBase;
import bunny.boardhole.user.domain.validation.UserValidationConstants;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User 엔티티 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("entity")
@Tag("jpa")
class UserEntityTest extends EntityTestBase {
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Nested
    @DisplayName("생성자 및 빌더 테스트")
    @Tag("creation")
    class UserCreation {

        @Test
        @DisplayName("✅ 빌더를 사용한 User 생성 테스트")
        void createUser_WithBuilder_Success() {
            // given
            String username = createUniqueUsername();
            String email = EntityTestBase.createUniqueEmail();

            // when
            User user = User.builder().username(username).password(passwordEncoder.encode(testData.password())).name(testData.name()).email(email).roles(Set.of(Role.USER)).build();

            // then
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(passwordEncoder.matches(testData.password(), user.getPassword())).isTrue();
            assertThat(user.getName()).isEqualTo(testData.name());
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getRoles()).isEqualTo(Set.of(Role.USER));
            assertThat(user.getCreatedAt()).isNull();
            assertThat(user.getUpdatedAt()).isNull();
            assertThat(user.getLastLogin()).isNull();
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    @Tag("validation")
    class RequiredFieldValidation {

        @Test
        @DisplayName("❌ 빈 사용자명으로 User 생성 시 예외 발생")
        void createUser_WithEmptyUsername_ThrowsException() {
            // given
            String expectedMessage = MessageUtils.get("validation.user.username.required");

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username("").password(passwordEncoder.encode(testData.password())).name(testData.name()).email(testData.email()).roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 빈 비밀번호로 User 생성 시 예외 발생")
        void createUser_WithEmptyPassword_ThrowsException() {
            // given
            String expectedMessage = MessageUtils.get("validation.user.password.required");

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username(createUniqueUsername()).password("").name(testData.name()).email(testData.email()).roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 빈 이름으로 User 생성 시 예외 발생")
        void createUser_WithEmptyName_ThrowsException() {
            // given
            String expectedMessage = MessageUtils.get("validation.user.name.required");

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name("").email(testData.email()).roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 빈 이메일로 User 생성 시 예외 발생")
        void createUser_WithEmptyEmail_ThrowsException() {
            // given
            String expectedMessage = MessageUtils.get("validation.user.email.required");

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name(testData.name()).email("").roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("길이 제한 검증 테스트")
    @Tag("validation")
    class LengthValidation {

        @Test
        @DisplayName("❌ 사용자명이 최대 길이를 초과할 때 예외 발생")
        void createUser_WithUsernameTooLong_ThrowsException() {
            // given
            String longUsername = "a".repeat(UserValidationConstants.USER_USERNAME_MAX_LENGTH + 1);
            String expectedMessage = MessageUtils.get("validation.user.username.too-long", UserValidationConstants.USER_USERNAME_MAX_LENGTH);

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username(longUsername).password(passwordEncoder.encode(testData.password())).name(testData.name()).email(testData.email()).roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 이름이 최대 길이를 초과할 때 예외 발생")
        void createUser_WithNameTooLong_ThrowsException() {
            // given
            String longName = "a".repeat(UserValidationConstants.USER_NAME_MAX_LENGTH + 1);
            String expectedMessage = MessageUtils.get("validation.user.name.too-long", UserValidationConstants.USER_NAME_MAX_LENGTH);

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name(longName).email(testData.email()).roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 이메일이 최대 길이를 초과할 때 예외 발생")
        void createUser_WithEmailTooLong_ThrowsException() {
            // given
            String longEmail = "a".repeat(UserValidationConstants.USER_EMAIL_MAX_LENGTH) + "@example.com";
            String expectedMessage = MessageUtils.get("validation.user.email.too-long", UserValidationConstants.USER_EMAIL_MAX_LENGTH);

            // when & then
            assertThatThrownBy(() -> {
                User user = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name(testData.name()).email(longEmail).roles(Set.of(Role.USER)).build();
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("JPA 생명주기 테스트")
    @Tag("lifecycle")
    class JpaLifecycle {

        @Test
        @DisplayName("✅ JPA Auditing 자동 설정 확인")
        void jpaAuditing_SetsTimestampsAutomatically() {
            // given
            User user = createTestUser();

            // when
            entityManager.persistAndFlush(user);

            // then - BaseEntity 상속으로 JPA Auditing이 자동으로 설정됨
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
            // 테스트 환경에서는 AuditorAware가 설정되지 않을 수 있음 (null 허용)
            assertThat(user.getLastLogin()).isNull(); // 비즈니스 필드는 여전히 수동 관리
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    @Tag("business")
    class BusinessMethods {

        @Test
        @DisplayName("✅ changeName 테스트 - 정상적인 이름 변경")
        void changeName_WithValidName_Success() {
            // given
            User user = createTestUser();
            final String newName = "새로운 이름";

            // when
            user.changeName(newName);

            // then
            assertThat(user.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("❌ changeName 테스트 - 빈 이름으로 변경 시 예외 발생")
        void changeName_WithEmptyName_ThrowsException() {
            // given
            User user = createTestUser();
            String expectedMessage = MessageUtils.get("validation.user.name.required");

            // when & then
            assertThatThrownBy(() -> {
                user.changeName("");
                entityManager.persistAndFlush(user);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("✅ changeEmail 테스트 - 정상적인 이메일 변경")
        void changeEmail_WithValidEmail_Success() {
            // given
            User user = createTestUser();
            String newEmail = EntityTestBase.createUniqueEmail();

            // when
            user.changeEmail(newEmail);

            // then
            assertThat(user.getEmail()).isEqualTo(newEmail);
        }

        @Test
        @DisplayName("✅ changePassword 테스트 - 정상적인 비밀번호 변경")
        void changePassword_WithValidPassword_Success() {
            // given
            User user = createTestUser();
            final String newPassword = "NewPassword123!";

            // when
            user.changePassword(newPassword);

            // then
            assertThat(user.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("✅ recordLastLogin 테스트 - 마지막 로그인 시간 기록")
        void recordLastLogin_UpdatesLastLoginTime() {
            // given
            User user = createTestUser();

            // when
            user.recordLastLogin();

            // then
            assertThat(user.getLastLogin()).isNotNull();
            assertThat(user.getLastLogin()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("JPA 영속성 테스트")
    @Tag("persistence")
    class JpaPersistence {

        @Test
        @DisplayName("✅ JPA 저장 및 조회 테스트")
        void saveAndFind_PersistsCorrectly() {
            // given
            User user = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name(testData.name()).email(EntityTestBase.createUniqueEmail()).roles(Set.of(Role.USER, Role.ADMIN)).build();

            // when
            entityManager.persistAndFlush(user);
            entityManager.clear();
            User foundUser = entityManager.find(User.class, user.getId());

            // then
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
            assertThat(foundUser.getName()).isEqualTo(testData.name());
            assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
            assertThat(foundUser.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
            assertThat(foundUser.getCreatedAt()).isNotNull();
            assertThat(foundUser.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("✅ equals와 hashCode 테스트 - ID 기반 동등성")
        void equalsAndHashCode_BasedOnId() {
            // given
            User user1 = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name("사용자1").email(EntityTestBase.createUniqueEmail()).roles(Set.of(Role.USER)).build();

            User user2 = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode("Password456!")).name("사용자2").email(EntityTestBase.createUniqueEmail()).roles(Set.of(Role.ADMIN)).build();

            // when
            entityManager.persistAndFlush(user1);
            entityManager.persistAndFlush(user2);

            // then
            assertThat(user1).isNotEqualTo(user2);
            assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());

            // 같은 ID를 가진 User는 동등
            User sameUser = entityManager.find(User.class, user1.getId());
            assertThat(user1).isEqualTo(sameUser);
        }

        @Test
        @DisplayName("✅ 권한 컬렉션 테스트")
        void roles_CollectionHandling() {
            // given
            User user = User.builder().username(createUniqueUsername()).password(passwordEncoder.encode(testData.password())).name(testData.name()).email(EntityTestBase.createUniqueEmail()).roles(Set.of(Role.USER, Role.ADMIN)).build();

            // when
            entityManager.persistAndFlush(user);
            entityManager.clear();
            User foundUser = entityManager.find(User.class, user.getId());

            // then
            assertThat(foundUser.getRoles()).hasSize(2);
            assertThat(foundUser.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        }
    }

    @Nested
    @DisplayName("보안 테스트")
    @Tag("security")
    class SecurityTests {

        @Test
        @DisplayName("✅ toString 테스트 - 민감한 정보 제외")
        void toString_ExcludesSensitiveFields() {
            // given
            User user = User.builder().username(createUniqueUsername()).password("SecretPassword123!").name(testData.name()).email(EntityTestBase.createUniqueEmail()).roles(Set.of(Role.USER)).build();

            // when
            String userString = user.toString();

            // then
            assertThat(userString).doesNotContain("SecretPassword123!");
            assertThat(userString).doesNotContain("password");
            assertThat(userString).contains(user.getUsername());
            assertThat(userString).contains(testData.name());
        }
    }
}
