package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidEmail;
import jakarta.validation.constraints.*;

public record RequestEmailVerificationCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId,

        @NotBlank(message = "{user.validation.password.current.required}")
        String currentPassword,

        @ValidEmail
        String newEmail
) {
}