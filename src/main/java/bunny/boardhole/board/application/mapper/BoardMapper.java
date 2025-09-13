package bunny.boardhole.board.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import bunny.boardhole.board.application.event.ViewedEvent;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface BoardMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.username")
    BoardResult toResult(Board board);

    /**
     * 게시글 조회 이벤트 생성
     *
     * @param boardId 게시글 ID
     * @return 조회 이벤트
     */
    ViewedEvent toViewedEvent(Long boardId);
}
