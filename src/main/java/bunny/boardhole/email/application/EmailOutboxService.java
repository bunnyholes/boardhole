package bunny.boardhole.email.application;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.email.domain.EmailOutbox;
import bunny.boardhole.email.domain.EmailStatus;
import bunny.boardhole.email.infrastructure.EmailOutboxRepository;

/**
 * EmailOutbox 비즈니스 로직 서비스
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmailOutboxService {

    private final EmailOutboxRepository repository;

    @Value("${boardhole.email.outbox.max-retry-count}")
    private int maxRetryCount;

    @Value("${boardhole.email.outbox.retention-days}")
    private int retentionDays;

    /**
     * 실패한 이메일을 Outbox에 저장
     *
     * @param message 이메일 메시지
     * @param error   실패 원인
     */
    public void saveFailedEmail(EmailMessage message, Exception error) {
        // 이미 동일한 수신자에게 대기 중인 이메일이 있는지 확인 (중복 방지)
        if (repository.existsByRecipientEmailAndStatus(message.recipientEmail(), EmailStatus.PENDING)) {
            log.warn("이미 대기 중인 이메일이 존재합니다: to={}", message.recipientEmail());
            return;
        }

        EmailOutbox outbox = EmailOutbox.from(message);
        outbox.recordFailure(error.getMessage(), maxRetryCount);

        EmailOutbox saved = repository.save(outbox);
        log.info("실패한 이메일을 Outbox에 저장: id={}, to={}, retryCount={}", saved.getId(), saved.getRecipientEmail(), saved.getRetryCount());
    }

    /**
     * 재시도 가능한 이메일 목록 조회
     *
     * @return 재시도 가능한 이메일 목록
     */
    @Transactional(readOnly = true)
    public List<EmailOutbox> findRetriableEmails() {
        return repository.findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNull(EmailStatus.PENDING, LocalDateTime.now(ZoneId.systemDefault()));
    }

    /**
     * EmailOutbox 상태 업데이트
     *
     * @param outbox 업데이트할 EmailOutbox
     */
    public void updateStatus(EmailOutbox outbox) {
        repository.save(outbox);
        log.debug("EmailOutbox 상태 업데이트: id={}, status={}, retryCount={}", outbox.getId(), outbox.getStatus(), outbox.getRetryCount());
    }

    /**
     * 이메일 발송 성공 처리
     *
     * @param outboxId Outbox ID
     */
    public void markAsSent(Long outboxId) {
        repository.findById(outboxId).ifPresent(outbox -> {
            outbox.markAsSent();
            repository.save(outbox);
            log.info("이메일 발송 성공: outboxId={}, to={}", outbox.getId(), outbox.getRecipientEmail());
        });
    }

    /**
     * 이메일 발송 실패 기록
     *
     * @param outboxId Outbox ID
     * @param error    실패 원인
     */
    public void recordFailure(Long outboxId, String error) {
        repository.findById(outboxId).ifPresent(outbox -> {
            outbox.recordFailure(error, maxRetryCount);
            repository.save(outbox);
            log.warn("이메일 발송 실패: outboxId={}, retryCount={}/{}, error={}", outbox.getId(), outbox.getRetryCount(), maxRetryCount, error);
        });
    }

    /**
     * 오래된 이메일 정리 (보관 기간 초과)
     *
     * @return 삭제된 레코드 수
     */
    @Transactional
    public int cleanupOldEmails() {
        LocalDateTime cutoffDate = LocalDateTime.now(ZoneId.systemDefault()).minusDays(retentionDays);
        List<EmailStatus> statuses = List.of(EmailStatus.SENT, EmailStatus.FAILED);

        int deleted = repository.deleteOldEmails(statuses, cutoffDate);
        if (deleted > 0)
            log.info("오래된 Outbox 이메일 정리: {}개 삭제 ({}일 이전)", deleted, retentionDays);

        return deleted;
    }

    /**
     * Outbox 통계 조회
     *
     * @return 상태별 이메일 개수
     */
    @Transactional(readOnly = true)
    public EmailOutboxStatistics getStatistics() {
        return new EmailOutboxStatistics(repository.countByStatus(EmailStatus.PENDING), repository.countByStatus(EmailStatus.PROCESSING), repository.countByStatus(EmailStatus.SENT), repository.countByStatus(EmailStatus.FAILED));
    }

    /**
     * EmailOutbox 통계 정보
     */
    public record EmailOutboxStatistics(long pending, long processing, long sent, long failed) {
        public long total() {
            return pending + processing + sent + failed;
        }
    }
}
