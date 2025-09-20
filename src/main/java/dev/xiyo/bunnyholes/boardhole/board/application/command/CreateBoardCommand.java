package dev.xiyo.bunnyholes.boardhole.board.application.command;

import jakarta.validation.constraints.NotBlank;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.required.ValidBoardContent;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.required.ValidBoardTitle;

public record CreateBoardCommand(
        @NotBlank(message = "{validation.board.authorUsername.required}") String authorUsername,

        @ValidBoardTitle String title,

        @ValidBoardContent String content) {
}
