package dev.xiyo.bunnyholes.boardhole.board.presentation.dto;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.optional.OptionalBoardContent;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.optional.OptionalBoardTitle;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BoardUpdateRequest", description = "게시글 수정 요청")
public record BoardUpdateRequest(
        @OptionalBoardTitle @Schema(description = "수정할 게시글 제목 (선택사항)", example = "수정된 게시글 제목", maxLength = BoardValidationConstants.BOARD_TITLE_MAX_LENGTH) String title,

        @OptionalBoardContent @Schema(description = "수정할 게시글 내용 (선택사항)", example = "수정된 게시글 내용입니다.", maxLength = BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH) String content) {
}
