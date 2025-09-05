package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidPassword;

import jakarta.validation.constraints.*;

public record UpdatePasswordCommand(
        @NotNull(message = "{validation.user.userId.required}") @Positive(message = "{validation.user.userId.positive}") Long userId,

        @NotBlank(message = "{validation.user.password.current.required}") String currentPassword,

        @ValidPassword String newPassword
) {
}
