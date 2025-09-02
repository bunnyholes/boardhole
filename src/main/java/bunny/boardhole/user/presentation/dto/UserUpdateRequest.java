package bunny.boardhole.user.presentation.dto;

import bunny.boardhole.user.domain.validation.optional.OptionalName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 정보 수정 요청 DTO
 * 기존 사용자의 정보를 수정하기 위한 요청 데이터를 담고 있는 객체입니다.
 * 현재는 이름만 변경 가능합니다.
 */
@Schema(name = "UserUpdateRequest", description = "사용자 정보 수정 요청 (이름만 변경 가능)")
public record UserUpdateRequest(
        @OptionalName
        @Schema(description = "수정할 이름 (선택사항, 1-50자)", example = "홍길동", minLength = 1, maxLength = 50)
        String name
) {
}
