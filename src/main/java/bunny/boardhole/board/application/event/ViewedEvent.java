package bunny.boardhole.board.application.event;


public record ViewedEvent(
        Long boardId,
        Long viewerId
) {
    public static ViewedEvent of(Long boardId, Long viewerId) {
        return new ViewedEvent(boardId, viewerId);
    }
}

