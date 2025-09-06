package bunny.boardhole.board.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import bunny.boardhole.board.domain.validation.required.ValidBoardContent;
import bunny.boardhole.board.domain.validation.required.ValidBoardTitle;

public record CreateBoardCommand(
        @NotNull(message = "{validation.board.authorId.required}") @Positive(message = "{validation.board.authorId.positive}") Long authorId,

        @ValidBoardTitle String title,

        @ValidBoardContent String content) {
}
