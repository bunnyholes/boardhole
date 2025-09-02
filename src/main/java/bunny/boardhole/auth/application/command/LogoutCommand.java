package bunny.boardhole.auth.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 로그아웃 명령
 * CQRS 패턴의 Command 객체로 로그아웃 요청을 나타냅니다.
 */
@Schema(name = "LogoutCommand", description = "로그아웃 명령 - CQRS 패턴의 Command 객체")
public record LogoutCommand(
        @Schema(description = "로그아웃할 사용자 ID", example = "1")
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId
) {
    /**
     * 로그아웃 명령 객체를 생성하는 정적 팩토리 메서드
     *
     * @param userId 로그아웃할 사용자의 ID
     * @return 생성된 LogoutCommand 객체
     */
    public static LogoutCommand create(final Long userId) {
        return new LogoutCommand(userId);
    }
}