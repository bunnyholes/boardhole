package bunny.boardhole.user.application.command;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateUserCommand", description = "사용자 생성 명령 - CQRS 패턴의 Command 객체")
public record CreateUserCommand(
        @Schema(description = "사용자명 (3-20자)", example = "johndoe")
        String username,
        @Schema(description = "비밀번호 (4-100자)", example = "password123!")
        String password,
        @Schema(description = "사용자 실명 (1-50자)", example = "홍길동")
        String name,
        @Schema(description = "이메일 주소", example = "john@example.com")
        String email
) {
}

