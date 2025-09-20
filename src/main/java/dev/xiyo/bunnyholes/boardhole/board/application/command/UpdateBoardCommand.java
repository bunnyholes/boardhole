package dev.xiyo.bunnyholes.boardhole.board.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.optional.OptionalBoardContent;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.optional.OptionalBoardTitle;

public record UpdateBoardCommand(
        @NotNull(message = "{validation.board.boardId.required}") UUID boardId,

        @OptionalBoardTitle String title,

        @OptionalBoardContent String content) {
}
