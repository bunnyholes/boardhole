package bunny.boardhole.user.application.event;

import bunny.boardhole.user.domain.User;

/**
 * 사용자 생성 이벤트
 * 회원가입 완료 시 발행되며, 이메일 인증 메일 발송을 트리거합니다.
 */
public record UserCreatedEvent(User user, String verificationToken, java.time.LocalDateTime expiresAt) {
}