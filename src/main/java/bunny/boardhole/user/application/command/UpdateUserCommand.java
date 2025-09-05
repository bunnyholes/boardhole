package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.optional.OptionalName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

public record UpdateUserCommand(
        @NotNull(message = "{validation.user.userId.required}") @Positive(message = "{validation.user.userId.positive}") Long userId,

        @OptionalName @Nullable String name
) {
}
