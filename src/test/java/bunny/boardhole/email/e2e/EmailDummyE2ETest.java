package bunny.boardhole.email.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.email.domain.EmailMessage;
import bunny.boardhole.email.infrastructure.DummyEmailService;
import bunny.boardhole.user.domain.User;

/** 더미 이메일 서비스 E2E 테스트 DummyEmailService가 기본으로 활성화되는지 검증 */
@SpringBootTest
@TestPropertySource(properties = "boardhole.security.require-email-verification=false")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("📧 더미 이메일 E2E 테스트")
class EmailDummyE2ETest {

  @Autowired private EmailService emailService;

  @Test
  @DisplayName("1️⃣ DummyEmailService가 기본으로 활성화됨")
  void dummyEmailServiceIsDefault() {
    // given & when & then
    assertThat(emailService).isNotNull().isInstanceOf(DummyEmailService.class);
  }

  @Test
  @DisplayName("2️⃣ 더미 이메일 발송시 예외 없이 처리됨")
  void sendEmailWithDummy() {
    // given
    EmailMessage emailMessage = EmailMessage.create("test@example.com", "테스트 제목", "<p>테스트 내용</p>");

    // when & then - 예외 없이 실행됨
    assertThat(emailService).isNotNull();
    emailService.sendEmail(emailMessage);
  }

  @Test
  @DisplayName("3️⃣ 회원가입 인증 이메일이 더미로 처리됨")
  void sendSignupVerificationWithDummy() {
    // given
    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .name("테스트 사용자")
            .password("password123")
            .build();
    final String token = "DUMMY-TOKEN-123";

    // when & then - 예외 없이 실행됨
    emailService.sendSignupVerificationEmail(user, token);
  }

  @Test
  @DisplayName("4️⃣ 이메일 변경 인증이 더미로 처리됨")
  void sendEmailChangeVerificationWithDummy() {
    // given
    User user =
        User.builder()
            .username("testuser")
            .email("old@example.com")
            .name("테스트 사용자")
            .password("password123")
            .build();
    final String newEmail = "new@example.com";
    final String token = "CHANGE-TOKEN-456";

    // when & then - 예외 없이 실행됨
    emailService.sendEmailChangeVerificationEmail(user, newEmail, token);
  }

  @Test
  @DisplayName("5️⃣ 환영 이메일이 더미로 처리됨")
  void sendWelcomeEmailWithDummy() {
    // given
    User user =
        User.builder()
            .username("welcomeuser")
            .email("welcome@example.com")
            .name("환영 사용자")
            .password("password123")
            .build();

    // when & then - 예외 없이 실행됨
    emailService.sendWelcomeEmail(user);
  }

  @Test
  @DisplayName("6️⃣ 이메일 변경 알림이 더미로 처리됨")
  void sendEmailChangedNotificationWithDummy() {
    // given
    User user =
        User.builder()
            .username("changeuser")
            .email("original@example.com")
            .name("변경 사용자")
            .password("password123")
            .build();
    final String newEmail = "changed@example.com";

    // when & then - 예외 없이 실행됨
    emailService.sendEmailChangedNotification(user, newEmail);
  }
}
