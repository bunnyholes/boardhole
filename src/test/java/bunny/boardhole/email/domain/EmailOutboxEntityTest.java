package bunny.boardhole.email.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import bunny.boardhole.testsupport.jpa.EntityTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailOutbox 엔티티 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("entity")
@Tag("email")
class EmailOutboxEntityTest extends EntityTestBase {

    private static EmailOutbox createTestEmailOutbox() {
        EmailMessage message = EmailMessage.create(EntityTestBase.createUniqueEmail(), "Test Subject", "<p>Test Content</p>");
        return EmailOutbox.from(message);
    }

    @Nested
    @DisplayName("생성자 및 빌더 테스트")
    @Tag("creation")
    class EmailOutboxCreation {

        @Test
        @DisplayName("✅ EmailMessage로부터 EmailOutbox 생성")
        void createEmailOutbox_FromEmailMessage_Success() {
            // given
            final String email = "test@example.com";
            final String subject = "Test Subject";
            final String content = "<p>Test Content</p>";
            EmailMessage message = EmailMessage.create(email, subject, content);

            // when
            EmailOutbox outbox = EmailOutbox.from(message);

            // then
            assertThat(outbox.getRecipientEmail()).isEqualTo(email);
            assertThat(outbox.getSubject()).isEqualTo(subject);
            assertThat(outbox.getContent()).isEqualTo(content);
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
            assertThat(outbox.getLastError()).isNull();
            assertThat(outbox.getNextRetryAt()).isNull();
        }

        @Test
        @DisplayName("✅ EmailOutbox를 EmailMessage로 변환")
        void toEmailMessage_Conversion_Success() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();

            // when
            EmailMessage message = outbox.toEmailMessage();

            // then
            assertThat(message.recipientEmail()).isEqualTo(outbox.getRecipientEmail());
            assertThat(message.subject()).isEqualTo(outbox.getSubject());
            assertThat(message.content()).isEqualTo(outbox.getContent());
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    @Tag("business")
    class BusinessMethods {

        @Test
        @DisplayName("✅ markAsSent() - 발송 성공 처리")
        void markAsSent_UpdatesStatus_Success() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            outbox.setNextRetryAt(LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(5));

            // when
            outbox.markAsSent();

            // then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.SENT);
            assertThat(outbox.getNextRetryAt()).isNull();
        }

        @Test
        @DisplayName("✅ recordFailure() - 재시도 가능한 실패 기록")
        void recordFailure_WithRetryAvailable_UpdatesToPending() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            final String errorMessage = "Connection timeout";
            final int maxRetries = 5;

            // when
            outbox.recordFailure(errorMessage, maxRetries);

            // then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getLastError()).isEqualTo(errorMessage);
            assertThat(outbox.getNextRetryAt()).isNotNull();
            assertThat(outbox.getNextRetryAt()).isAfter(LocalDateTime.now(ZoneId.systemDefault()));
        }

        @Test
        @DisplayName("✅ recordFailure() - 최대 재시도 초과 시 최종 실패")
        void recordFailure_MaxRetriesExceeded_UpdatesToFailed() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            final String errorMessage = "Permanent failure";
            final int maxRetries = 3;

            // when - 최대 횟수만큼 실패 기록
            for (int i = 0; i < maxRetries; i++)
                outbox.recordFailure(errorMessage + " " + (i + 1), maxRetries);

