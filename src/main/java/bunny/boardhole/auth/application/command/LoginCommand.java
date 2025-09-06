package bunny.boardhole.auth.application.command;

import bunny.boardhole.user.domain.validation.required.ValidPassword;
import bunny.boardhole.user.domain.validation.required.ValidUsername;

/**
 * 로그인 명령
 * CQRS 패턴의 Command 객체로 로그인 요청을 나타냅니다.
 */
public record LoginCommand(@ValidUsername String username,

                           @ValidPassword String password) {
}
