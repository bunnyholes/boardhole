package dev.xiyo.bunnyholes.boardhole.board.application.mapper;

import java.util.UUID;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import dev.xiyo.bunnyholes.boardhole.board.application.command.UpdateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.event.ViewedEvent;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.domain.Board;

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
    ViewedEvent toViewedEvent(UUID boardId);

    /**
     * 게시글 업데이트 - null이 아닌 값만 업데이트
     *
     * @param command 업데이트 명령
     * @param board   업데이트할 게시글 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBoardFromCommand(UpdateBoardCommand command, @MappingTarget Board board);
}
