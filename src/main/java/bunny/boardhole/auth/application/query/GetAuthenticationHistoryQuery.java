package bunny.boardhole.auth.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Pageable;

/**
 * 인증 이력 조회 쿼리
 * CQRS 패턴의 Query 객체로 사용자의 인증 이력 조회를 나타냅니다.
 */
@Schema(name = "GetAuthenticationHistoryQuery", description = "인증 이력 조회 쿼리 - CQRS 패턴의 Query 객체")
public record GetAuthenticationHistoryQuery(
        @Schema(description = "조회할 사용자 ID", example = "1")
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId,

        @Schema(description = "페이지네이션 정보")
        Pageable pageable
) {
    public static GetAuthenticationHistoryQuery of(Long userId, Pageable pageable) {
        return new GetAuthenticationHistoryQuery(userId, pageable);
    }
}