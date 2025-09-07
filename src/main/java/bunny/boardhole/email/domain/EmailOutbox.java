package bunny.boardhole.email.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.lang.Nullable;

import bunny.boardhole.shared.domain.BaseEntity;

/**
 * 실패한 이메일을 저장하고 재시도를 관리하는 Outbox 엔티티
 */
@Entity
@Table(name = "email_outbox", indexes = {@Index(name = "idx_email_outbox_status", columnList = "status"), @Index(name = "idx_email_outbox_next_retry", columnList = "status,nextRetryAt")})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.PENDING;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private @Nullable String lastError;

    private @Nullable LocalDateTime nextRetryAt;

    /**
     * EmailMessage로부터 EmailOutbox 생성
     */
    public static EmailOutbox from(EmailMessage emailMessage) {
        EmailOutbox outbox = new EmailOutbox();
        outbox.recipientEmail = emailMessage.recipientEmail();
        outbox.subject = emailMessage.subject();
        outbox.content = emailMessage.content();
        outbox.status = EmailStatus.PENDING;
        outbox.retryCount = 0;
        return outbox;
    }

    /**
     * EmailOutbox를 EmailMessage로 변환
     */
    public EmailMessage toEmailMessage() {
        return EmailMessage.create(recipientEmail, subject, content);
    }

    /**
     * 이메일 발송 성공 처리
     */
    public void markAsSent() {
        status = EmailStatus.SENT;
        nextRetryAt = null;
    }

    /**
     * 이메일 발송 실패 기록
     *
     * @param error      실패 원인
     * @param maxRetries 최대 재시도 횟수
     */
    public void recordFailure(@Nullable String error, int maxRetries) {
        retryCount++;
        lastError = error;

        if (retryCount >= maxRetries) {
            status = EmailStatus.FAILED;
            nextRetryAt = null;
        } else {
            status = EmailStatus.PENDING;
            nextRetryAt = calculateNextRetryTime();
        }
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry() {
        return status == EmailStatus.PENDING && (nextRetryAt == null || nextRetryAt.isBefore(LocalDateTime.now(ZoneId.systemDefault())));
    }

    /**
     * 처리 중 상태로 변경
     */
    public void markAsProcessing() {
        status = EmailStatus.PROCESSING;
    }

    /**
     * 다음 재시도 시간 계산 (고정 10분 간격)
     */
    private LocalDateTime calculateNextRetryTime() {
        // 고정 10분 간격으로 재시도 (총 5회: 최초 1회 + 10분 후, 20분 후, 30분 후, 40분 후)
        return LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(10);
    }
}
