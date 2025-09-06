package bunny.boardhole.email.infrastructure;

import bunny.boardhole.email.application.*;
import bunny.boardhole.email.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@DisplayName("EmailRetryScheduler 테스트")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@Tag("unit")
@Tag("scheduler")
@Tag("email")
class EmailRetrySchedulerTest {

    @Mock
    private EmailOutboxService outboxService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailRetryScheduler scheduler;

    private static EmailOutbox createTestEmailOutbox(Long id, String email, EmailStatus status) {
        EmailMessage message = EmailMessage.create(email, "Test Subject", "Test Content");
        EmailOutbox outbox = EmailOutbox.from(message);
        outbox.setId(id);
        outbox.setStatus(status);
        return outbox;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "maxRetryCount", 10);
    }

    @Nested
    @DisplayName("실패 이메일 재시도 테스트")
    @Tag("retry")
    class RetryFailedEmailsTest {

        @Test
        @DisplayName("✅ PENDING 이메일 재시도 후 성공")
        void retryFailedEmails_Success() {
            // given
            EmailOutbox outbox1 = createTestEmailOutbox(1L, "test1@example.com", EmailStatus.PENDING);
            EmailOutbox outbox2 = createTestEmailOutbox(2L, "test2@example.com", EmailStatus.PENDING);
            List<EmailOutbox> pendingEmails = Arrays.asList(outbox1, outbox2);

            when(outboxService.findRetriableEmails()).thenReturn(pendingEmails);
            doNothing().when(emailService).sendEmail(any(EmailMessage.class));

            // when
            scheduler.retryFailedEmails();

            // then
            // 각 이메일이 처리됨
            assertThat(outbox1.getStatus()).isEqualTo(EmailStatus.SENT);
            assertThat(outbox2.getStatus()).isEqualTo(EmailStatus.SENT);

            // EmailService.sendEmail이 2번 호출됨
            verify(emailService, times(2)).sendEmail(any(EmailMessage.class));

            // 상태 업데이트가 각각 2번씩 (PROCESSING, SENT) 총 4번 호출됨
            verify(outboxService, times(4)).updateStatus(any(EmailOutbox.class));
        }

        @Test
        @DisplayName("✅ 일부 이메일 재시도 실패")
        void retryFailedEmails_PartialFailure() {
            // given
            EmailOutbox outbox1 = createTestEmailOutbox(1L, "success@example.com", EmailStatus.PENDING);
            EmailOutbox outbox2 = createTestEmailOutbox(2L, "fail@example.com", EmailStatus.PENDING);
            List<EmailOutbox> pendingEmails = Arrays.asList(outbox1, outbox2);

            when(outboxService.findRetriableEmails()).thenReturn(pendingEmails);

            // 첫 번째는 성공, 두 번째는 실패
            doNothing()
                    .doThrow(new RuntimeException("Send failed"))
                    .when(emailService)
                    .sendEmail(any(EmailMessage.class));

            // when
            scheduler.retryFailedEmails();

            // then
            assertThat(outbox1.getStatus()).isEqualTo(EmailStatus.SENT);
            assertThat(outbox2.getStatus()).isNotEqualTo(EmailStatus.SENT);
            assertThat(outbox2.getRetryCount()).isEqualTo(1);
            assertThat(outbox2.getLastError()).contains("Send failed");

            verify(emailService, times(2)).sendEmail(any(EmailMessage.class));
            verify(outboxService, times(4)).updateStatus(any(EmailOutbox.class));
        }

        @Test
        @DisplayName("✅ 재시도할 이메일이 없을 때")
        void retryFailedEmails_NoEmailsToRetry() {
            // given
            when(outboxService.findRetriableEmails()).thenReturn(Collections.emptyList());

            // when
            scheduler.retryFailedEmails();

            // then
            verify(emailService, never()).sendEmail(any(EmailMessage.class));
            verify(outboxService, never()).updateStatus(any(EmailOutbox.class));
        }

        @Test
        @DisplayName("✅ 최대 재시도 횟수 초과 시 FAILED 상태로 변경")
        void retryFailedEmails_MaxRetriesExceeded() {
            // given
            EmailOutbox outbox = createTestEmailOutbox(1L, "test@example.com", EmailStatus.PENDING);
            outbox.setRetryCount(9); // 이미 9번 시도함
            List<EmailOutbox> pendingEmails = Collections.singletonList(outbox);

            when(outboxService.findRetriableEmails()).thenReturn(pendingEmails);
            doThrow(new RuntimeException("Final failure"))
                    .when(emailService)
                    .sendEmail(any(EmailMessage.class));

            // when
            scheduler.retryFailedEmails();

            // then
            assertThat(outbox.getRetryCount()).isEqualTo(10);
            assertThat(outbox.getStatus()).isEqualTo(EmailStatus.FAILED);

            verify(emailService, times(1)).sendEmail(any(EmailMessage.class));
            verify(outboxService, times(2)).updateStatus(outbox); // PROCESSING, FAILED
        }
    }

    @Nested
    @DisplayName("오래된 이메일 정리 테스트")
    @Tag("cleanup")
    class CleanupOldEmailsTest {

        @Test
        @DisplayName("✅ 오래된 이메일 정리 성공")
        void cleanupOldEmails_Success() {
            // given
            when(outboxService.cleanupOldEmails()).thenReturn(10);

            // when
            scheduler.cleanupOldEmails();

            // then
            verify(outboxService, times(1)).cleanupOldEmails();
        }

        @Test
        @DisplayName("✅ 정리 중 예외 발생 시 처리")
        void cleanupOldEmails_HandleException() {
            // given
            when(outboxService.cleanupOldEmails()).thenThrow(new RuntimeException("Cleanup failed"));

            // when & then - 예외가 발생해도 스케줄러는 중단되지 않음
            assertDoesNotThrow(() -> scheduler.cleanupOldEmails());

            verify(outboxService, times(1)).cleanupOldEmails();
        }
    }

    @Nested
    @DisplayName("통계 로깅 테스트")
    @Tag("statistics")
    class LogStatisticsTest {

        @Test
        @DisplayName("✅ 통계 로깅 성공")
        void logStatistics_Success() {
            // given
            EmailOutboxService.EmailOutboxStatistics stats =
                    new EmailOutboxService.EmailOutboxStatistics(5L, 2L, 100L, 3L);
            when(outboxService.getStatistics()).thenReturn(stats);

            // when
            scheduler.logStatistics();

            // then
            verify(outboxService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("✅ 통계가 0일 때는 로깅하지 않음")
        void logStatistics_ZeroStats_NoLogging() {
            // given
            EmailOutboxService.EmailOutboxStatistics stats =
                    new EmailOutboxService.EmailOutboxStatistics(0L, 0L, 0L, 0L);
            when(outboxService.getStatistics()).thenReturn(stats);

            // when
            scheduler.logStatistics();

            // then
            verify(outboxService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("✅ 통계 조회 중 예외 발생 시 처리")
        void logStatistics_HandleException() {
            // given
            when(outboxService.getStatistics()).thenThrow(new RuntimeException("Statistics failed"));

            // when & then - 예외가 발생해도 스케줄러는 중단되지 않음
            assertDoesNotThrow(() -> scheduler.logStatistics());

            verify(outboxService, times(1)).getStatistics();
        }
    }
}
