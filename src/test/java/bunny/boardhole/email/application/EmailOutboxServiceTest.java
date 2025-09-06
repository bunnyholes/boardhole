package bunny.boardhole.email.application;

import bunny.boardhole.email.domain.*;
import bunny.boardhole.email.infrastructure.EmailOutboxRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@DisplayName("EmailOutboxService 테스트")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@Tag("service")
@Tag("email")
class EmailOutboxServiceTest {

    @Mock
    private EmailOutboxRepository repository;

    @InjectMocks
    private EmailOutboxService service;

    private static EmailMessage createTestEmailMessage() {
        return EmailMessage.create("test@example.com", "Test Subject", "<p>Test Content</p>");
    }

    private static EmailOutbox createTestEmailOutbox() {
        EmailOutbox outbox = EmailOutbox.from(createTestEmailMessage());
        outbox.setId(1L);
        return outbox;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxRetryCount", 10);
        ReflectionTestUtils.setField(service, "retentionDays", 30);
    }

    @Nested
    @DisplayName("실패 이메일 저장 테스트")
    @Tag("save")
    class SaveFailedEmailTest {

        @Test
        @DisplayName("✅ 실패한 이메일을 Outbox에 저장")
        void saveFailedEmail_Success() {
            // given
            EmailMessage message = createTestEmailMessage();
            Exception error = new RuntimeException("SMTP connection failed");
            EmailOutbox savedOutbox = createTestEmailOutbox();

            when(repository.existsByRecipientEmailAndStatus(anyString(), any(EmailStatus.class)))
                    .thenReturn(false);
            when(repository.save(any(EmailOutbox.class))).thenReturn(savedOutbox);

            // when
            service.saveFailedEmail(message, error);

            // then
            verify(repository).existsByRecipientEmailAndStatus("test@example.com", EmailStatus.PENDING);
            verify(repository)
                    .save(
                            argThat(
                                    outbox ->
                                            outbox.getRecipientEmail().equals("test@example.com")
                                                    && outbox.getSubject().equals("Test Subject")
                                                    && "SMTP connection failed".equals(outbox.getLastError())
                                                    && outbox.getRetryCount() == 1));
        }

        @Test
        @DisplayName("❌ 이미 대기 중인 이메일이 있으면 저장하지 않음")
        void saveFailedEmail_AlreadyPending_SkipsSave() {
            // given
            EmailMessage message = createTestEmailMessage();
            Exception error = new RuntimeException("Error");

            when(repository.existsByRecipientEmailAndStatus(anyString(), any(EmailStatus.class)))
                    .thenReturn(true);

            // when
            service.saveFailedEmail(message, error);

            // then
            verify(repository).existsByRecipientEmailAndStatus("test@example.com", EmailStatus.PENDING);
            verify(repository, never()).save(any(EmailOutbox.class));
        }
    }

    @Nested
    @DisplayName("재시도 가능 이메일 조회 테스트")
    @Tag("query")
    class FindRetriableEmailsTest {

