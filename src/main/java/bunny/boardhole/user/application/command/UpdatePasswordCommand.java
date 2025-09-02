package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "UpdatePasswordCommand", description = "패스워드 변경 명령 - CQRS 패턴의 Command 객체")
public record UpdatePasswordCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @NotBlank(message = "{user.validation.password.current.required}")
        @Schema(description = "현재 패스워드", example = "CurrentPass123!")
        String currentPassword,

        @ValidPassword
        @Schema(description = "새 패스워드", example = "NewPass123!")
        String newPassword
) {
}