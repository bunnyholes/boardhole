package bunny.boardhole.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import bunny.boardhole.shared.config.TestJpaConfig;
import bunny.boardhole.user.domain.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@Tag("integration")
class EmailVerificationRepositoryTest {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private EmailVerification verification1;
    private EmailVerification verification2;
    private EmailVerification verification3;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        user1 = User.builder()
                .username("testuser1")
                .password("password123")
                .name("Test User 1")
                .email("test1@example.com")
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .username("testuser2")
                .password("password456")
                .name("Test User 2")
                .email("test2@example.com")
                .build();
        user2 = userRepository.save(user2);

        // 테스트 이메일 검증 데이터 생성
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        
        // 유효한 검증 정보
        verification1 = EmailVerification.builder()
                .code("valid-token-123")
                .userId(user1.getId())
                .newEmail("test1@example.com")
                .expiresAt(now.plusHours(1))
                .verificationType(EmailVerificationType.SIGNUP)
                .build();
        verification1 = emailVerificationRepository.save(verification1);

        // 만료된 검증 정보
        verification2 = EmailVerification.builder()
                .code("expired-token-456")
                .userId(user1.getId())
                .newEmail("test1@example.com")
                .expiresAt(now.minusHours(1))
                .verificationType(EmailVerificationType.SIGNUP)
                .build();
        verification2 = emailVerificationRepository.save(verification2);

