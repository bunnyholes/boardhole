package bunny.boardhole.board.application.mapper;

import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.shared.mapstruct.MapstructConfig;
import org.mapstruct.*;

@Mapper(config = MapstructConfig.class)
public interface BoardMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.username")
    BoardResult toResult(Board board);
}
