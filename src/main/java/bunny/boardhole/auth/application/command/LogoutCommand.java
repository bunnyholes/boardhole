package bunny.boardhole.auth.application.command;

import jakarta.validation.constraints.*;

/**
 * 로그아웃 명령
 * CQRS 패턴의 Command 객체로 로그아웃 요청을 나타냅니다.
 */
public record LogoutCommand(
        @NotNull(message = "{user.validation.userId.required}") @Positive(message = "{user.validation.userId.positive}") Long userId
) {
}