package bunny.boardhole.email.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.user.application.event.UserCreatedEvent;

/**
 * 이메일 관련 이벤트 리스너
 * 트랜잭션 완료 후 비동기로 이메일을 발송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    /**
     * 회원가입 이벤트 처리
     * 트랜잭션 커밋 후 비동기로 인증 이메일을 발송합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreated(UserCreatedEvent event) {
        try {
            log.debug("Sending signup verification email to user: {}", event.user().getUsername());
            emailService.sendSignupVerificationEmail(event.user(), event.verificationToken());
            log.info("Signup verification email sent successfully to: {}", event.user().getEmail());
        } catch (Exception e) {
            log.error("Failed to send signup verification email to: {}", event.user().getEmail(), e);
            // TODO: 이메일 발송 실패 시 재시도 로직 또는 실패 알림 처리
        }
    }

    /**
     * 이메일 변경 요청 이벤트 처리
     * 트랜잭션 커밋 후 비동기로 인증 이메일을 발송합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailVerificationRequested(EmailVerificationRequestedEvent event) {
        try {
            log.debug("Sending email change verification to user: {}", event.user().getUsername());
            emailService.sendEmailChangeVerificationEmail(event.user(), event.newEmail(), event.verificationCode());
            log.info("Email change verification sent successfully to: {}", event.newEmail());
        } catch (Exception e) {
            log.error("Failed to send email change verification to: {}", event.newEmail(), e);
            // TODO: 이메일 발송 실패 시 재시도 로직 또는 실패 알림 처리
        }
    }

    /**
     * 이메일 변경 완료 이벤트 처리
     * 트랜잭션 커밋 후 비동기로 알림 이메일을 발송합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailChanged(EmailChangedEvent event) {
        try {
            log.debug("Sending email change notification to user: {}", event.user().getUsername());
            emailService.sendEmailChangedNotification(event.user(), event.newEmail());
            log.info("Email change notification sent successfully to both {} and {}", event.oldEmail(), event.newEmail());
        } catch (Exception e) {
            log.error("Failed to send email change notification", e);
            // TODO: 이메일 발송 실패 시 재시도 로직 또는 실패 알림 처리
        }
    }
}