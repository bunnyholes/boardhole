package bunny.boardhole.auth.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "LoginRequest", description = "로그인 요청")
public class LoginRequest {
    @NotBlank(message = "{validation.auth.username.required}")
    @Schema(description = "사용자명", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @NotBlank(message = "{validation.auth.password.required}")
    @Schema(description = "비밀번호", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
