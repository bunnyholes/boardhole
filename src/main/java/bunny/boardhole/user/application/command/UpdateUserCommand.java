package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.optional.OptionalName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "UpdateUserCommand", description = "사용자 정보 수정 명령 - CQRS 패턴의 Command 객체 (이름만 변경 가능)")
public record UpdateUserCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        @Schema(description = "수정할 사용자 ID", example = "1")
        Long userId,

        @OptionalName
        @Schema(description = "수정할 사용자 실명 (선택적)", example = "홍길동", nullable = true)
        String name
) {
}

