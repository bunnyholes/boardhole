package bunny.boardhole.user.domain;

import bunny.boardhole.shared.test.EntityTestBase;
import bunny.boardhole.shared.util.MessageUtils;
import org.junit.jupiter.api.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EmailVerification 엔티티 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("entity")
@Tag("jpa")
class EmailVerificationEntityTest extends EntityTestBase {

    private EmailVerification createEmailVerification() {
        return EmailVerification.builder()
                .code(createUniqueCode())
                .userId(1L)
                .newEmail(createUniqueEmail())
                .expiresAt(getTestExpirationTime())
                .verificationType(EmailVerificationType.SIGNUP)
                .build();
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
            Long userId = 1L;
            String newEmail = createUniqueEmail();
            LocalDateTime expiresAt = getTestExpirationTime();

            // when
            EmailVerification verification = EmailVerification.builder()
                    .code(code)
                    .userId(userId)
                    .newEmail(newEmail)
                    .expiresAt(expiresAt)
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build();

            // then
            assertThat(verification.getCode()).isEqualTo(code);
            assertThat(verification.getUserId()).isEqualTo(userId);
            assertThat(verification.getNewEmail()).isEqualTo(newEmail);
            assertThat(verification.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(verification.isUsed()).isFalse();
            assertThat(verification.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    @Tag("validation")
    class RequiredFieldValidation {

        @Test
        @DisplayName("❌ 빈 검증 코드로 생성 시 예외 발생")
        void createEmailVerification_WithEmptyCode_ThrowsException() {
            // given
            String expectedMessage = MessageUtils.get("validation.email-verification.code.required");

            // when & then
            assertThatThrownBy(() -> EmailVerification.builder()
                    .code("")
                    .userId(1L)
                    .newEmail(createUniqueEmail())
                    .expiresAt(getTestExpirationTime())
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(expectedMessage);
        }


        @Test
        @DisplayName("❌ 빈 새 이메일로 생성 시 예외 발생")
        void createEmailVerification_WithEmptyNewEmail_ThrowsException() {
            // given
            String expectedMessage = MessageUtils.get("validation.email-verification.new-email.required");

            // when & then
            assertThatThrownBy(() -> EmailVerification.builder()
                    .code(createUniqueCode())
                    .userId(1L)
                    .newEmail("")
                    .expiresAt(getTestExpirationTime())
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(expectedMessage);
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
            String expectedMessage = "이미 사용된 검증 코드입니다";
            assertThatThrownBy(verification::markAsUsed)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMessage);
        }

        @Test
        @DisplayName("❌ markAsUsed 테스트 - 만료된 검증 코드 예외")
        void markAsUsed_Expired_ThrowsException() {
            // given
            EmailVerification verification = EmailVerification.builder()
                    .code(createUniqueCode())
                    .userId(1L)
                    .newEmail(createUniqueEmail())
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1))
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build();

            // when & then
            String expectedMessage = "만료된 검증 코드입니다";
            assertThatThrownBy(verification::markAsUsed)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedMessage);
        }

        @Test
        @DisplayName("✅ isExpired 테스트 - 만료 여부 확인")
        void isExpired_ChecksExpirationCorrectly() {
            // given
            EmailVerification expiredVerification = EmailVerification.builder()
                    .code(createUniqueCode())
                    .userId(1L)
                    .newEmail(createUniqueEmail())
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1))
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build();

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

            EmailVerification expiredVerification = EmailVerification.builder()
                    .code(createUniqueCode())
                    .userId(1L)
                    .newEmail(createUniqueEmail())
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).minusHours(1))
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build();

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
            assertThat(foundVerification.getUserId()).isEqualTo(1L);
            assertThat(foundVerification.getNewEmail()).isEqualTo(verification.getNewEmail());
            assertThat(foundVerification.isUsed()).isFalse();
            assertThat(foundVerification.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("✅ equals와 hashCode 테스트 - code 기반 동등성")
        void equalsAndHashCode_BasedOnCode() {
            // given
            EmailVerification verification1 = EmailVerification.builder()
                    .code(createUniqueCode())
                    .userId(1L)
                    .newEmail(createUniqueEmail())
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1))
                    .verificationType(EmailVerificationType.SIGNUP)
                    .build();

            EmailVerification verification2 = EmailVerification.builder()
                    .code(createUniqueCode())
                    .userId(2L)
                    .newEmail(createUniqueEmail())
                    .expiresAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(2))
                    .verificationType(EmailVerificationType.CHANGE_EMAIL)
                    .build();

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