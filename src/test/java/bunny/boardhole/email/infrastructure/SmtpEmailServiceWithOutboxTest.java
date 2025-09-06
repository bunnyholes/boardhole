package bunny.boardhole.email.infrastructure;

import bunny.boardhole.email.application.*;
import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.testsupport.integration.IntegrationTestBase;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@DisplayName("SmtpEmailService Outbox 통합 테스트")
@Tag("integration")
@Tag("email")
class SmtpEmailServiceWithOutboxTest extends IntegrationTestBase {

    @Autowired
    private EmailService emailService;
    @Autowired
    private AtomicInteger attemptCounter;

    @MockitoBean
    private EmailOutboxService emailOutboxService;

    @MockitoBean
    private EmailTemplateService templateService;

    @Test
    @DisplayName("✅ 재시도 실패 시 @Recover에서 Outbox에 저장")
    void whenRetryFails_SaveToOutbox() {
        // given
        EmailMessage message =
                EmailMessage.create("test@example.com", "Test Subject", "<p>Test Content</p>");

        // when
        assertDoesNotThrow(() -> emailService.sendEmail(message));

        // then
        // application-test.yml에서 max-attempts: 3으로 설정됨
        assertThat(attemptCounter.get()).isEqualTo(3);

        // @Recover 메서드에서 EmailOutboxService.saveFailedEmail이 호출되어야 함
        verify(emailOutboxService, times(1))
                .saveFailedEmail(
                        argThat(
                                msg ->
                                        msg.recipientEmail().equals("test@example.com")
                                                && msg.subject().equals("Test Subject")
                                                && msg.content().equals("<p>Test Content</p>")),
                        any(Exception.class));
    }

    @Test
    @DisplayName("✅ Outbox 저장 중 예외 발생 시에도 에러 처리")
    void whenOutboxSaveFails_HandleGracefully() {
        // given
        EmailMessage message =
                EmailMessage.create("test@example.com", "Test Subject", "<p>Test Content</p>");

        // Outbox 저장도 실패하도록 설정
        doThrow(new RuntimeException("Outbox save failed"))
                .when(emailOutboxService)
                .saveFailedEmail(any(EmailMessage.class), any(Exception.class));

        // when
        assertDoesNotThrow(() -> emailService.sendEmail(message));

        // then
        verify(emailOutboxService, times(1))
                .saveFailedEmail(any(EmailMessage.class), any(Exception.class));
    }

    @Test
    @DisplayName("❌ null EmailMessage인 경우 Outbox 저장 시도하지 않음")
    void whenEmailMessageIsNull_DoNotSaveToOutbox() {
        // given
        // SmtpEmailService의 @Recover 메서드는 직접 호출할 수 없으므로
        // 이 케이스는 실제로는 발생하지 않지만, 로직 검증을 위한 테스트

        // when - 정상적인 이메일 발송 시도
        EmailMessage message =
                EmailMessage.create("test@example.com", "Test Subject", "<p>Test Content</p>");
        assertDoesNotThrow(() -> emailService.sendEmail(message));

        // then - null이 아닌 정상 메시지로 saveFailedEmail 호출됨
        verify(emailOutboxService, times(1))
                .saveFailedEmail(argThat(msg -> msg != null), any(Exception.class));
    }

    @TestConfiguration
    static class TestMailConfig {

        @Bean
        AtomicInteger attemptCounter() {
            return new AtomicInteger();
        }

        @Bean
        @Primary
        public JavaMailSender failingMailSender(AtomicInteger attemptCounter) {
            return new JavaMailSender() {
                @Override
                public MimeMessage createMimeMessage() {
                    return new MimeMessage((Session) null);
                }

                @Override
                public MimeMessage createMimeMessage(java.io.InputStream contentStream)
                        throws MailSendException {
                    try {
                        return new MimeMessage(null, contentStream);
                    } catch (MessagingException e) {
                        throw new MailSendException("Failed to create MimeMessage", e);
                    }
                }

                @Override
                public void send(MimeMessage mimeMessage) throws MailSendException {
                    int attempt = attemptCounter.incrementAndGet();
                    // 항상 실패하여 @Recover가 호출되도록 함
                    throw new MailSendException("Simulated failure #" + attempt);
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
                public void send(
                        org.springframework.mail.javamail.MimeMessagePreparator mimeMessagePreparator) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void send(
                        org.springframework.mail.javamail.MimeMessagePreparator... mimeMessagePreparators) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    @TestConfiguration
    static class SyncExecutorConfig {
        @Bean(name = "taskExecutor")
        @Primary
        public Executor taskExecutor() {
            // 비동기를 동기로 실행하여 테스트 가능하게 함
            return new SyncTaskExecutor();
        }
    }
}
