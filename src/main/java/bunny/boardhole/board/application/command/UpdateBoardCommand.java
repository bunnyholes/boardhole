package bunny.boardhole.board.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import bunny.boardhole.board.domain.validation.optional.OptionalBoardContent;
import bunny.boardhole.board.domain.validation.optional.OptionalBoardTitle;

public record UpdateBoardCommand(
        @NotNull(message = "{validation.board.boardId.required}") UUID boardId,

        @OptionalBoardTitle String title,

        @OptionalBoardContent String content) {
}
