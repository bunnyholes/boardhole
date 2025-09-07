package bunny.boardhole.user.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.jpa.EntityTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ConstraintViolationException;

@DisplayName("EmailVerification 엔티티 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("entity")
@Tag("jpa")
class EmailVerificationEntityTest extends EntityTestBase {

    private EmailVerification createEmailVerification() {
        User testUser = createAndPersistUser(); // Persist user first to avoid transient entity issues
        return EmailVerification.builder().code(createUniqueCode()).user(testUser).newEmail(createUniqueEmail()).expiresAt(getTestExpirationTime()).verificationType(EmailVerificationType.SIGNUP).build();
    }

    @Nested
    @DisplayName("생성자 및 빌더 테스트")
    @Tag("creation")
    class EmailVerificationCreation {

        @Test
        @DisplayName("✅ 빌더를 사용한 EmailVerification 생성 테스트")
        void createEmailVerification_WithBuilder_Success() {
            // given
            String code = createUniqueCode();
            String newEmail = createUniqueEmail();
            LocalDateTime expiresAt = getTestExpirationTime();

            // when
            User testUser = createAndPersistUser();
            EmailVerification verification = EmailVerification.builder().code(code).user(testUser).newEmail(newEmail).expiresAt(expiresAt).verificationType(EmailVerificationType.SIGNUP).build();

            // then
            assertThat(verification.getCode()).isEqualTo(code);
            assertThat(verification.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(verification.getNewEmail()).isEqualTo(newEmail);
            assertThat(verification.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(verification.getVerificationType()).isEqualTo(EmailVerificationType.SIGNUP);
            assertThat(verification.isUsed()).isFalse();
            assertThat(verification.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    @Tag("validation")
    class RequiredFieldValidation {

        @Test
        @DisplayName("❌ null 검증 코드로 생성 시 예외 발생")
        void createEmailVerification_WithNullCode_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(null)
                        .user(testUser)
                        .newEmail(createUniqueEmail())
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 빈 검증 코드로 생성 시 예외 발생")
        void createEmailVerification_WithEmptyCode_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code("")
                        .user(testUser)
                        .newEmail(createUniqueEmail())
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 짧은 검증 코드로 생성 시 예외 발생")
        void createEmailVerification_WithShortCode_ThrowsException() {
            // given
            String shortCode = "SHORT"; // 5 characters, minimum is 32

            // when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(shortCode)
                        .user(testUser)
                        .newEmail(createUniqueEmail())
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 잘못된 패턴의 검증 코드로 생성 시 예외 발생")
        void createEmailVerification_WithInvalidPatternCode_ThrowsException() {
            // given
            String invalidCode = "ABCDEF1234567890ABCDEF123456789!@"; // 32 chars but contains special characters

            // when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(invalidCode)
                        .user(testUser)
                        .newEmail(createUniqueEmail())
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ null 새 이메일로 생성 시 예외 발생")
        void createEmailVerification_WithNullNewEmail_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(createUniqueCode())
                        .user(testUser)
                        .newEmail(null)
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 빈 새 이메일로 생성 시 예외 발생")
        void createEmailVerification_WithEmptyNewEmail_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(createUniqueCode())
                        .user(testUser)
                        .newEmail("")
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 잘못된 형식의 새 이메일로 생성 시 예외 발생")
        void createEmailVerification_WithInvalidFormatNewEmail_ThrowsException() {
            // given
            String invalidEmail = "invalid-email-format";

            // when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(createUniqueCode())
                        .user(testUser)
                        .newEmail(invalidEmail)
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ null 사용자로 생성 시 예외 발생")
        void createEmailVerification_WithNullUser_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                EmailVerification verification = EmailVerification.builder()
                        .code(createUniqueCode())
                        .user(null)
                        .newEmail(createUniqueEmail())
                        .expiresAt(getTestExpirationTime())
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ null 만료시간으로 생성 시 예외 발생")
        void createEmailVerification_WithNullExpiresAt_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(createUniqueCode())
                        .user(testUser)
                        .newEmail(createUniqueEmail())
                        .expiresAt(null)
                        .verificationType(EmailVerificationType.SIGNUP)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ null 검증 타입으로 생성 시 예외 발생")
        void createEmailVerification_WithNullVerificationType_ThrowsException() {
            // given & when & then
            assertThatThrownBy(() -> {
                User testUser = createAndPersistUser();
                EmailVerification verification = EmailVerification.builder()
                        .code(createUniqueCode())
                        .user(testUser)
                        .newEmail(createUniqueEmail())
                        .expiresAt(getTestExpirationTime())
                        .verificationType(null)
                        .build();
                entityManager.persistAndFlush(verification);
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("JPA 생명주기 테스트")
    @Tag("lifecycle")
    class JpaLifecycle {

        @Test
        @DisplayName("✅ @PrePersist 테스트 - 생성 시 createdAt 자동 설정")
        void prePersist_SetsCreatedAt() {
            // given
            EmailVerification verification = createEmailVerification();

            // when
            entityManager.persistAndFlush(verification);

            // then
            assertThat(verification.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    @Tag("business")
    class BusinessMethods {

        @Test
        @DisplayName("✅ markAsUsed 테스트 - 정상적인 사용 처리")
        void markAsUsed_WithValidVerification_Success() {
            // given
            EmailVerification verification = createEmailVerification();

            // when
            verification.markAsUsed();

            // then
            assertThat(verification.isUsed()).isTrue();
        }

        @Test
        @DisplayName("❌ markAsUsed 테스트 - 이미 사용된 검증 코드 예외")
        void markAsUsed_AlreadyUsed_ThrowsException() {
            // given
            EmailVerification verification = createEmailVerification();
            verification.markAsUsed();

            // when & then
            String expectedMessage = "Email verification code has already been used";
            assertThatThrownBy(verification::markAsUsed).isInstanceOf(IllegalStateException.class).hasMessage(expectedMessage);
        }

        @Test
        @DisplayName("❌ markAsUsed 테스트 - 만료된 검증 코드 예외")
        void markAsUsed_Expired_ThrowsException() {
            // given
            User testUser = createAndPersistUser();
            EmailVerification verification = EmailVerification.builder().code(createUniqueCode()).user(testUser).newEmail(createUniqueEmail()).expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1)).verificationType(EmailVerificationType.SIGNUP).build();

            // when & then
            String expectedMessage = "Email verification code has expired";
            assertThatThrownBy(verification::markAsUsed).isInstanceOf(IllegalStateException.class).hasMessage(expectedMessage);
        }

        @Test
        @DisplayName("✅ isExpired 테스트 - 만료 여부 확인")
        void isExpired_ChecksExpirationCorrectly() {
            // given
            User testUser = createAndPersistUser();
            EmailVerification expiredVerification = EmailVerification.builder().code(createUniqueCode()).user(testUser).newEmail(createUniqueEmail()).expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1)).verificationType(EmailVerificationType.SIGNUP).build();

            EmailVerification validVerification = createEmailVerification();

            // when & then
            assertThat(expiredVerification.isExpired()).isTrue();
            assertThat(validVerification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("✅ isValid 테스트 - 유효성 종합 확인")
        void isValid_ChecksUsedAndExpired() {
            // given
            EmailVerification validVerification = createEmailVerification();

            EmailVerification usedVerification = createEmailVerification();
            usedVerification.markAsUsed();

            User testUser = createAndPersistUser();
            EmailVerification expiredVerification = EmailVerification.builder().code(createUniqueCode()).user(testUser).newEmail(createUniqueEmail()).expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1)).verificationType(EmailVerificationType.SIGNUP).build();

            // when & then
            assertThat(validVerification.isValid()).isTrue();
            assertThat(usedVerification.isValid()).isFalse();
            assertThat(expiredVerification.isValid()).isFalse();
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
            EmailVerification verification = createEmailVerification();

            // when
            entityManager.persistAndFlush(verification);
            entityManager.clear();
            EmailVerification foundVerification = entityManager.find(EmailVerification.class, verification.getCode());

            // then
            assertThat(foundVerification).isNotNull();
            assertThat(foundVerification.getCode()).isEqualTo(verification.getCode());
            assertThat(foundVerification.getUser().getId()).isEqualTo(verification.getUser().getId());
            assertThat(foundVerification.getNewEmail()).isEqualTo(verification.getNewEmail());
            assertThat(foundVerification.getVerificationType()).isEqualTo(verification.getVerificationType());
            assertThat(foundVerification.isUsed()).isFalse();
            assertThat(foundVerification.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("✅ equals와 hashCode 테스트 - code 기반 동등성")
        void equalsAndHashCode_BasedOnCode() {
            // given
            User testUser1 = createAndPersistUser();
            EmailVerification verification1 = EmailVerification.builder().code(createUniqueCode()).user(testUser1).newEmail(createUniqueEmail()).expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1)).verificationType(EmailVerificationType.SIGNUP).build();

            User testUser2 = createAndPersistUser();
            EmailVerification verification2 = EmailVerification.builder().code(createUniqueCode()).user(testUser2).newEmail(createUniqueEmail()).expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(2)).verificationType(EmailVerificationType.CHANGE_EMAIL).build();

            // when
            entityManager.persistAndFlush(verification1);
            entityManager.persistAndFlush(verification2);

            // then
            assertThat(verification1).isNotEqualTo(verification2);
            assertThat(verification1.hashCode()).isNotEqualTo(verification2.hashCode());

            // 같은 code를 가진 EmailVerification은 동등
            EmailVerification sameVerification = entityManager.find(EmailVerification.class, verification1.getCode());
            assertThat(verification1).isEqualTo(sameVerification);
        }
    }
}