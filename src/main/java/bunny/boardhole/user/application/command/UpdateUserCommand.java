package bunny.boardhole.user.application.command;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateUserCommand", description = "사용자 정보 수정 명령 - CQRS 패턴의 Command 객체")
public record UpdateUserCommand(
        @Schema(description = "수정할 사용자 ID", example = "1")
        Long userId,
        @Schema(description = "수정할 사용자 실명", example = "홍길동")
        String name,
        @Schema(description = "수정할 이메일 주소", example = "john@example.com")
        String email,
        @Schema(description = "수정할 비밀번호 (선택사항)", example = "newpassword123!")
        String password
) {
}