            // then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.FAILED);
            assertThat(outbox.getRetryCount()).isEqualTo(maxRetries);
            assertThat(outbox.getLastError()).contains("Permanent failure");
            assertThat(outbox.getNextRetryAt()).isNull();
        }

        @Test
        @DisplayName("✅ canRetry() - 재시도 가능 조건 확인")
        void canRetry_PendingStatusWithPastRetryTime_ReturnsTrue() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            outbox.setStatus(EmailStatus.PENDING);
            outbox.setNextRetryAt(LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(1));

            // when
            boolean canRetry = outbox.canRetry();

            // then
            assertThat(canRetry).isTrue();
        }

        @Test
        @DisplayName("❌ canRetry() - 실패 상태에서는 재시도 불가")
        void canRetry_FailedStatus_ReturnsFalse() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            outbox.setStatus(EmailStatus.FAILED);

            // when
            boolean canRetry = outbox.canRetry();

            // then
            assertThat(canRetry).isFalse();
        }

        @Test
        @DisplayName("❌ canRetry() - 미래 재시도 시간에는 재시도 불가")
        void canRetry_FutureRetryTime_ReturnsFalse() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            outbox.setStatus(EmailStatus.PENDING);
            outbox.setNextRetryAt(LocalDateTime.now(ZoneId.systemDefault()).plusHours(1));

            // when
            boolean canRetry = outbox.canRetry();

            // then
            assertThat(canRetry).isFalse();
        }

        @Test
        @DisplayName("✅ markAsProcessing() - 처리 중 상태로 변경")
        void markAsProcessing_UpdatesStatus_Success() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();

            // when
            outbox.markAsProcessing();

            // then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PROCESSING);
        }

        @Test
        @DisplayName("✅ 지수 백오프 재시도 시간 계산")
        void calculateNextRetryTime_ExponentialBackoff() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            final int maxRetries = 10;

            // when & then - 지수 백오프 확인 (1분, 2분, 4분, 8분...)
            LocalDateTime previousRetryTime = LocalDateTime.now(ZoneId.systemDefault());

            for (int i = 1; i <= 7; i++) {
                outbox.recordFailure("Test failure " + i, maxRetries);
                LocalDateTime nextRetryTime = outbox.getNextRetryAt();

                // 지수 백오프: 2^(i-1) 분
                int expectedDelayMinutes = (int) Math.pow(2, Math.min(i - 1, 6));

                if (i < 7) {
                    assertThat(nextRetryTime).isNotNull();
                    // 대략적인 시간 차이 확인 (±10초 허용)
                    long actualDelayMinutes = (nextRetryTime.toEpochSecond(java.time.ZoneOffset.UTC) - previousRetryTime.toEpochSecond(java.time.ZoneOffset.UTC)) / 60;
                    assertThat(actualDelayMinutes).isBetween(expectedDelayMinutes - 1L, expectedDelayMinutes + 1L);
                }

                previousRetryTime = LocalDateTime.now(ZoneId.systemDefault());
            }

            // 최대 지연 시간은 64분 (2^6)
            assertThat(outbox.getRetryCount()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("JPA 영속성 테스트")
    @Tag("jpa")
    class JpaPersistence {

        @Test
        @DisplayName("✅ EmailOutbox 저장 및 조회")
        void saveAndFind_EmailOutbox_Success() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();

            // when
            EmailOutbox saved = entityManager.persistAndFlush(outbox);
            entityManager.clear();

            EmailOutbox found = entityManager.find(EmailOutbox.class, saved.getId());

            // then
            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getRecipientEmail()).isEqualTo(saved.getRecipientEmail());
            assertThat(found.getStatus()).isEqualTo(EmailStatus.PENDING);
            assertThat(found.getCreatedAt()).isNotNull(); // BaseEntity 필드
            assertThat(found.getUpdatedAt()).isNotNull(); // BaseEntity 필드
        }

        @Test
        @DisplayName("✅ EmailOutbox 상태 업데이트")
        void updateStatus_EmailOutbox_Success() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            EmailOutbox saved = entityManager.persistAndFlush(outbox);

            // when
            saved.markAsSent();
            entityManager.flush();
            entityManager.clear();

            EmailOutbox updated = entityManager.find(EmailOutbox.class, saved.getId());

            // then
            assertThat(updated.getStatus()).isEqualTo(EmailStatus.SENT);
        }
    }
}
