package bunny.boardhole.user.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "UpdateEmailCommand", description = "이메일 변경 명령 - CQRS 패턴의 Command 객체")
public record UpdateEmailCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @NotBlank(message = "{user.validation.verification.code.required}")
        @Schema(description = "이메일 검증 코드", example = "ABC123")
        String verificationCode
) {
}