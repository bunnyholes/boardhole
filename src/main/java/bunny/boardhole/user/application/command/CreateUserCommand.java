package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.*;

public record CreateUserCommand(
        @ValidUsername
        String username,

        @ValidPassword
        String password,

        @ValidName
        String name,

        @ValidEmail
        String email
) {
}

