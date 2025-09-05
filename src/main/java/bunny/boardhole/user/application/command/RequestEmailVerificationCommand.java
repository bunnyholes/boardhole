package bunny.boardhole.user.application.command;

import bunny.boardhole.user.domain.validation.required.ValidEmail;
import jakarta.validation.constraints.*;

public record RequestEmailVerificationCommand(
        @NotNull(message = "{validation.user.userId.required}") @Positive(message = "{validation.user.userId.positive}") Long userId,

        @NotBlank(message = "{validation.user.password.current.required}") String currentPassword,

        @ValidEmail String newEmail
) {
}
