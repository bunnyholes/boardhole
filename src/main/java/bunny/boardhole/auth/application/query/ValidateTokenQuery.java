package bunny.boardhole.auth.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 토큰 검증 쿼리
 * CQRS 패턴의 Query 객체로 토큰 유효성 검증을 나타냅니다.
 */
@Schema(name = "ValidateTokenQuery", description = "토큰 검증 쿼리 - CQRS 패턴의 Query 객체")
public record ValidateTokenQuery(
        @Schema(description = "검증할 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "{auth.validation.token.required}")
        String token
) {
    public static ValidateTokenQuery of(String token) {
        return new ValidateTokenQuery(token);
    }
}