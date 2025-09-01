package bunny.boardhole.auth.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인증 결과 DTO
 * 인증/인가 작업의 결과를 Application Layer에서 Web Layer로 전달하기 위한 객체입니다.
 */
@Schema(name = "AuthResult", description = "인증 작업 결과")
public record AuthResult(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자명", example = "admin")
        String username,

        @Schema(description = "이메일", example = "admin@boardhole.com")
        String email,

        @Schema(description = "이름", example = "관리자")
        String name,

        @Schema(description = "역할", example = "ADMIN")
        String role,

        @Schema(description = "인증 성공 여부", example = "true")
        boolean authenticated
) {
}
