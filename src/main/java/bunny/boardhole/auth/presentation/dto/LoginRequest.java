package bunny.boardhole.auth.presentation.dto;

import bunny.boardhole.user.domain.validation.required.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 * 사용자 인증을 위한 로그인 요청 데이터를 담고 있는 객체입니다.
 */
@Schema(name = "LoginRequest", description = "로그인 요청")
public record LoginRequest(
        @ValidUsername
        @Schema(description = "사용자명", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @NotBlank(message = "{user.validation.password.required}")
        @Schema(description = "비밀번호", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
