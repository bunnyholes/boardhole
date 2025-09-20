package dev.xiyo.bunnyholes.boardhole.user.application.command;

import jakarta.validation.constraints.NotBlank;

import org.springframework.lang.Nullable;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.optional.OptionalName;

public record UpdateUserCommand(
        @NotBlank(message = "{validation.user.username.required}") String username,

        @OptionalName @Nullable String name) {
}
