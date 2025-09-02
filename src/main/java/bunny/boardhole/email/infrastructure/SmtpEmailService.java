package bunny.boardhole.email.infrastructure;

import bunny.boardhole.email.application.*;
import bunny.boardhole.email.domain.*;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final MessageUtils messageUtils;

    @Value("${spring.mail.username:noreply@boardhole.com}")
    private String fromEmail;

    @Value("${boardhole.email.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Async
    public void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailMessage.getTo());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getContent(), true);

            if (emailMessage.getCc() != null && !emailMessage.getCc().isEmpty()) {
                helper.setCc(emailMessage.getCc().toArray(new String[0]));
            }

            if (emailMessage.getBcc() != null && !emailMessage.getBcc().isEmpty()) {
                helper.setBcc(emailMessage.getBcc().toArray(new String[0]));
            }

            mailSender.send(mimeMessage);
            log.info("이메일 발송 성공: to={}, subject={}", emailMessage.getTo(), emailMessage.getSubject());

        } catch (MessagingException | MailException e) {
            log.error("이메일 발송 실패: to={}, error={}", emailMessage.getTo(), e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    @Override
    public void sendTemplatedEmail(String to, EmailTemplate template, Map<String, Object> variables) {
        String processedContent = templateService.processTemplate(template, variables);
        EmailMessage emailMessage = EmailMessage.create(to, template.getDefaultSubject(), processedContent);
        sendEmail(emailMessage);
    }

    @Override
    public void sendSignupVerificationEmail(User user, String verificationToken) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
        
        Map<String, Object> variables = Map.of(
                "userName", user.getName(),
                "userEmail", user.getEmail(),
                "verificationUrl", verificationUrl,
                "expirationHours", 24
        );

        sendTemplatedEmail(user.getEmail(), EmailTemplate.SIGNUP_VERIFICATION, variables);
        log.info("회원가입 인증 이메일 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailChangeVerificationEmail(User user, String newEmail, String verificationToken) {
        String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
        
        Map<String, Object> variables = Map.of(
                "userName", user.getName(),
                "currentEmail", user.getEmail(),
                "newEmail", newEmail,
                "verificationUrl", verificationUrl,
                "expirationHours", 24
        );

        sendTemplatedEmail(newEmail, EmailTemplate.EMAIL_CHANGE_VERIFICATION, variables);
        log.info("이메일 변경 인증 이메일 발송: userId={}, newEmail={}", user.getId(), newEmail);
    }

    @Override
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = Map.of(
                "userName", user.getName(),
                "userEmail", user.getEmail(),
                "loginUrl", baseUrl + "/login"
        );

        sendTemplatedEmail(user.getEmail(), EmailTemplate.WELCOME, variables);
        log.info("환영 이메일 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendEmailChangedNotification(User user, String newEmail) {
        Map<String, Object> variables = Map.of(
                "userName", user.getName(),
                "newEmail", newEmail,
                "loginUrl", baseUrl + "/login"
        );

        sendTemplatedEmail(newEmail, EmailTemplate.EMAIL_CHANGED, variables);
        log.info("이메일 변경 완료 알림 발송: userId={}, newEmail={}", user.getId(), newEmail);
    }
}