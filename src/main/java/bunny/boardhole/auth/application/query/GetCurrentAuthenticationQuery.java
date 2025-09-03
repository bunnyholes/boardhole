package bunny.boardhole.auth.application.query;

import jakarta.validation.constraints.*;

/**
 * 현재 인증 정보 조회 쿼리
 * CQRS 패턴의 Query 객체로 현재 인증된 사용자 정보 조회를 나타냅니다.
 */
public record GetCurrentAuthenticationQuery(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId
) {
    public static GetCurrentAuthenticationQuery of(Long userId) {
        return new GetCurrentAuthenticationQuery(userId);
    }
}