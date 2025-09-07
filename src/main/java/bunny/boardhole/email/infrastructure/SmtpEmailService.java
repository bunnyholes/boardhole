package bunny.boardhole.email.infrastructure;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.email.application.EmailTemplateService;
import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.email.domain.EmailTemplate;
import bunny.boardhole.user.domain.User;

/**
 * SMTP 기반 이메일 발송 서비스 구현체
 */
@Slf4j
@Service
@Primary
@Profile("smtp")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    /**
     * 인증 토큰 만료 시간 (시간)
     */
    @Value("${boardhole.validation.email-verification.expiration-ms}")
    private long verificationExpirationMs;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${boardhole.email.base-url}")
    private String baseUrl;

    @Recover
    static void recover(Exception e, EmailMessage emailMessage) {
        // 영구 실패 시 아웃박스/DLQ에 적재하는 전략으로 확장 가능
        log.error("[RETRY-RECOVER] 이메일 발송 최종 실패: to={}, subject={}, cause={}", emailMessage != null ? emailMessage.recipientEmail() : "<null>", emailMessage != null ? emailMessage.subject() : "<null>", e.getMessage(), e);
        // TODO: Outbox 저장 후 배치/스케줄러로 재시도
    }

    @Override
    @Async
    @Retryable(retryFor = {MailException.class, MessagingException.class}, maxAttempts = 5, backoff = @Backoff(delay = 500, multiplier = 2.0, random = true))
    public void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailMessage.recipientEmail());
            helper.setSubject(emailMessage.subject());
            helper.setText(emailMessage.content(), true);

            if (emailMessage.carbonCopy() != null && !emailMessage.carbonCopy().isEmpty())
                helper.setCc(emailMessage.carbonCopy().toArray(new String[0]));

            if (emailMessage.blindCarbonCopy() != null && !emailMessage.blindCarbonCopy().isEmpty())
                helper.setBcc(emailMessage.blindCarbonCopy().toArray(new String[0]));

            mailSender.send(mimeMessage);
            log.info("이메일 발송 성공: to={}, subject={}", emailMessage.recipientEmail(), emailMessage.subject());

        } catch (MessagingException | MailException e) {
            log.error("이메일 발송 실패: to={}, error={}", emailMessage.recipientEmail(), e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    @Override
    public void sendTemplatedEmail(String recipientEmail, EmailTemplate emailTemplate, Map<String, Object> templateVariables) {
        String processedContent = templateService.processTemplate(emailTemplate, templateVariables);
        EmailMessage emailMessage = EmailMessage.create(recipientEmail, emailTemplate.getDefaultSubject(), processedContent);
        sendEmail(emailMessage);
    }

    @Override
    public void sendSignupVerificationEmail(User user, String verificationToken) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

        Map<String, Object> templateVariables = Map.of("userName", user.getName(), "userEmail", user.getEmail(), "verificationUrl", verificationUrl, "expirationHours", Math.max(1, (int) java.time.Duration.ofMillis(verificationExpirationMs).toHours()));

        sendTemplatedEmail(user.getEmail(), EmailTemplate.SIGNUP_VERIFICATION, templateVariables);
        log.info("회원가입 인증 이메일 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailChangeVerificationEmail(User user, String newEmail, String verificationToken) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

        Map<String, Object> templateVariables = Map.of("userName", user.getName(), "currentEmail", user.getEmail(), "newEmail", newEmail, "verificationUrl", verificationUrl, "expirationHours", Math.max(1, (int) java.time.Duration.ofMillis(verificationExpirationMs).toHours()));

        sendTemplatedEmail(newEmail, EmailTemplate.EMAIL_CHANGE_VERIFICATION, templateVariables);
        log.info("이메일 변경 인증 이메일 발송: userId={}, newEmail={}", user.getId(), newEmail);
    }

    @Override
    public void sendWelcomeEmail(User user) {
        Map<String, Object> templateVariables = Map.of("userName", user.getName(), "userEmail", user.getEmail(), "loginUrl", baseUrl + "/login");

        sendTemplatedEmail(user.getEmail(), EmailTemplate.WELCOME, templateVariables);
        log.info("환영 이메일 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailChangedNotification(User user, String newEmail) {
        Map<String, Object> templateVariables = Map.of("userName", user.getName(), "newEmail", newEmail, "loginUrl", baseUrl + "/login");

        sendTemplatedEmail(newEmail, EmailTemplate.EMAIL_CHANGED, templateVariables);
        log.info("이메일 변경 완료 알림 발송: userId={}, newEmail={}", user.getId(), newEmail);
    }
}
