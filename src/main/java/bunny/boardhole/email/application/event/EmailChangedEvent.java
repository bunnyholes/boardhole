package bunny.boardhole.email.application.event;

import bunny.boardhole.user.domain.User;

/**
 * 이메일 변경 완료 이벤트
 * 이메일 변경이 완료되었을 때 발행되며, 알림 메일 발송을 트리거합니다.
 */
public record EmailChangedEvent(
        User user,
        String oldEmail,
        String newEmail
) {
}