package bunny.boardhole.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청")
public class UserUpdateRequest {
    @Size(min = 1, max = 50)
    @Schema(description = "수정할 이름 (선택사항, 1-50자)", example = "홍길동", minLength = 1, maxLength = 50)
    private String name;

    @Email
    @Schema(description = "수정할 이메일 주소 (선택사항)", example = "john@example.com")
    private String email;

    @Size(min = 4, max = 100)
    @Schema(description = "수정할 비밀번호 (선택사항, 4-100자)", example = "newpassword123!", minLength = 4, maxLength = 100)
    private String password;
}

