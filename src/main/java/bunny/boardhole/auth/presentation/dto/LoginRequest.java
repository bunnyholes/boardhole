package bunny.boardhole.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

import bunny.boardhole.user.domain.validation.required.ValidUsername;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "로그인 요청")
public record LoginRequest(
        @ValidUsername @Schema(description = "사용자명", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED) String username,

        @NotBlank(message = "{validation.user.password.required}") @Schema(description = "비밀번호", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED) String password) {
}
