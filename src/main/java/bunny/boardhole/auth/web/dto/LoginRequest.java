package bunny.boardhole.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(name = "LoginRequest", description = "로그인 요청")
public class LoginRequest {
    @NotBlank
    @Schema(description = "사용자명", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    @NotBlank
    @Schema(description = "비밀번호", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

