package bunny.boardhole.auth.application.result;


/**
 * 인증 정보 조회 결과 DTO
 * 현재 인증 상태와 사용자 정보를 포함하는 결과 객체입니다.
 */
public record AuthenticationResult(
        Long userId,

        String username,

        String email,

        String name,

        String role,

        boolean authenticated,

        String sessionId
) {
}