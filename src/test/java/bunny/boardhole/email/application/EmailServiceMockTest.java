package bunny.boardhole.email.application;

import bunny.boardhole.email.domain.EmailTemplate;
import bunny.boardhole.email.infrastructure.SmtpEmailService;
import bunny.boardhole.user.domain.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceMockTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateService templateService;


    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private SmtpEmailService emailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("password")
                .name("테스트 사용자")
                .email("test@example.com")
                .roles(Set.of(Role.USER))
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // @Value 필드 모킹
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.boardhole.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("회원가입 인증 이메일 발송 - Mock 테스트")
    void sendSignupVerificationEmail_Mock() {
        // given
        String verificationToken = "test-token-123";
        String processedTemplate = "<html>인증 이메일 내용</html>";

        when(templateService.processTemplate(eq(EmailTemplate.SIGNUP_VERIFICATION), any(Map.class)))
                .thenReturn(processedTemplate);

        // when
        emailService.sendSignupVerificationEmail(testUser, verificationToken);

        // then
        verify(mailSender).send(mimeMessage);
        verify(templateService).processTemplate(eq(EmailTemplate.SIGNUP_VERIFICATION), any(Map.class));
    }

    @Test
    @DisplayName("환영 이메일 발송 - Mock 테스트")
    void sendWelcomeEmail_Mock() {
        // given
        String processedTemplate = "<html>환영 이메일 내용</html>";

        when(templateService.processTemplate(eq(EmailTemplate.WELCOME), any(Map.class)))
                .thenReturn(processedTemplate);

        // when
        emailService.sendWelcomeEmail(testUser);

        // then
        verify(mailSender).send(mimeMessage);
        verify(templateService).processTemplate(eq(EmailTemplate.WELCOME), any(Map.class));
    }

    @Test
    @DisplayName("이메일 변경 인증 발송 - Mock 테스트")
    void sendEmailChangeVerificationEmail_Mock() {
        // given
        String newEmail = "newemail@example.com";
        String verificationToken = "change-token-123";
        String processedTemplate = "<html>이메일 변경 인증 내용</html>";

        when(templateService.processTemplate(eq(EmailTemplate.EMAIL_CHANGE_VERIFICATION), any(Map.class)))
                .thenReturn(processedTemplate);

        // when
        emailService.sendEmailChangeVerificationEmail(testUser, newEmail, verificationToken);

        // then
        verify(mailSender).send(mimeMessage);
        verify(templateService).processTemplate(eq(EmailTemplate.EMAIL_CHANGE_VERIFICATION), any(Map.class));
    }
}