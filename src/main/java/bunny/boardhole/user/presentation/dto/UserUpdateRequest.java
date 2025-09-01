package bunny.boardhole.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청")
public record UserUpdateRequest(
    @Size(min = 1, max = 50, message = "{validation.user.name.size}")
    @Schema(description = "수정할 이름 (선택사항, 1-50자)", example = "홍길동", minLength = 1, maxLength = 50)
    String name,

    @Email(message = "{validation.user.email.format}")
    @Schema(description = "수정할 이메일 주소 (선택사항)", example = "user@example.com")
    String email,

    @Size(min = 4, max = 100, message = "{validation.user.password.size}")
    @Schema(description = "수정할 비밀번호 (선택사항, 4-100자)", example = "newpassword123", minLength = 4, maxLength = 100)
    String password
) {}
