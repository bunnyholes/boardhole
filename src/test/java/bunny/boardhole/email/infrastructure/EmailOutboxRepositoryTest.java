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
@Tag("repository")
@Tag("email")
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
}
