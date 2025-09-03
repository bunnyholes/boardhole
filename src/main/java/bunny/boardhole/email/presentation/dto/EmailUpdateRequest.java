package bunny.boardhole.email.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "EmailUpdateRequest", description = "이메일 변경 요청")
public record EmailUpdateRequest(
        @NotBlank(message = "{user.validation.verification.code.required}")
        @Schema(description = "이메일 검증 코드", example = "ABC123", requiredMode = Schema.RequiredMode.REQUIRED)
        String verificationCode
) {
}