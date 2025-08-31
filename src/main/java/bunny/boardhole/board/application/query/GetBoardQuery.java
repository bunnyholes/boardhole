package bunny.boardhole.board.application.query;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GetBoardQuery", description = "게시글 조회 쿼리 - CQRS 패턴의 Query 객체")
public record GetBoardQuery(
        @Schema(description = "조회할 게시글 ID", example = "1")
        Long id
) {
}

