package dev.xiyo.bunnyholes.boardhole.user.application.command;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidEmail;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidName;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidPassword;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidUsername;

public record CreateUserCommand(@ValidUsername String username,

                                @ValidPassword String password,

                                @ValidName String name,

                                @ValidEmail String email) {
}
