package bunny.boardhole.board.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import bunny.boardhole.board.application.command.IncrementViewCountCommand;

/**
 * 게시글 명령 객체 매퍼
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface BoardCommandMapper {

    /**
     * boardId로 조회수 증가 명령 생성
     */
    IncrementViewCountCommand toIncrementViewCountCommand(Long boardId);
}