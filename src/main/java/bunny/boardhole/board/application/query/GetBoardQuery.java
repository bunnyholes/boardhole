package bunny.boardhole.board.application.query;


public record GetBoardQuery(
        Long id
) {
    public static GetBoardQuery of(Long id) {
        return new GetBoardQuery(id);
    }
}

