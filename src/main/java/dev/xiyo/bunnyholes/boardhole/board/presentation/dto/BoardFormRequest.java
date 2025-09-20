package dev.xiyo.bunnyholes.boardhole.board.presentation.dto;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.required.ValidBoardContent;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.required.ValidBoardTitle;

public record BoardFormRequest(
        @ValidBoardTitle
        String title,

        @ValidBoardContent
        String content
) {
    public static BoardFormRequest empty() {
        return new BoardFormRequest("", "");
    }
}