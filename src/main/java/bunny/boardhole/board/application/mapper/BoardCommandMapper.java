package bunny.boardhole.board.application.mapper;

import org.mapstruct.Mapper;

import bunny.boardhole.board.application.command.IncrementViewCountCommand;
import bunny.boardhole.shared.mapstruct.MapstructConfig;

/**
 * 게시글 명령 객체 매퍼
 */
@Mapper(config = MapstructConfig.class)
@SuppressWarnings("NullableProblems")
public interface BoardCommandMapper {

    /**
     * boardId로 조회수 증가 명령 생성
     */
    IncrementViewCountCommand toIncrementViewCountCommand(Long boardId);
}