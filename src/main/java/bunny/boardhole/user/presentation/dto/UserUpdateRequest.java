package bunny.boardhole.user.presentation.dto;

import bunny.boardhole.user.domain.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청")
public record UserUpdateRequest(
        @OptionalName
        @Schema(description = "수정할 이름 (선택사항, 1-50자)", example = "홍길동", minLength = 1, maxLength = 50)
        String name,

        @OptionalEmail
        @Schema(description = "수정할 이메일 주소 (선택사항)", example = "user@example.com")
        String email,

        @OptionalPassword
        @Schema(description = "수정할 비밀번호 (선택사항, 8-100자)", example = "NewPass123!", minLength = 8, maxLength = 100)
        String password
) {
}
