package bunny.boardhole.email.presentation.dto;

import bunny.boardhole.user.domain.validation.required.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "EmailVerificationRequest", description = "이메일 변경 검증 요청")
public record EmailVerificationRequest(
        @NotBlank(message = "{user.validation.password.current.required}")
        @Schema(description = "본인 확인용 현재 패스워드", example = "CurrentPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        String currentPassword,

        @ValidEmail
        @Schema(description = "변경할 새 이메일 주소", example = "newemail@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String newEmail
) {
}