package bunny.boardhole.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import bunny.boardhole.user.domain.validation.*;

@Schema(name = "UserCreateRequest", description = "사용자 생성 요청")
public record UserCreateRequest(
        @ValidUsername
        @Schema(description = "사용자명 (3-20자, 영문/숫자)", example = "user01", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @ValidPassword
        @Schema(description = "비밀번호 (8-100자)", example = "User1234!", minLength = 8, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @ValidName
        @Schema(description = "이름 (1-50자)", example = "홍길동", minLength = 1, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @ValidEmail
        @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {
}
