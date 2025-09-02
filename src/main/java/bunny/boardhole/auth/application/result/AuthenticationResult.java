package bunny.boardhole.auth.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인증 정보 조회 결과 DTO
 * 현재 인증 상태와 사용자 정보를 포함하는 결과 객체입니다.
 */
@Schema(name = "AuthenticationResult", description = "인증 정보 조회 결과")
public record AuthenticationResult(
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

        @Schema(description = "인증 상태", example = "true")
        boolean authenticated,

        @Schema(description = "세션 ID", example = "JSESSIONID=ABC123")
        String sessionId
) {
}