package bunny.boardhole.board.presentation.dto;

import bunny.boardhole.board.domain.validation.BoardValidationConstants;
import bunny.boardhole.board.domain.validation.required.ValidBoardContent;
import bunny.boardhole.board.domain.validation.required.ValidBoardTitle;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BoardCreateRequest", description = "게시글 작성 요청")
public record BoardCreateRequest(
        @ValidBoardTitle @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!", maxLength = BoardValidationConstants.BOARD_TITLE_MAX_LENGTH, requiredMode = Schema.RequiredMode.REQUIRED) String title,

        @ValidBoardContent @Schema(description = "게시글 내용", example = "이것은 새로운 게시글의 내용입니다.", maxLength = BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH, requiredMode = Schema.RequiredMode.REQUIRED) String content) {
}
