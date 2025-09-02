package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "RequestEmailVerificationCommand", description = "이메일 검증 요청 명령 - CQRS 패턴의 Command 객체")
public record RequestEmailVerificationCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @NotBlank(message = "{user.validation.password.current.required}")
        @Schema(description = "현재 패스워드 (본인 확인용)", example = "CurrentPass123!")
        String currentPassword,

        @ValidEmail
        @Schema(description = "변경할 새 이메일 주소", example = "newemail@example.com")
        String newEmail
) {
}