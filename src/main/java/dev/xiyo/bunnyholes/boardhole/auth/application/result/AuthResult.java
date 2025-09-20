package dev.xiyo.bunnyholes.boardhole.auth.application.result;

import java.util.UUID;

/**
 * 인증 결과 DTO
 * 인증/인가 작업의 결과를 Application Layer에서 Web Layer로 전달하기 위한 객체입니다.
 */
public record AuthResult(UUID userId,

                         String username,

                         String email,

                         String name,

                         String role,

                         boolean authenticated) {
}
