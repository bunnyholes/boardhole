package bunny.boardhole.board.application.query;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 게시글 조회 쿼리 DTO
 * CQRS 패턴의 Query 객체로 게시글 조회 요청을 표현합니다.
 */
@Schema(name = "GetBoardQuery", description = "게시글 조회 쿼리 - CQRS 패턴의 Query 객체")
public record GetBoardQuery(
        @Schema(description = "조회할 게시글 ID", example = "1")
        Long id
) {
    public static GetBoardQuery of(Long id) {
        return new GetBoardQuery(id);
    }
}

