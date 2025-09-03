package bunny.boardhole.user.application.command;

import jakarta.validation.constraints.*;

public record UpdateEmailCommand(
        @NotNull(message = "{user.validation.userId.required}")
        @Positive(message = "{user.validation.userId.positive}")
        Long userId,

        @NotBlank(message = "{user.validation.verification.code.required}")
        String verificationCode
) {
}