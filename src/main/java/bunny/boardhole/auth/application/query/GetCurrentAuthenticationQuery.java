package bunny.boardhole.auth.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 현재 인증 정보 조회 쿼리
 * CQRS 패턴의 Query 객체로 현재 인증된 사용자 정보 조회를 나타냅니다.
 */
@Schema(name = "GetCurrentAuthenticationQuery", description = "현재 인증 정보 조회 쿼리 - CQRS 패턴의 Query 객체")
public record GetCurrentAuthenticationQuery(
        @Schema(description = "조회할 사용자 ID", example = "1")
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId
) {
    public static GetCurrentAuthenticationQuery of(Long userId) {
        return new GetCurrentAuthenticationQuery(userId);
    }
}