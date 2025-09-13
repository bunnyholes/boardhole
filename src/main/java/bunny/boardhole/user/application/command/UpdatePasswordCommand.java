package bunny.boardhole.user.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import bunny.boardhole.user.domain.validation.required.ValidPassword;

public record UpdatePasswordCommand(
        @NotNull(message = "{validation.user.userId.required}") @Positive(message = "{validation.user.userId.positive}") Long userId,

        @NotBlank(message = "{validation.user.password.current.required}") String currentPassword,

        @ValidPassword String newPassword) {
}
