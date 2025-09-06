package bunny.boardhole.email.infrastructure;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.testsupport.integration.IntegrationTestBase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@Tag("email")
class SmtpEmailServiceRetryTest extends IntegrationTestBase {

    @org.springframework.beans.factory.annotation.Autowired
    EmailService emailService;
    @org.springframework.beans.factory.annotation.Autowired
    AtomicInteger attemptCounter;

    @Test
    @DisplayName("SMTP 전송 실패 시 재시도 후 성공")
    void shouldRetryAndSucceed() {
        EmailMessage msg = EmailMessage.create("test@example.com", "Hello", "<p>world</p>");
        assertDoesNotThrow(() -> emailService.sendEmail(msg));
        // 3번 실패 + 1번 성공 = 최소 4회 시도
        assertEquals(4, attemptCounter.get());
    }

    @TestConfiguration
    static class StubMailConfig {
        @Bean
        AtomicInteger attemptCounter() {
            return new AtomicInteger();
        }

        @Bean
        JavaMailSender javaMailSender(AtomicInteger attemptCounter) {
            return new JavaMailSender() {
                @Override
                public MimeMessage createMimeMessage() {
                    return new MimeMessage((Session) null);
                }

                @Override
                public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
                    try {
                        return new MimeMessage(null, contentStream);
                    } catch (MessagingException e) {
                        throw new MailSendException("stub createMimeMessage fail", e);
                    }
                }

                @Override
                public void send(MimeMessage mimeMessage) throws MailSendException {
                    int attempt = attemptCounter.incrementAndGet();
                    // 처음 3회 실패, 이후 성공
                    if (attempt <= 3)
                        throw new MailSendException("stub failure #" + attempt);
                }

                @Override
                public void send(MimeMessage... mimeMessages) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void send(org.springframework.mail.javamail.MimeMessagePreparator mimeMessagePreparator) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void send(org.springframework.mail.javamail.MimeMessagePreparator... mimeMessagePreparators) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    @TestConfiguration
    static class SyncAsyncConfig {
        @Bean(name = "taskExecutor")
        @org.springframework.context.annotation.Primary
        public java.util.concurrent.Executor taskExecutor() {
            return new org.springframework.core.task.SyncTaskExecutor();
        }
    }
}
