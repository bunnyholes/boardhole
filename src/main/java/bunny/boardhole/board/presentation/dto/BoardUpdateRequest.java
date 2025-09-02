package bunny.boardhole.board.presentation.dto;

import bunny.boardhole.board.domain.validation.optional.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BoardUpdateRequest", description = "게시글 수정 요청")
public record BoardUpdateRequest(
        @OptionalBoardTitle
        @Schema(description = "수정할 게시글 제목 (선택사항)", example = "수정된 게시글 제목", maxLength = 200)
        String title,

        @OptionalBoardContent
        @Schema(description = "수정할 게시글 내용 (선택사항)", example = "수정된 게시글 내용입니다.", maxLength = 10000)
        String content
) {
}
