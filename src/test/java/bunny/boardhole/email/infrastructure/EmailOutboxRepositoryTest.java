package bunny.boardhole.email.infrastructure;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.email.domain.EmailOutbox;
import bunny.boardhole.email.domain.EmailStatus;
import bunny.boardhole.testsupport.jpa.EntityTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailOutboxRepository 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("unit")
@Tag("repository")
class EmailOutboxRepositoryTest extends EntityTestBase {

    @Autowired
    private EmailOutboxRepository repository;

    private EmailOutbox createAndSaveOutbox(String email, EmailStatus status, @Nullable LocalDateTime nextRetryAt) {
        EmailOutbox outbox = EmailOutbox.from(EmailMessage.create(email, "Test Subject", "Test Content"));
        outbox.setStatus(status);
        outbox.setNextRetryAt(nextRetryAt);
        return repository.save(outbox);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("재시도 가능 이메일 조회 테스트")
    @Tag("query")
    class RetriableEmailsQuery {

        @Test
        @DisplayName("✅ PENDING 상태이고 재시도 시간이 지난 이메일 조회")
        void findRetriableEmails_WithPastRetryTime_Success() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            createAndSaveOutbox("test1@example.com", EmailStatus.PENDING, now.minusMinutes(5));
            createAndSaveOutbox("test2@example.com", EmailStatus.PENDING, now.plusMinutes(5)); // 미래
            createAndSaveOutbox("test3@example.com", EmailStatus.SENT, now.minusMinutes(5)); // 이미 발송
            createAndSaveOutbox("test4@example.com", EmailStatus.PENDING, null); // nextRetryAt이 null

            // when
            List<EmailOutbox> retriableEmails = repository.findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNull(EmailStatus.PENDING, now);

            // then
            assertThat(retriableEmails).hasSize(2);
            assertThat(retriableEmails).extracting(EmailOutbox::getRecipientEmail).containsExactlyInAnyOrder("test1@example.com", "test4@example.com");
        }

        @Test
        @DisplayName("✅ nextRetryAt이 null인 PENDING 이메일도 조회")
        void findRetriableEmails_WithNullRetryTime_Success() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            createAndSaveOutbox("test1@example.com", EmailStatus.PENDING, null);
            createAndSaveOutbox("test2@example.com", EmailStatus.PENDING, null);

            // when
            List<EmailOutbox> retriableEmails = repository.findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNull(EmailStatus.PENDING, now);

            // then
            assertThat(retriableEmails).hasSize(2);
        }
    }

    @Nested
    @DisplayName("상태별 카운트 테스트")
    @Tag("count")
    class StatusCountQuery {

        @Test
        @DisplayName("✅ 상태별 이메일 개수 카운트")
        void countByStatus_Success() {
            // given
            createAndSaveOutbox("test1@example.com", EmailStatus.PENDING, null);
            createAndSaveOutbox("test2@example.com", EmailStatus.PENDING, null);
            createAndSaveOutbox("test3@example.com", EmailStatus.PROCESSING, null);
            createAndSaveOutbox("test4@example.com", EmailStatus.SENT, null);
            createAndSaveOutbox("test5@example.com", EmailStatus.SENT, null);
            createAndSaveOutbox("test6@example.com", EmailStatus.SENT, null);
            createAndSaveOutbox("test7@example.com", EmailStatus.FAILED, null);

            // when & then
            assertThat(repository.countByStatus(EmailStatus.PENDING)).isEqualTo(2L);
            assertThat(repository.countByStatus(EmailStatus.PROCESSING)).isEqualTo(1L);
            assertThat(repository.countByStatus(EmailStatus.SENT)).isEqualTo(3L);
            assertThat(repository.countByStatus(EmailStatus.FAILED)).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("오래된 이메일 삭제 테스트")
    @Tag("delete")
    class DeleteOldEmailsQuery {

        @Test
        @DisplayName("✅ 특정 날짜 이전의 완료/실패 이메일 삭제")
        @Transactional
        void deleteOldEmails_Success() {
            // given
            LocalDateTime cutoffDate = LocalDateTime.now(ZoneId.systemDefault()).minusDays(30);
            LocalDateTime oldDate = cutoffDate.minusDays(1);

            // 오래된 이메일 (31일 전)
            EmailOutbox oldSent = createAndSaveOutbox("old1@example.com", EmailStatus.SENT, null);
            EmailOutbox oldFailed = createAndSaveOutbox("old2@example.com", EmailStatus.FAILED, null);

            // 최근 이메일
            createAndSaveOutbox("new1@example.com", EmailStatus.SENT, null);
            createAndSaveOutbox("new2@example.com", EmailStatus.FAILED, null);

            // PENDING 상태는 삭제하지 않음
            EmailOutbox oldPending = createAndSaveOutbox("old3@example.com", EmailStatus.PENDING, null);

            // Flush to ensure all entities are persisted
            entityManager.flush();

            // Update createdAt using native SQL to bypass JPA auditing
            entityManager.getEntityManager().createNativeQuery("UPDATE EMAIL_OUTBOX SET CREATED_AT = :oldDate WHERE ID IN (:ids)").setParameter("oldDate", oldDate).setParameter("ids", List.of(oldSent.getId(), oldFailed.getId(), oldPending.getId())).executeUpdate();

            entityManager.clear();

            // when
            int deletedCount = repository.deleteOldEmails(List.of(EmailStatus.SENT, EmailStatus.FAILED), cutoffDate);

            // then
            assertThat(deletedCount).isEqualTo(2);
            assertThat(repository.count()).isEqualTo(3L); // 최근 2개 + PENDING 1개
        }
    }

    @Nested
    @DisplayName("수신자별 대기 이메일 확인 테스트")
    @Tag("existence")
    class ExistenceCheckQuery {

        @Test
        @DisplayName("✅ 특정 수신자의 PENDING 이메일 존재 여부 확인")
        void existsByRecipientEmailAndStatus_Success() {
            // given
            final String email = "test@example.com";
            createAndSaveOutbox(email, EmailStatus.PENDING, null);
            createAndSaveOutbox(email, EmailStatus.SENT, null); // 다른 상태
            createAndSaveOutbox("other@example.com", EmailStatus.PENDING, null); // 다른 수신자

            // when & then
            assertThat(repository.existsByRecipientEmailAndStatus(email, EmailStatus.PENDING)).isTrue();
            assertThat(repository.existsByRecipientEmailAndStatus(email, EmailStatus.PROCESSING)).isFalse();
            assertThat(repository.existsByRecipientEmailAndStatus("nonexistent@example.com", EmailStatus.PENDING)).isFalse();
        }
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    @Tag("crud")
    class BasicCrudOperations {

        @Test
        @DisplayName("✅ EmailOutbox 저장 및 조회")
        void saveAndFindById_Success() {
            // given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("test@example.com", "Subject", "Content"));

            // when
            EmailOutbox saved = repository.save(outbox);
            EmailOutbox found = repository.findById(saved.getId()).orElse(null);

            // then
            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getRecipientEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("✅ EmailOutbox 업데이트")
        void updateEmailOutbox_Success() {
            // given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("test@example.com", "Subject", "Content"));
            EmailOutbox saved = repository.save(outbox);

            // when
            saved.markAsSent();
            repository.save(saved);
            EmailOutbox updated = repository.findById(saved.getId()).orElse(null);

            // then
            assertThat(updated).isNotNull();
            assertThat(updated.getStatus()).isEqualTo(EmailStatus.SENT);
        }

        @Test
        @DisplayName("✅ EmailOutbox 삭제")
        void deleteEmailOutbox_Success() {
            // given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("test@example.com", "Subject", "Content"));
            EmailOutbox saved = repository.save(outbox);

            // when
            repository.deleteById(saved.getId());

            // then
            assertThat(repository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("상태 전환 및 재시도 로직 테스트")
    class StatusTransitionAndRetryTest {

        @Test
        @DisplayName("PENDING → PROCESSING → SENT 상태 전환")
        void statusTransition_PendingToSent() {
            // Given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("transition@example.com", "Subject", "Content"));
            outbox = repository.save(outbox);

            // When - PENDING → PROCESSING
            outbox.markAsProcessing();
            outbox = repository.save(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PROCESSING);

            // When - PROCESSING → SENT
            outbox.markAsSent();
            outbox = repository.save(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.SENT);
            assertThat(outbox.getNextRetryAt()).isNull();
        }

        @Test
        @DisplayName("실패 후 재시도 횟수 증가 및 다음 재시도 시간 설정")
        void recordFailure_IncrementsRetryCount() {
            // Given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("retry@example.com", "Subject", "Content"));
            outbox = repository.save(outbox);

            // When - 첫 번째 실패
            outbox.recordFailure("Connection timeout", 3);
            outbox = repository.save(outbox);

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PENDING);
            assertThat(outbox.getNextRetryAt()).isNotNull();
            assertThat(outbox.getLastError()).isEqualTo("Connection timeout");

            // When - 두 번째 실패
            outbox.recordFailure("Authentication failed", 3);
            outbox = repository.save(outbox);

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(2);
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PENDING);

            // When - 세 번째 실패 (최대 재시도 횟수 도달)
            outbox.recordFailure("Maximum retries exceeded", 3);
            outbox = repository.save(outbox);

            // Then
            assertThat(outbox.getRetryCount()).isEqualTo(3);
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.FAILED);
            assertThat(outbox.getNextRetryAt()).isNull();
        }

        @Test
        @DisplayName("재시도 가능 여부 확인")
        void canRetry_ChecksCorrectly() {
            // Given
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

            // Case 1: PENDING + nextRetryAt is null
            EmailOutbox outbox1 = EmailOutbox.from(EmailMessage.create("test1@example.com", "Subject", "Content"));
            assertThat(outbox1.canRetry()).isTrue();

            // Case 2: PENDING + nextRetryAt is past
            EmailOutbox outbox2 = EmailOutbox.from(EmailMessage.create("test2@example.com", "Subject", "Content"));
            outbox2.setNextRetryAt(now.minusMinutes(5));
            assertThat(outbox2.canRetry()).isTrue();

            // Case 3: PENDING + nextRetryAt is future
            EmailOutbox outbox3 = EmailOutbox.from(EmailMessage.create("test3@example.com", "Subject", "Content"));
            outbox3.setNextRetryAt(now.plusMinutes(5));
            assertThat(outbox3.canRetry()).isFalse();

            // Case 4: SENT status
            EmailOutbox outbox4 = EmailOutbox.from(EmailMessage.create("test4@example.com", "Subject", "Content"));
            outbox4.markAsSent();
            assertThat(outbox4.canRetry()).isFalse();
        }
    }

    @Nested
    @DisplayName("EmailMessage 변환 테스트")
    class EmailMessageConversionTest {

        @Test
        @DisplayName("EmailMessage로부터 EmailOutbox 생성")
        void fromEmailMessage_CreatesCorrectly() {
            // Given
            EmailMessage message = EmailMessage.create("test@example.com", "Test Subject", "Test Content");

            // When
            EmailOutbox outbox = EmailOutbox.from(message);

            // Then
            assertThat(outbox.getRecipientEmail()).isEqualTo("test@example.com");
            assertThat(outbox.getSubject()).isEqualTo("Test Subject");
            assertThat(outbox.getContent()).isEqualTo("Test Content");
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("EmailOutbox를 EmailMessage로 변환")
        void toEmailMessage_ConvertsCorrectly() {
            // Given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("test@example.com", "Subject", "Content"));

            // When
            EmailMessage message = outbox.toEmailMessage();

            // Then
            assertThat(message.recipientEmail()).isEqualTo("test@example.com");
            assertThat(message.subject()).isEqualTo("Subject");
            assertThat(message.content()).isEqualTo("Content");
        }
    }

    @Nested
    @DisplayName("Auditing 기능 테스트")
    class AuditingTest {

        @Test
        @DisplayName("생성 시 createdAt, updatedAt 자동 설정")
        void save_NewOutbox_SetsAuditFields() {
            // Given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("audit@example.com", "Subject", "Content"));

            // When
            EmailOutbox saved = repository.save(outbox);

            // Then
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
        }

        @Test
        @DisplayName("수정 시 updatedAt 변경 확인")
        void update_ExistingOutbox_UpdatesAuditFields() {
            // Given
            EmailOutbox outbox = EmailOutbox.from(EmailMessage.create("update@example.com", "Subject", "Content"));
            EmailOutbox saved = repository.save(outbox);

            // When
            saved.markAsSent();
            EmailOutbox updated = repository.save(saved);

            // Then - updatedAt이 설정되어 있음을 확인
            assertThat(updated.getCreatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isNotNull();
        }
    }
}
