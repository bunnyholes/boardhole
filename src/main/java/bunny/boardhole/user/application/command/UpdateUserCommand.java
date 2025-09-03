package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.optional.OptionalName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

public record UpdateUserCommand(
        @NotNull(message = "{user.validation.userId.required}") @Positive(message = "{user.validation.userId.positive}") Long userId,

        @OptionalName @Nullable String name
) {
}

