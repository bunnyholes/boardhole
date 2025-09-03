package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.validation.optional.*;
import jakarta.validation.constraints.*;

public record UpdateBoardCommand(
        @NotNull(message = "{board.validation.boardId.required}")
        @Positive(message = "{board.validation.boardId.positive}")
        Long boardId,

        @NotNull(message = "{board.validation.authorId.required}")
        @Positive(message = "{board.validation.authorId.positive}")
        Long authorId,

        @OptionalBoardTitle
        String title,

        @OptionalBoardContent
        String content
) {
}

