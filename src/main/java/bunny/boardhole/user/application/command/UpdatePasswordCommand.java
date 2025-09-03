package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidPassword;
import jakarta.validation.constraints.*;

public record UpdatePasswordCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId,

        @NotBlank(message = "{user.validation.password.current.required}")
        String currentPassword,

        @ValidPassword
        String newPassword
) {
}