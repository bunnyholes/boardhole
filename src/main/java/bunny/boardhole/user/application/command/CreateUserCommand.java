package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidEmail;
import bunny.boardhole.user.domain.validation.required.ValidName;
import bunny.boardhole.user.domain.validation.required.ValidPassword;
import bunny.boardhole.user.domain.validation.required.ValidUsername;

public record CreateUserCommand(@ValidUsername String username,

                                @ValidPassword String password,

                                @ValidName String name,

                                @ValidEmail String email) {
}
