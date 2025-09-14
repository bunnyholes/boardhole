package bunny.boardhole.board.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import bunny.boardhole.board.domain.validation.required.ValidBoardContent;
import bunny.boardhole.board.domain.validation.required.ValidBoardTitle;

public record CreateBoardCommand(
        @NotNull(message = "{validation.board.authorId.required}") UUID authorId,

        @ValidBoardTitle String title,

        @ValidBoardContent String content) {
}
