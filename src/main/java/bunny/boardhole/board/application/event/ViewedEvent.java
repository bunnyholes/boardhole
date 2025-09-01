package bunny.boardhole.board.application.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ViewedEvent", description = "게시글 조회 이벤트 - 비동기 조회수 증가를 위한 이벤트 객체")
public record ViewedEvent(
        @Schema(description = "조회된 게시글 ID", example = "1")
        Long boardId,
        @Schema(description = "조회자 ID (null 가능 - 익명 사용자)", example = "1")
        Long viewerId
) {
    public static ViewedEvent of(Long boardId, Long viewerId) {
        return new ViewedEvent(boardId, viewerId);
    }
}

