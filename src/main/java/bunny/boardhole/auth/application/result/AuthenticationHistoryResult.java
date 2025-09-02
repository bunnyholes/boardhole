package bunny.boardhole.auth.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 인증 이력 조회 결과 DTO
 * 사용자의 로그인/로그아웃 이력 정보를 나타냅니다.
 */
@Schema(name = "AuthenticationHistoryResult", description = "인증 이력 조회 결과")
public record AuthenticationHistoryResult(
        @Schema(description = "이력 ID", example = "1")
        Long historyId,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자명", example = "admin")
        String username,

        @Schema(description = "인증 이벤트 유형", example = "LOGIN")
        String eventType,

        @Schema(description = "인증 시간", example = "2025-01-15T10:30:00")
        LocalDateTime timestamp,

        @Schema(description = "IP 주소", example = "192.168.1.1")
        String ipAddress,

        @Schema(description = "User Agent", example = "Mozilla/5.0...")
        String userAgent
) {
}