package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 생성 명령 DTO
 * CQRS 패턴의 Command 객체로 새로운 사용자 생성 요청을 표현합니다.
 * 사용자 계정 생성에 필요한 모든 필수 정보를 포함합니다.
 */
@Schema(name = "CreateUserCommand", description = "사용자 생성 명령 - CQRS 패턴의 Command 객체")
public record CreateUserCommand(
        @ValidUsername
        @Schema(description = "사용자명 (3-20자, 영문/숫자/언더스코어만 허용)", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
        String username,

        @ValidPassword
        @Schema(description = "비밀번호 (8-100자, 대소문자/숫자/특수문자 모두 포함 필수)", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @ValidName
        @Schema(description = "사용자 실명 (1-50자)", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @ValidEmail
        @Schema(description = "이메일 주소 (유효한 이메일 형식)", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {
}

