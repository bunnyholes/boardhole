package bunny.boardhole.auth.application.result;


/**
 * 토큰 검증 결과 DTO
 * 토큰의 유효성 검증 결과와 관련 정보를 나타냅니다.
 */
public record TokenValidationResult(
        boolean valid,

        Long userId,

        String username,

        String errorMessage
) {
}