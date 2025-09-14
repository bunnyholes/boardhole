package bunny.boardhole.user.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import bunny.boardhole.user.domain.validation.required.ValidPassword;

public record UpdatePasswordCommand(
        @NotNull(message = "{validation.user.userId.required}") UUID userId,

        @NotBlank(message = "{validation.user.password.current.required}") String currentPassword,

        @ValidPassword String newPassword) {
}
