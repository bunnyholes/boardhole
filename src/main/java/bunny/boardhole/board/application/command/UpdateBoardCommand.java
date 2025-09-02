package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "UpdateBoardCommand", description = "게시글 수정 명령 - CQRS 패턴의 Command 객체")
public record UpdateBoardCommand(
        @NotNull(message = "{board.validation.boardId.required}")
        @Positive(message = "{board.validation.boardId.positive}")
        @Schema(description = "수정할 게시글 ID", example = "1")
        Long boardId,

        @NotNull(message = "{board.validation.authorId.required}")
        @Positive(message = "{board.validation.authorId.positive}")
        @Schema(description = "수정 요청자 ID (권한 검증용)", example = "1")
        Long authorId,

        @OptionalBoardTitle
        @Schema(description = "수정할 게시글 제목 (선택적)", example = "수정된 게시글 제목", nullable = true)
        String title,

        @OptionalBoardContent
        @Schema(description = "수정할 게시글 내용 (선택적)", example = "수정된 게시글 내용입니다.", nullable = true)
        String content
) {
}

