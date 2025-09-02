package bunny.boardhole.auth.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 검증 결과 DTO
 * 토큰의 유효성 검증 결과와 관련 정보를 나타냅니다.
 */
@Schema(name = "TokenValidationResult", description = "토큰 검증 결과")
public record TokenValidationResult(
        @Schema(description = "토큰 유효성", example = "true")
        boolean valid,

        @Schema(description = "사용자 ID (유효한 토큰인 경우)", example = "1")
        Long userId,

        @Schema(description = "사용자명 (유효한 토큰인 경우)", example = "admin")
        String username,

        @Schema(description = "검증 실패 사유 (무효한 토큰인 경우)", example = "Token expired")
        String errorMessage
) {
}