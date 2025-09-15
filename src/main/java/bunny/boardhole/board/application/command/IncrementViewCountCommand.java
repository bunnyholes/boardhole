package bunny.boardhole.board.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record IncrementViewCountCommand(
        @NotNull(message = "{validation.board.boardId.required}") UUID boardId) {
}
