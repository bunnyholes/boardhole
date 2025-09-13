package bunny.boardhole.user.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.jspecify.annotations.Nullable;

import bunny.boardhole.user.domain.validation.optional.OptionalName;

public record UpdateUserCommand(
        @NotNull(message = "{validation.user.userId.required}") @Positive(message = "{validation.user.userId.positive}") Long userId,

        @OptionalName @Nullable String name) {
}