        // 다른 사용자의 검증 정보
        verification3 = EmailVerification.builder()
                .code("another-token-789")
                .userId(user2.getId())
                .newEmail("new@example.com")
                .expiresAt(now.plusHours(2))
                .verificationType(EmailVerificationType.CHANGE_EMAIL)
                .build();
        verification3 = emailVerificationRepository.save(verification3);
    }

    @AfterEach
    void tearDown() {
        emailVerificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("유효한 검증 정보 조회")
    class FindValidVerificationTest {

        @Test
        @DisplayName("유효한 검증 정보 조회 성공")
        void findValidVerification_ValidToken_ReturnsVerification() {
            // Given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

            // When
            Optional<EmailVerification> found = emailVerificationRepository.findValidVerification(
                    user1.getId(), "valid-token-123", now);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getCode()).isEqualTo("valid-token-123");
            assertThat(found.get().getUserId()).isEqualTo(user1.getId());
            assertThat(found.get().getNewEmail()).isEqualTo("test1@example.com");
            assertThat(found.get().getVerificationType()).isEqualTo(EmailVerificationType.SIGNUP);
        }

        @Test
        @DisplayName("만료된 토큰으로 조회 시 빈 결과")
        void findValidVerification_ExpiredToken_ReturnsEmpty() {
            // Given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

            // When
            Optional<EmailVerification> found = emailVerificationRepository.findValidVerification(
                    user1.getId(), "expired-token-456", now);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("사용된 토큰으로 조회 시 빈 결과")
        void findValidVerification_UsedToken_ReturnsEmpty() {
            // Given
            verification1.markAsUsed();
            emailVerificationRepository.save(verification1);
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

            // When
            Optional<EmailVerification> found = emailVerificationRepository.findValidVerification(
                    user1.getId(), "valid-token-123", now);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("잘못된 사용자 ID로 조회 시 빈 결과")
        void findValidVerification_WrongUserId_ReturnsEmpty() {
            // Given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

            // When
            Optional<EmailVerification> found = emailVerificationRepository.findValidVerification(
                    user2.getId(), "valid-token-123", now);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("코드로 검증 정보 조회")
    class FindByCodeTest {

        @Test
        @DisplayName("미사용 토큰 코드로 조회 성공")
        void findByCodeAndUsedFalse_UnusedToken_ReturnsVerification() {
            // When
            Optional<EmailVerification> found = emailVerificationRepository.findByCodeAndUsedFalse("valid-token-123");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUserId()).isEqualTo(user1.getId());
            assertThat(found.get().isUsed()).isFalse();
        }

        @Test
        @DisplayName("사용된 토큰 코드로 조회 시 빈 결과")
        void findByCodeAndUsedFalse_UsedToken_ReturnsEmpty() {
            // Given
            verification1.markAsUsed();
            emailVerificationRepository.save(verification1);

            // When
            Optional<EmailVerification> found = emailVerificationRepository.findByCodeAndUsedFalse("valid-token-123");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 빈 결과")
        void findByCodeAndUsedFalse_NonExistingCode_ReturnsEmpty() {
            // When
            Optional<EmailVerification> found = emailVerificationRepository.findByCodeAndUsedFalse("nonexistent");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자별 검증 정보 조회")
    class FindByUserIdTest {

        @Test
        @DisplayName("사용자의 미사용 검증 정보 목록 조회")
        void findByUserIdAndUsedFalse_ReturnsUnusedVerifications() {
            // When
            List<EmailVerification> verifications = emailVerificationRepository.findByUserIdAndUsedFalse(user1.getId());

            // Then
            assertThat(verifications).hasSize(2); // valid-token-123, expired-token-456
            assertThat(verifications)
                    .extracting(EmailVerification::getCode)
                    .containsExactlyInAnyOrder("valid-token-123", "expired-token-456");
        }

        @Test
        @DisplayName("일부 사용된 토큰이 있는 경우 미사용만 반환")
        void findByUserIdAndUsedFalse_SomeUsed_ReturnsOnlyUnused() {
            // Given
            verification1.markAsUsed();
            emailVerificationRepository.save(verification1);

            // When
            List<EmailVerification> verifications = emailVerificationRepository.findByUserIdAndUsedFalse(user1.getId());

            // Then
            assertThat(verifications).hasSize(1);
            assertThat(verifications.get(0).getCode()).isEqualTo("expired-token-456");
        }

        @Test
        @DisplayName("검증 정보가 없는 사용자 조회 시 빈 목록")
        void findByUserIdAndUsedFalse_NoVerifications_ReturnsEmptyList() {
            // Given
            User newUser = User.builder()
                    .username("newuser")
                    .password("password")
                    .name("New User")
                    .email("new@example.com")
                    .build();
            newUser = userRepository.save(newUser);

            // When
            List<EmailVerification> verifications = emailVerificationRepository.findByUserIdAndUsedFalse(newUser.getId());

            // Then
            assertThat(verifications).isEmpty();
        }
    }

    @Nested
    @DisplayName("만료된 검증 정보 삭제")
    class DeleteExpiredVerificationsTest {

        @Test
        @DisplayName("만료된 검증 정보 삭제")
        void deleteExpiredVerifications_RemovesExpiredAndUsed() {
            // Given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            
            // 사용된 토큰 추가
            verification3.markAsUsed();
            emailVerificationRepository.save(verification3);

            // When
            int deletedCount = emailVerificationRepository.deleteExpiredVerifications(now);

            // Then
            assertThat(deletedCount).isEqualTo(2); // expired-token-456, used verification3
            
            List<EmailVerification> remaining = emailVerificationRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getCode()).isEqualTo("valid-token-123");
        }

        @Test
        @DisplayName("만료된 검증 정보가 없는 경우")
        void deleteExpiredVerifications_NoExpired_DeletesNothing() {
            // Given
            LocalDateTime farPast = LocalDateTime.now(ZoneId.systemDefault()).minusYears(1);

            // When
            int deletedCount = emailVerificationRepository.deleteExpiredVerifications(farPast);

            // Then
            assertThat(deletedCount).isEqualTo(0);
            assertThat(emailVerificationRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("사용자 검증 정보 무효화")
    class InvalidateUserVerificationsTest {

        @Test
        @DisplayName("특정 사용자의 모든 미사용 검증 정보 무효화")
        void invalidateUserVerifications_MarksAllAsUsed() {
            // Given
            // user1은 2개의 검증 정보를 가지고 있음

            // When
            emailVerificationRepository.invalidateUserVerifications(user1.getId());

            // Then
            List<EmailVerification> userVerifications = emailVerificationRepository.findByUserIdAndUsedFalse(user1.getId());
            assertThat(userVerifications).isEmpty();
            
            // user2의 검증 정보는 영향받지 않음
            List<EmailVerification> user2Verifications = emailVerificationRepository.findByUserIdAndUsedFalse(user2.getId());
            assertThat(user2Verifications).hasSize(1);
        }

        @Test
        @DisplayName("이미 모두 사용된 경우 변경 없음")
        void invalidateUserVerifications_AllAlreadyUsed_NoChange() {
            // Given
            verification1.markAsUsed();
            verification2.markAsUsed();
            emailVerificationRepository.save(verification1);
            emailVerificationRepository.save(verification2);

            // When
            emailVerificationRepository.invalidateUserVerifications(user1.getId());

            // Then
            List<EmailVerification> userVerifications = emailVerificationRepository.findByUserIdAndUsedFalse(user1.getId());
            assertThat(userVerifications).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD 작업")
    class CrudOperationsTest {

        @Test
        @DisplayName("새 검증 정보 생성")
        void save_NewVerification_CreatesSuccessfully() {
            // Given
            LocalDateTime expiresAt = LocalDateTime.now(ZoneId.systemDefault()).plusHours(1);
            EmailVerification newVerification = EmailVerification.builder()
                    .code("new-token-999")
                    .userId(user1.getId())
                    .newEmail("newemail@example.com")
                    .expiresAt(expiresAt)
                    .verificationType(EmailVerificationType.CHANGE_EMAIL)
                    .build();

            // When
            EmailVerification saved = emailVerificationRepository.save(newVerification);

            // Then
            assertThat(saved.getCode()).isNotNull();
            assertThat(saved.getCode()).isEqualTo("new-token-999");
            assertThat(saved.getNewEmail()).isEqualTo("newemail@example.com");
            assertThat(saved.isUsed()).isFalse();
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("검증 정보 사용 처리")
        void markAsUsed_UpdatesSuccessfully() {
            // Given
            verification1.markAsUsed();

            // When
            EmailVerification updated = emailVerificationRepository.save(verification1);

            // Then
            assertThat(updated.isUsed()).isTrue();
        }

        @Test
        @DisplayName("검증 정보 삭제")
        void delete_RemovesSuccessfully() {
            // Given
            String verificationCode = verification1.getCode();

            // When
            emailVerificationRepository.delete(verification1);

            // Then
            Optional<EmailVerification> found = emailVerificationRepository.findByCodeAndUsedFalse(verificationCode);
            assertThat(found).isEmpty();
            assertThat(emailVerificationRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("만료 여부 확인")
        void isExpired_ChecksCorrectly() {
            // When & Then
            assertThat(verification1.isExpired()).isFalse(); // 1시간 후 만료
            assertThat(verification2.isExpired()).isTrue();  // 1시간 전 만료
        }
    }
}