package bunny.boardhole.board.application.command;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record IncrementViewCountCommand(
        @NotNull(message = "{validation.board.boardId.required}") UUID boardId) {
}
