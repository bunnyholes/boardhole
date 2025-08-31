package bunny.boardhole.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(name = "UserCreateRequest", description = "사용자 생성 요청")
public class UserCreateRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Schema(description = "사용자명 (3-20자)", example = "johndoe", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    @Schema(description = "비밀번호 (4-100자)", example = "password123!", minLength = 4, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "이름 (1-50자)", example = "홍길동", minLength = 1, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Email
    @Schema(description = "이메일 주소", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}

