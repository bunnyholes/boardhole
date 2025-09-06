package bunny.boardhole.board.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import bunny.boardhole.board.domain.validation.optional.OptionalBoardContent;
import bunny.boardhole.board.domain.validation.optional.OptionalBoardTitle;

public record UpdateBoardCommand(
        @NotNull(message = "{validation.board.boardId.required}") @Positive(message = "{validation.board.boardId.positive}") Long boardId,

        @NotNull(message = "{validation.board.authorId.required}") @Positive(message = "{validation.board.authorId.positive}") Long authorId,

        @OptionalBoardTitle String title,

        @OptionalBoardContent String content) {
}
