package bunny.boardhole.email.application.event;

import bunny.boardhole.user.domain.User;

/**
 * 이메일 변경 인증 요청 이벤트
 * 이메일 변경 요청 시 발행되며, 인증 메일 발송을 트리거합니다.
 */
public record EmailVerificationRequestedEvent(
        User user,
        String newEmail,
        String verificationCode,
        java.time.LocalDateTime expiresAt
) {
}