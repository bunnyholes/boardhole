package bunny.boardhole.auth.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * 로그아웃 명령
 * CQRS 패턴의 Command 객체로 로그아웃 요청을 나타냅니다.
 */
public record LogoutCommand(
        @NotNull(message = "{validation.user.userId.required}") UUID userId) {
}
