package bunny.boardhole.user.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateEmailCommand(
        @NotNull(message = "{validation.user.userId.required}") @Positive(message = "{validation.user.userId.positive}") Long userId,

        @NotBlank(message = "{validation.email.verification.code.required}") String verificationCode) {
}
