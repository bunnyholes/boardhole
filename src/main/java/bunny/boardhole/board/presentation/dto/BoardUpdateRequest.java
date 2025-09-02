package bunny.boardhole.board.presentation.dto;

import bunny.boardhole.board.domain.validation.optional.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 게시글 수정 요청 DTO
 * 게시글의 제목이나 내용을 부분적으로 수정하기 위한 요청 데이터를 담는 데이터 전송 객체입니다.
 */
@Schema(name = "BoardUpdateRequest", description = "게시글 수정 요청")
public record BoardUpdateRequest(
        @OptionalBoardTitle
        @Schema(description = "수정할 게시글 제목 (선택사항)", example = "수정된 게시글 제목", maxLength = 200)
        String title,

        @OptionalBoardContent
        @Schema(description = "수정할 게시글 내용 (선택사항)", example = "수정된 게시글 내용입니다.", maxLength = 10_000)
        String content
) {
}
