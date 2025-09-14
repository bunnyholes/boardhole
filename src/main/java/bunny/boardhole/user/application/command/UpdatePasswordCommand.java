package bunny.boardhole.user.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import bunny.boardhole.shared.validation.PasswordConfirmed;
import bunny.boardhole.user.domain.validation.required.ValidPassword;

@PasswordConfirmed
public record UpdatePasswordCommand(
        @NotNull(message = "{validation.user.userId.required}") UUID userId,

        @NotBlank(message = "{validation.user.password.current.required}") String currentPassword,

        @ValidPassword String newPassword,

        @NotBlank(message = "{validation.user.password.confirm.required}") String confirmPassword) {
}
