package bunny.boardhole.email.application;

import java.time.*;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 인증 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    @Value("${boardhole.email.verification-expiration-ms}")
    private long verificationExpirationMs;

    /**
     * 이메일 인증 처리
     *
     * @param id    사용자 ID
     * @param token 인증 토큰
     * @return 성공 메시지
     */
    @Transactional
    public String verifyEmail(Long id, String token) {
        EmailVerification verification = emailVerificationRepository.findByCodeAndUsedFalse(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.get("error.email-verification.invalid-token")));

        if (verification.isExpired()) throw new IllegalArgumentException(
                MessageUtils.get("error.email-verification.expired"));

        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.get("error.user.not-found.id", verification.getUserId())));

        // 인증 타입에 따른 처리
        if (verification.getVerificationType() == EmailVerificationType.SIGNUP) {
            // 회원가입 인증
            user.verifyEmail();
            user.changeEmail(verification.getNewEmail());
            emailService.sendWelcomeEmail(user);

        } else if (verification.getVerificationType() == EmailVerificationType.CHANGE_EMAIL) {
            // 이메일 변경 인증
            user.changeEmail(verification.getNewEmail());
            emailService.sendEmailChangedNotification(user, verification.getNewEmail());
        }

        verification.markAsUsed();
        userRepository.save(user);
        emailVerificationRepository.save(verification);

        return MessageUtils.get("success.email-verification.completed");
    }

    /**
     * 인증 이메일 재발송
     *
     * @param id 사용자 ID
     * @return 성공 메시지
     */
    @Transactional
    public String resendVerificationEmail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.get("error.user.not-found.id", id)));

        if (user.isEmailVerified()) throw new IllegalArgumentException(
                MessageUtils.get("error.email-verification.already-verified"));

        // 기존 미사용 토큰들을 만료 처리
        List<EmailVerification> existingVerifications = emailVerificationRepository.findByUserIdAndUsedFalse(user.getId());
        existingVerifications.forEach(verification -> {
            verification.markAsUsed();
            emailVerificationRepository.save(verification);
        });

        // 새 인증 토큰 생성 및 이메일 발송
        String newToken = java.util.UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.systemDefault())
                .plus(java.time.Duration.ofMillis(verificationExpirationMs));

        EmailVerification newVerification = EmailVerification.builder()
                .code(newToken)
                .userId(user.getId())
                .newEmail(user.getEmail())
                .expiresAt(expiresAt)
                .verificationType(EmailVerificationType.SIGNUP)
                .build();

        emailVerificationRepository.save(newVerification);
        emailService.sendSignupVerificationEmail(user, newToken);

        return MessageUtils.get("success.email-verification.resent");
    }
}
