package bunny.boardhole.testsupport.config;

import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.email.domain.EmailTemplate;
import bunny.boardhole.user.domain.User;

/**
 * 테스트용 이메일 서비스 설정
 * 실제 이메일 발송 대신 로그만 출력하는 Mock 구현체
 */
@TestConfiguration
public class TestEmailConfig {

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return new MockEmailService();
    }

    /**
     * 테스트용 Mock 이메일 서비스
     * 실제 이메일을 발송하지 않고 로그만 출력
     */
    static class MockEmailService implements EmailService {

        @Override
        public void sendEmail(@NonNull EmailMessage emailMessage) {
            // 실제 이메일 발송 대신 로그만 출력
            System.out.println("Mock Email Service: 이메일 발송 - to: " + emailMessage.recipientEmail() + ", subject: " + emailMessage.subject());
        }

        @Override
        public void sendTemplatedEmail(@NonNull String recipientEmail, @NonNull EmailTemplate emailTemplate, @NonNull Map<String, Object> templateVariables) {
            // 실제 템플릿 이메일 발송 대신 로그만 출력
            System.out.println("Mock Email Service: 템플릿 이메일 발송 - to: " + recipientEmail + ", template: " + emailTemplate);
        }

        @Override
        public void sendSignupVerificationEmail(@NonNull User user, @NonNull String verificationToken) {
            // 실제 회원가입 인증 이메일 발송 대신 로그만 출력
            System.out.println("Mock Email Service: 회원가입 인증 이메일 발송 - to: " + user.getEmail() + ", token: " + verificationToken);
        }

        @Override
        public void sendEmailChangeVerificationEmail(@NonNull User user, @NonNull String newEmail, @NonNull String verificationToken) {
            // 실제 이메일 변경 인증 이메일 발송 대신 로그만 출력
            System.out.println("Mock Email Service: 이메일 변경 인증 이메일 발송 - to: " + newEmail + ", token: " + verificationToken);
        }

        @Override
        public void sendWelcomeEmail(@NonNull User user) {
            // 실제 환영 이메일 발송 대신 로그만 출력
            System.out.println("Mock Email Service: 환영 이메일 발송 - to: " + user.getEmail());
        }

        @Override
        public void sendEmailChangedNotification(@NonNull User user, @NonNull String newEmail) {
            // 실제 이메일 변경 완료 알림 발송 대신 로그만 출력
            System.out.println("Mock Email Service: 이메일 변경 완료 알림 발송 - to: " + newEmail);
        }
    }
}
