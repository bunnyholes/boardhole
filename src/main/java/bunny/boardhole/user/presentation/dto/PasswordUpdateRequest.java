package bunny.boardhole.user.presentation.dto;

import bunny.boardhole.user.domain.validation.required.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "PasswordUpdateRequest", description = "패스워드 변경 요청")
public record PasswordUpdateRequest(
        @NotBlank(message = "{validation.user.password.current.required}") @Schema(description = "현재 패스워드", example = "CurrentPass123!", requiredMode = Schema.RequiredMode.REQUIRED) String currentPassword,

        @ValidPassword @Schema(description = "새 패스워드 (8-100자, 대문자/소문자/숫자/특수문자 모두 포함)", example = "NewPass123!", requiredMode = Schema.RequiredMode.REQUIRED) String newPassword,

        @NotBlank(message = "{validation.user.password.confirm.required}") @Schema(description = "새 패스워드 확인", example = "NewPass123!", requiredMode = Schema.RequiredMode.REQUIRED) String confirmPassword
) {
}
