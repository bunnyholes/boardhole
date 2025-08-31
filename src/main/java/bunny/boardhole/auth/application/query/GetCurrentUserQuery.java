package bunny.boardhole.auth.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 현재 사용자 조회 쿼리
 * CQRS 패턴의 Query 객체로 현재 인증된 사용자 정보 조회 요청을 나타냅니다.
 */
@Schema(name = "GetCurrentUserQuery", description = "현재 사용자 조회 쿼리 - CQRS 패턴의 Query 객체")
public record GetCurrentUserQuery(
        @Schema(description = "조회할 사용자 ID", example = "1")
        @NotNull
        Long userId
) {
}