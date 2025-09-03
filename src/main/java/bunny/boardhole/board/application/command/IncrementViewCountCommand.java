package bunny.boardhole.board.application.command;

import jakarta.validation.constraints.*;

public record IncrementViewCountCommand(
        @NotNull(message = "{board.validation.boardId.required}")
        @Positive(message = "{board.validation.boardId.positive}")
        Long boardId
) {
}

