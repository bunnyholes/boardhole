package dev.xiyo.bunnyholes.boardhole.user.application.command;

import jakarta.validation.constraints.NotBlank;

import dev.xiyo.bunnyholes.boardhole.shared.validation.FieldsMatch;
import dev.xiyo.bunnyholes.boardhole.shared.validation.FieldsNotEqual;
import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidPassword;

@FieldsMatch(fields = {"newPassword", "confirmPassword"})
@FieldsNotEqual(fields = {"currentPassword", "newPassword"})
public record UpdatePasswordCommand(
        @NotBlank(message = "{validation.user.username.required}") String username,

        @NotBlank(message = "{validation.user.password.current.required}") String currentPassword,

        @ValidPassword String newPassword,

        @NotBlank(message = "{validation.user.password.confirm.required}") String confirmPassword) {
}
