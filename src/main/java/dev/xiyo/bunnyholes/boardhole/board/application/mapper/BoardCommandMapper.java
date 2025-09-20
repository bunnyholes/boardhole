package dev.xiyo.bunnyholes.boardhole.board.application.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import dev.xiyo.bunnyholes.boardhole.board.application.command.IncrementViewCountCommand;

/**
 * 게시글 명령 객체 매퍼
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface BoardCommandMapper {

    /**
     * boardId로 조회수 증가 명령 생성
     */
    IncrementViewCountCommand toIncrementViewCountCommand(UUID boardId);
}