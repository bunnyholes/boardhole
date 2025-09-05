package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.validation.required.*;
import jakarta.validation.constraints.*;

public record CreateBoardCommand(
        @NotNull(message = "{validation.board.authorId.required}") @Positive(message = "{validation.board.authorId.positive}") Long authorId,

        @ValidBoardTitle String title,

        @ValidBoardContent String content
) {
}
