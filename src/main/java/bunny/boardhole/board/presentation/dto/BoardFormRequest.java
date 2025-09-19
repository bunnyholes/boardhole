package bunny.boardhole.board.presentation.dto;

import bunny.boardhole.board.domain.validation.required.ValidBoardTitle;
import bunny.boardhole.board.domain.validation.required.ValidBoardContent;

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