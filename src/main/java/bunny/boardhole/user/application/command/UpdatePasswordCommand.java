package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 패스워드 변경 명령 DTO
 * CQRS 패턴의 Command 객체로 사용자 비밀번호 변경 요청을 표현합니다.
 * 보안을 위해 현재 비밀번호 확인이 필요합니다.
 */
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