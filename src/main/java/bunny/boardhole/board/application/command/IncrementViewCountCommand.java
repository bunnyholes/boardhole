package bunny.boardhole.board.application.command;

import jakarta.validation.constraints.*;

public record IncrementViewCountCommand(
        @NotNull(message = "{validation.board.boardId.required}") @Positive(message = "{validation.board.boardId.positive}") Long boardId
) {
}
