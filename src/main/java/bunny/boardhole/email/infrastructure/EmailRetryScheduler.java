package bunny.boardhole.email.infrastructure;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import bunny.boardhole.email.application.EmailOutboxService;
import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.email.domain.EmailOutbox;
import bunny.boardhole.email.domain.EmailStatus;

/**
 * 실패한 이메일을 주기적으로 재시도하는 스케줄러
 */
@Component
@ConditionalOnProperty(name = "boardhole.email.outbox.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class EmailRetryScheduler {

    private final EmailOutboxService outboxService;
    private final EmailService emailService;

    @Value("${boardhole.email.outbox.max-retry-count:10}")
    private int maxRetryCount;

    /**
     * 실패한 이메일 재시도 기본 5분마다 실행
     */
    @Scheduled(fixedDelayString = "${boardhole.email.outbox.scheduler-rate:300000}")
    public void retryFailedEmails() {
        List<EmailOutbox> pendingEmails = outboxService.findRetriableEmails();

        if (pendingEmails.isEmpty())
            return;

        log.info("Outbox 이메일 재시도 시작: {}개", pendingEmails.size());

        int successCount = 0;
        int failureCount = 0;

        for (EmailOutbox outbox : pendingEmails)
            try {
                // 처리 중 상태로 변경
                outbox.markAsProcessing();
                outboxService.updateStatus(outbox);

                // EmailMessage 생성 및 발송
                EmailMessage message = outbox.toEmailMessage();
                emailService.sendEmail(message);

                // 성공 처리
                outbox.markAsSent();
                outboxService.updateStatus(outbox);
                successCount++;

                log.info("Outbox 이메일 발송 성공: id={}, to={}", outbox.getId(), outbox.getRecipientEmail());

            } catch (Exception e) {
                // 실패 처리
                outbox.recordFailure(e.getMessage(), maxRetryCount);
                outboxService.updateStatus(outbox);
                failureCount++;

                if (outbox.getStatus() == EmailStatus.FAILED)
                    log.error("Outbox 이메일 최종 실패 (최대 재시도 횟수 초과): id={}, to={}, retryCount={}", outbox.getId(), outbox.getRecipientEmail(), outbox.getRetryCount());
                else
                    log.warn("Outbox 이메일 발송 실패 (재시도 예정): id={}, to={}, retryCount={}/{}, nextRetry={}", outbox.getId(), outbox.getRecipientEmail(), outbox.getRetryCount(), maxRetryCount, outbox.getNextRetryAt());
            }

        if (successCount > 0 || failureCount > 0)
            log.info("Outbox 이메일 재시도 완료: 성공={}, 실패={}", successCount, failureCount);
    }

    /**
     * 오래된 이메일 정리 (매일 새벽 2시 실행)
     */
    @Scheduled(cron = "${boardhole.email.outbox.cleanup-cron:0 0 2 * * *}")
    public void cleanupOldEmails() {
        try {
            int deleted = outboxService.cleanupOldEmails();
            if (deleted > 0)
                log.info("오래된 Outbox 이메일 정리 완료: {}개 삭제", deleted);
        } catch (Exception e) {
            log.error("오래된 Outbox 이메일 정리 실패", e);
        }
    }

    /**
     * Outbox 통계 로깅 (매시간 실행)
     */
    @Scheduled(cron = "${boardhole.email.outbox.stats-cron:0 0 * * * *}")
    public void logStatistics() {
        try {
            var stats = outboxService.getStatistics();
            if (stats.total() > 0)
                log.info("EmailOutbox 통계: 대기={}, 처리중={}, 완료={}, 실패={}, 전체={}", stats.pending(), stats.processing(), stats.sent(), stats.failed(), stats.total());
        } catch (Exception e) {
            log.error("Outbox 통계 조회 실패", e);
        }
    }
}
