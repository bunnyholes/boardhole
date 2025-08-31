package bunny.boardhole.auth.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 명령
 * CQRS 패턴의 Command 객체로 로그인 요청을 나타냅니다.
 */
@Schema(name = "LoginCommand", description = "로그인 명령 - CQRS 패턴의 Command 객체")
public record LoginCommand(
        @Schema(description = "사용자명 또는 이메일", example = "admin")
        @NotBlank(message = "사용자명은 필수입니다.")
        String username,

        @Schema(description = "비밀번호", example = "admin123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
