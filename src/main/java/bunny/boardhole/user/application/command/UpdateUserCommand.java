package bunny.boardhole.user.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import bunny.boardhole.user.domain.validation.optional.OptionalName;

public record UpdateUserCommand(
        @NotNull(message = "{validation.user.userId.required}") UUID userId,

        @OptionalName @Nullable String name) {
}
