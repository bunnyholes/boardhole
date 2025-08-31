package bunny.boardhole.user.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserCreateRequest", description = "사용자 생성 요청")
public class UserCreateRequest {
    @NotBlank(message = "{validation.user.username.required}")
    @Size(min = 3, max = 20, message = "{validation.user.username.size}")
    @Schema(description = "사용자명 (3-20자, 영문/숫자)", example = "user01", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 4, max = 100, message = "{validation.user.password.size}")
    @Schema(description = "비밀번호 (4-100자)", example = "user1234", minLength = 4, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "{validation.user.name.required}")
    @Size(min = 1, max = 50, message = "{validation.user.name.size}")
    @Schema(description = "이름 (1-50자)", example = "홍길동", minLength = 1, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "{validation.user.email.required}")
    @Email(message = "{validation.user.email.format}")
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
