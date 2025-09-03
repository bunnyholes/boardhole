package bunny.boardhole.auth.application.query;

import jakarta.validation.constraints.*;
import org.springframework.data.domain.Pageable;

/**
 * 인증 이력 조회 쿼리
 * CQRS 패턴의 Query 객체로 사용자의 인증 이력 조회를 나타냅니다.
 */
public record GetAuthenticationHistoryQuery(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId,

        Pageable pageable
) {
    public static GetAuthenticationHistoryQuery of(Long userId, Pageable pageable) {
        return new GetAuthenticationHistoryQuery(userId, pageable);
    }
}