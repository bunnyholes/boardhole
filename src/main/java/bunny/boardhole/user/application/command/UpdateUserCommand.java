package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.OptionalEmail;
import bunny.boardhole.user.domain.validation.OptionalName;
import bunny.boardhole.user.domain.validation.OptionalPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "UpdateUserCommand", description = "사용자 정보 수정 명령 - CQRS 패턴의 Command 객체")
public record UpdateUserCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        @Schema(description = "수정할 사용자 ID", example = "1")
        Long userId,
        
        @OptionalName
        @Schema(description = "수정할 사용자 실명 (선택적)", example = "홍길동", nullable = true)
        String name,
        
        @OptionalEmail
        @Schema(description = "수정할 이메일 주소 (선택적)", example = "john@example.com", nullable = true)
        String email,
        
        @OptionalPassword
        @Schema(description = "수정할 비밀번호 (선택적)", example = "NewPassword123!", nullable = true)
        String password
) {
}

