package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.required.ValidName;

public record UpdateUserRequest(@ValidName String name) {
}
