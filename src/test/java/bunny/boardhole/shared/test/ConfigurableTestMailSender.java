package bunny.boardhole.shared.test;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.mail.*;
import org.springframework.mail.javamail.*;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;

/** 테스트용 JavaMailSender 구현체 설정 가능한 실패 횟수를 통해 재시도 시나리오를 테스트할 수 있습니다. */
public class ConfigurableTestMailSender implements JavaMailSender {

  private final AtomicInteger attemptCount = new AtomicInteger(0);
  private final int failuresBeforeSuccess;
  private final JavaMailSender delegateMailSender;
  private final String failureMessage;

  /**
   * @param failuresBeforeSuccess 성공 전 실패 횟수 (0: 항상 성공, Integer.MAX_VALUE: 항상 실패)
   * @param delegateMailSender 실제 메일 발송을 위한 delegate (GreenMail 등)
   */
  public ConfigurableTestMailSender(int failuresBeforeSuccess, JavaMailSender delegateMailSender) {
    this(failuresBeforeSuccess, delegateMailSender, "Simulated mail failure");
  }

  /**
   * @param failuresBeforeSuccess 성공 전 실패 횟수
   * @param delegateMailSender 실제 메일 발송을 위한 delegate
   * @param failureMessage 실패 시 반환할 메시지
   */
  private ConfigurableTestMailSender(
      int failuresBeforeSuccess, JavaMailSender delegateMailSender, String failureMessage) {
    this.failuresBeforeSuccess = failuresBeforeSuccess;
    this.delegateMailSender = delegateMailSender;
    this.failureMessage = failureMessage;
  }

  @Override
  public MimeMessage createMimeMessage() {
    return delegateMailSender != null
        ? delegateMailSender.createMimeMessage()
        : new MimeMessage((Session) null);
  }

  @Override
  public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
    if (delegateMailSender != null) return delegateMailSender.createMimeMessage(contentStream);
    try {
      return new MimeMessage(null, contentStream);
    } catch (MessagingException e) {
      throw new MailSendException("Failed to create MimeMessage", e);
    }
  }

  @Override
  public void send(MimeMessage mimeMessage) throws MailException {
    int attempt = attemptCount.incrementAndGet();

    // 설정된 실패 횟수만큼 실패
    if (attempt <= failuresBeforeSuccess)
      throw new MailSendException(failureMessage + " (attempt " + attempt + ")");

    // 이후에는 성공 (delegate가 있으면 실제 발송)
    if (delegateMailSender != null) delegateMailSender.send(mimeMessage);
  }

  @Override
  public void send(MimeMessage... mimeMessages) throws MailException {
    for (MimeMessage message : mimeMessages) send(message);
  }

  @Override
  public void send(SimpleMailMessage simpleMessage) throws MailException {
    int attempt = attemptCount.incrementAndGet();

    if (attempt <= failuresBeforeSuccess)
      throw new MailSendException(failureMessage + " (attempt " + attempt + ")");

    if (delegateMailSender != null) delegateMailSender.send(simpleMessage);
  }

  @Override
  public void send(SimpleMailMessage... simpleMessages) throws MailException {
    for (SimpleMailMessage message : simpleMessages) send(message);
  }

  @Override
  public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
    MimeMessage message = createMimeMessage();
    try {
      mimeMessagePreparator.prepare(message);
    } catch (Exception e) {
      throw new MailSendException("Failed to prepare message", e);
    }
    send(message);
  }

  @Override
  public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
    for (MimeMessagePreparator preparator : mimeMessagePreparators) send(preparator);
  }

  /** 현재까지의 시도 횟수를 반환합니다. */
  public int getAttemptCount() {
    return attemptCount.get();
  }

  /** 시도 횟수를 초기화합니다. */
  public void reset() {
    attemptCount.set(0);
  }

  /** 다음 시도가 성공할지 여부를 반환합니다. */
  public boolean willNextAttemptSucceed() {
    return attemptCount.get() >= failuresBeforeSuccess;
  }
}
