package bunny.boardhole.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 변경 요청 DTO
 * 이메일 변경 검증 후 실제 이메일 주소 변경을 완료하기 위한 요청 객체입니다.
 * 이메일 검증 코드가 필요합니다.
 */
@Schema(name = "EmailUpdateRequest", description = "이메일 변경 요청")
public record EmailUpdateRequest(
        @NotBlank(message = "{user.validation.verification.code.required}")
        @Schema(description = "이메일 검증 코드", example = "ABC123", requiredMode = Schema.RequiredMode.REQUIRED)
        String verificationCode
) {
}