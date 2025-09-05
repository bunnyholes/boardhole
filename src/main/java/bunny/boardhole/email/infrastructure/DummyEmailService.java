package bunny.boardhole.email.infrastructure;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.email.domain.*;
import bunny.boardhole.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 더미 이메일 서비스 (개발/테스트용) 실제 이메일을 발송하지 않고 로그만 출력합니다.
 */
@Service
@Slf4j
public class DummyEmailService implements EmailService {

    @Override
    public void sendEmail(final EmailMessage emailMessage) {
        log.info("📧 [Dummy Email] 더미 이메일 발송 (실제 발송 안함)");
        log.debug("  - To: {}", emailMessage.recipientEmail());
        log.debug("  - Subject: {}", emailMessage.subject());
    }

    @Override
    public void sendTemplatedEmail(
            final String recipientEmail,
            final EmailTemplate emailTemplate,
            final Map<String, Object> templateVariables) {
        log.info("📧 [Dummy Email] 템플릿 이메일 (더미)");
        log.debug("  - Template: {}", emailTemplate);
        log.debug("  - To: {}", recipientEmail);
    }

    @Override
    public void sendSignupVerificationEmail(final User user, final String verificationToken) {
        log.info("📧 [Dummy Email] 회원가입 인증 이메일 (더미)");
        log.info("  - User: {}", user.getEmail());
        log.info("  - Token: {}", verificationToken);
        log.info("  - 실제 이메일은 발송되지 않았습니다.");
    }

    @Override
    public void sendEmailChangeVerificationEmail(
            final User user, final String newEmail, final String verificationToken) {
        log.info("📧 [Dummy Email] 이메일 변경 인증 (더미)");
        log.info("  - New Email: {}", newEmail);
        log.info("  - Token: {}", verificationToken);
        log.info("  - 실제 이메일은 발송되지 않았습니다.");
    }

    @Override
    public void sendWelcomeEmail(final User user) {
        log.info("📧 [Dummy Email] 환영 이메일 (더미): {}", user.getEmail());
    }

    @Override
    public void sendEmailChangedNotification(final User user, final String newEmail) {
        log.info("📧 [Dummy Email] 이메일 변경 알림 (더미): {}", newEmail);
    }
}
