package bunny.boardhole.board.application.command;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateBoardCommand", description = "게시글 수정 명령 - CQRS 패턴의 Command 객체")
public record UpdateBoardCommand(
        @Schema(description = "수정할 게시글 ID", example = "1")
        Long boardId,
        @Schema(description = "수정 요청자 ID (권한 검증용)", example = "1")
        Long authorId,
        @Schema(description = "수정할 게시글 제목", example = "수정된 게시글 제목")
        String title,
        @Schema(description = "수정할 게시글 내용", example = "수정된 게시글 내용입니다.")
        String content
) {
}