        @Test
        @DisplayName("✅ 재시도 가능한 이메일 목록 조회")
        void findRetriableEmails_Success() {
            // given
            List<EmailOutbox> expectedEmails = List.of(createTestEmailOutbox(), createTestEmailOutbox());

            when(repository.findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNull(
                    any(EmailStatus.class), any(LocalDateTime.class)))
                    .thenReturn(expectedEmails);

            // when
            List<EmailOutbox> result = service.findRetriableEmails();

            // then
            assertThat(result).hasSize(2);
            verify(repository)
                    .findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNull(
                            eq(EmailStatus.PENDING), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("상태 업데이트 테스트")
    @Tag("update")
    class UpdateStatusTest {

        @Test
        @DisplayName("✅ EmailOutbox 상태 업데이트")
        void updateStatus_Success() {
            // given
            EmailOutbox outbox = createTestEmailOutbox();
            outbox.markAsSent();

            when(repository.save(any(EmailOutbox.class))).thenReturn(outbox);

            // when
            service.updateStatus(outbox);

            // then
            verify(repository).save(outbox);
        }

        @Test
        @DisplayName("✅ 이메일 발송 성공 처리")
        void markAsSent_Success() {
            // given
            final Long outboxId = 1L;
            EmailOutbox outbox = createTestEmailOutbox();

            when(repository.findById(outboxId)).thenReturn(Optional.of(outbox));
            when(repository.save(any(EmailOutbox.class))).thenReturn(outbox);

            // when
            service.markAsSent(outboxId);

            // then
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.SENT);
            verify(repository).findById(outboxId);
            verify(repository).save(outbox);
        }

        @Test
        @DisplayName("❌ 존재하지 않는 ID로 markAsSent 호출 시 무시")
        void markAsSent_NotFound_DoesNothing() {
            // given
            final Long outboxId = 999L;

            when(repository.findById(outboxId)).thenReturn(Optional.empty());

            // when
            service.markAsSent(outboxId);

            // then
            verify(repository).findById(outboxId);
            verify(repository, never()).save(any(EmailOutbox.class));
        }

        @Test
        @DisplayName("✅ 이메일 발송 실패 기록")
        void recordFailure_Success() {
            // given
            final Long outboxId = 1L;
            EmailOutbox outbox = createTestEmailOutbox();
            final String error = "Connection timeout";

            when(repository.findById(outboxId)).thenReturn(Optional.of(outbox));
            when(repository.save(any(EmailOutbox.class))).thenReturn(outbox);

            // when
            service.recordFailure(outboxId, error);

            // then
            assertThat(outbox.getRetryCount()).isEqualTo(1);
            assertThat(outbox.getLastError()).isEqualTo(error);
            verify(repository).findById(outboxId);
            verify(repository).save(outbox);
        }
    }

    @Nested
    @DisplayName("오래된 이메일 정리 테스트")
    @Tag("cleanup")
    class CleanupOldEmailsTest {

        @Test
        @DisplayName("✅ 보관 기간이 지난 이메일 삭제")
        void cleanupOldEmails_Success() {
            // given
            final int expectedDeleted = 5;

            when(repository.deleteOldEmails(anyList(), any(LocalDateTime.class)))
                    .thenReturn(expectedDeleted);

            // when
            int result = service.cleanupOldEmails();

            // then
            assertThat(result).isEqualTo(expectedDeleted);
            verify(repository)
                    .deleteOldEmails(
                            eq(List.of(EmailStatus.SENT, EmailStatus.FAILED)), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("✅ 삭제할 이메일이 없을 때")
        void cleanupOldEmails_NothingToDelete() {
            // given
            when(repository.deleteOldEmails(anyList(), any(LocalDateTime.class))).thenReturn(0);

            // when
            int result = service.cleanupOldEmails();

            // then
            assertThat(result).isEqualTo(0);
            verify(repository).deleteOldEmails(anyList(), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("통계 조회 테스트")
    @Tag("statistics")
    class GetStatisticsTest {

        @Test
        @DisplayName("✅ EmailOutbox 통계 조회")
        void getStatistics_Success() {
            // given
            when(repository.countByStatus(EmailStatus.PENDING)).thenReturn(5L);
            when(repository.countByStatus(EmailStatus.PROCESSING)).thenReturn(2L);
            when(repository.countByStatus(EmailStatus.SENT)).thenReturn(100L);
            when(repository.countByStatus(EmailStatus.FAILED)).thenReturn(3L);

            // when
            EmailOutboxService.EmailOutboxStatistics stats = service.getStatistics();

            // then
            assertThat(stats.pending()).isEqualTo(5L);
            assertThat(stats.processing()).isEqualTo(2L);
            assertThat(stats.sent()).isEqualTo(100L);
            assertThat(stats.failed()).isEqualTo(3L);
            assertThat(stats.total()).isEqualTo(110L);

            verify(repository, times(4)).countByStatus(any(EmailStatus.class));
        }
    }
}
