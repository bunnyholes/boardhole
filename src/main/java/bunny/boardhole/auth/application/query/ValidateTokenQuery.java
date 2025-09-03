package bunny.boardhole.auth.application.query;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 검증 쿼리
 * CQRS 패턴의 Query 객체로 토큰 유효성 검증을 나타냅니다.
 */
public record ValidateTokenQuery(
        @NotBlank(message = "{auth.validation.token.required}")
        String token
) {
    public static ValidateTokenQuery of(String token) {
        return new ValidateTokenQuery(token);
    }
}