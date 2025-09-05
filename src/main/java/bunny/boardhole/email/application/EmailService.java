package bunny.boardhole.email.application;

import java.util.Map;

import bunny.boardhole.email.domain.*;
import bunny.boardhole.user.domain.User;

/**
 * 이메일 발송 서비스 인터페이스
 */
public interface EmailService {

    /**
     * 단순 이메일 발송
     *
     * @param emailMessage 이메일 메시지
     */
    void sendEmail(EmailMessage emailMessage);

    /**
     * 템플릿 기반 이메일 발송
     *
     * @param recipientEmail    받는 사람 이메일
     * @param emailTemplate     이메일 템플릿
     * @param templateVariables 템플릿 변수들
     */
    void sendTemplatedEmail(String recipientEmail, EmailTemplate emailTemplate, Map<String, Object> templateVariables);

    /**
     * 회원가입 인증 이메일 발송
     *
     * @param user              사용자 정보
     * @param verificationToken 인증 토큰
     */
    void sendSignupVerificationEmail(User user, String verificationToken);

    /**
     * 이메일 변경 인증 이메일 발송
     *
     * @param user              사용자 정보
     * @param newEmail          새 이메일 주소
     * @param verificationToken 인증 토큰
     */
    void sendEmailChangeVerificationEmail(User user, String newEmail, String verificationToken);

    /**
     * 환영 이메일 발송 (인증 완료 후)
     *
     * @param user 사용자 정보
     */
    void sendWelcomeEmail(User user);

    /**
     * 이메일 변경 완료 알림
     *
     * @param user     사용자 정보
     * @param newEmail 새 이메일 주소
     */
    void sendEmailChangedNotification(User user, String newEmail);
}