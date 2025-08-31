package bunny.boardhole.auth.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 로그아웃 명령
 * CQRS 패턴의 Command 객체로 로그아웃 요청을 나타냅니다.
 */
@Schema(name = "LogoutCommand", description = "로그아웃 명령 - CQRS 패턴의 Command 객체")
public record LogoutCommand(
        @Schema(description = "로그아웃할 사용자 ID", example = "1")
        @NotNull
        Long userId
) {
}