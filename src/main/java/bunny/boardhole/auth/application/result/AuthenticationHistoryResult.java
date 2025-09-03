package bunny.boardhole.auth.application.result;


import java.time.LocalDateTime;

/**
 * 인증 이력 조회 결과 DTO
 * 사용자의 로그인/로그아웃 이력 정보를 나타냅니다.
 */
public record AuthenticationHistoryResult(
        Long historyId,

        Long userId,

        String username,

        String eventType,

        LocalDateTime timestamp,

        String ipAddress,

        String userAgent
) {
}