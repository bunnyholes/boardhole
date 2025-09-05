package bunny.boardhole.board.application.mapper;

import bunny.boardhole.board.application.command.IncrementViewCountCommand;
import bunny.boardhole.shared.mapstruct.MapstructConfig;
import org.mapstruct.Mapper;

/**
 * 게시글 명령 객체 매퍼
 */
@Mapper(config = MapstructConfig.class)
public interface BoardCommandMapper {

    /**
     * boardId로 조회수 증가 명령 생성
     */
    IncrementViewCountCommand toIncrementViewCountCommand(Long boardId);
}