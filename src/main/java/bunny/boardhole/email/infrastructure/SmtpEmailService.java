package bunny.boardhole.email.infrastructure;

import bunny.boardhole.email.application.*;
import bunny.boardhole.email.domain.*;
import bunny.boardhole.user.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * SMTP 기반 이메일 발송 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    /**
     * 인증 토큰 만료 시간 (시간)
     */
    @Value("${boardhole.email.verification-expiration-hours:24}")
    private int verificationExpirationHours;
    @Value("${spring.mail.username:noreply@boardhole.com}")
    private String fromEmail;

    @Value("${boardhole.email.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Async
    public void sendEmail(final EmailMessage emailMessage) {
        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailMessage.recipientEmail());
            helper.setSubject(emailMessage.subject());
            helper.setText(emailMessage.content(), true);

            if (emailMessage.carbonCopy() != null && !emailMessage.carbonCopy().isEmpty()) {
                helper.setCc(emailMessage.carbonCopy().toArray(new String[0]));
            }

            if (emailMessage.blindCarbonCopy() != null && !emailMessage.blindCarbonCopy().isEmpty()) {
                helper.setBcc(emailMessage.blindCarbonCopy().toArray(new String[0]));
            }

            mailSender.send(mimeMessage);
            log.info("이메일 발송 성공: to={}, subject={}", emailMessage.recipientEmail(), emailMessage.subject());

        } catch (final MessagingException | MailException e) {
            log.error("이메일 발송 실패: to={}, error={}", emailMessage.recipientEmail(), e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    @Override
    public void sendTemplatedEmail(final String recipientEmail, final EmailTemplate emailTemplate, final Map<String, Object> templateVariables) {
        final String processedContent = templateService.processTemplate(emailTemplate, templateVariables);
        final EmailMessage emailMessage = EmailMessage.create(recipientEmail, emailTemplate.getDefaultSubject(), processedContent);
        sendEmail(emailMessage);
    }

    @Override
    public void sendSignupVerificationEmail(final User user, final String verificationToken) {
        final String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

        final Map<String, Object> templateVariables = Map.of(
                "userName", user.getName(),
                "userEmail", user.getEmail(),
                "verificationUrl", verificationUrl,
                "expirationHours", verificationExpirationHours
        );

        sendTemplatedEmail(user.getEmail(), EmailTemplate.SIGNUP_VERIFICATION, templateVariables);
        log.info("회원가입 인증 이메일 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailChangeVerificationEmail(final User user, final String newEmail, final String verificationToken) {
        final String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

        final Map<String, Object> templateVariables = Map.of(
                "userName", user.getName(),
                "currentEmail", user.getEmail(),
                "newEmail", newEmail,
                "verificationUrl", verificationUrl,
                "expirationHours", verificationExpirationHours
        );

        sendTemplatedEmail(newEmail, EmailTemplate.EMAIL_CHANGE_VERIFICATION, templateVariables);
        log.info("이메일 변경 인증 이메일 발송: userId={}, newEmail={}", user.getId(), newEmail);
    }

    @Override
    public void sendWelcomeEmail(final User user) {
        final Map<String, Object> templateVariables = Map.of(
                "userName", user.getName(),
                "userEmail", user.getEmail(),
                "loginUrl", baseUrl + "/login"
        );

        sendTemplatedEmail(user.getEmail(), EmailTemplate.WELCOME, templateVariables);
        log.info("환영 이메일 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailChangedNotification(final User user, final String newEmail) {
        final Map<String, Object> templateVariables = Map.of(
                "userName", user.getName(),
                "newEmail", newEmail,
                "loginUrl", baseUrl + "/login"
        );

        sendTemplatedEmail(newEmail, EmailTemplate.EMAIL_CHANGED, templateVariables);
        log.info("이메일 변경 완료 알림 발송: userId={}, newEmail={}", user.getId(), newEmail);
    }
}