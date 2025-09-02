package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.ValidEmail;
import bunny.boardhole.user.domain.validation.ValidName;
import bunny.boardhole.user.domain.validation.ValidPassword;
import bunny.boardhole.user.domain.validation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateUserCommand", description = "사용자 생성 명령 - CQRS 패턴의 Command 객체")
public record CreateUserCommand(
        @ValidUsername
        @Schema(description = "사용자명 (3-20자)", example = "johndoe")
        String username,
        
        @ValidPassword
        @Schema(description = "비밀번호 (8-100자)", example = "Password123!")
        String password,
        
        @ValidName
        @Schema(description = "사용자 실명 (1-50자)", example = "홍길동")
        String name,
        
        @ValidEmail
        @Schema(description = "이메일 주소", example = "john@example.com")
        String email
) {
}

