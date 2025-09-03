package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.validation.required.*;
import jakarta.validation.constraints.*;

public record CreateBoardCommand(
        @NotNull(message = "{board.validation.authorId.required}")
        @Positive(message = "{board.validation.authorId.positive}")
        Long authorId,

        @ValidBoardTitle
        String title,

        @ValidBoardContent
        String content
) {
}

