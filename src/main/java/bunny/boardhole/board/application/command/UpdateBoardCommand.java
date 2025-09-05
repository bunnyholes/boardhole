package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.validation.optional.*;

import jakarta.validation.constraints.*;

public record UpdateBoardCommand(
        @NotNull(message = "{validation.board.boardId.required}") @Positive(message = "{validation.board.boardId.positive}") Long boardId,

        @NotNull(message = "{validation.board.authorId.required}") @Positive(message = "{validation.board.authorId.positive}") Long authorId,

        @OptionalBoardTitle String title,

        @OptionalBoardContent String content
) {
}
