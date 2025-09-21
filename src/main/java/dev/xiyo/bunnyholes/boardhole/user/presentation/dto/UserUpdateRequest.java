package dev.xiyo.bunnyholes.boardhole.user.presentation.dto;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.UserValidationConstants;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidName;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청 (이름만 변경 가능)")
public record UserUpdateRequest(
        @ValidName @Schema(description = "수정할 이름 (필수, 1-50자)", example = "홍길동", minLength = UserValidationConstants.USER_NAME_MIN_LENGTH, maxLength = UserValidationConstants.USER_NAME_MAX_LENGTH) String name) {
}
