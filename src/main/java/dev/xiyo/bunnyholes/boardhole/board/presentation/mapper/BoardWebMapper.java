package dev.xiyo.bunnyholes.boardhole.board.presentation.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import dev.xiyo.bunnyholes.boardhole.board.application.command.CreateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.command.UpdateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.query.GetBoardQuery;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardCreateRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardFormRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardResponse;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardUpdateRequest;

/**
 * 게시글 웹 계층 매퍼
 * 게시글 웹 DTO와 애플리케이션 Command/Result 간 매핑을 담당합니다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface BoardWebMapper {

    /**
     * 게시글 결과를 웹 응답으로 변환
     *
     * @param result 게시글 조회 결과
     * @return 웹 응답 DTO
     */
    BoardResponse toResponse(BoardResult result);

    /**
     * 게시글 생성 요청을 명령으로 변환
     *
     * @param req      게시글 생성 요청 DTO
     * @param authorId 작성자 ID
     * @return 게시글 생성 명령
     */
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "title", source = "req.title")
    @Mapping(target = "content", source = "req.content")
    CreateBoardCommand toCreateCommand(BoardCreateRequest req, UUID authorId);

    /**
     * 게시글 수정 요청을 명령으로 변환
     *
     * @param id       수정할 게시글 ID
     * @param authorId 작성자 ID
     * @param req      게시글 수정 요청 DTO
     * @return 게시글 수정 명령
     */
    @Mapping(target = "boardId", source = "id")
    @Mapping(target = "title", source = "req.title")
    @Mapping(target = "content", source = "req.content")
    UpdateBoardCommand toUpdateCommand(UUID id, BoardUpdateRequest req);

    /**
     * ID로 게시글 조회 쿼리 생성
     *
     * @param id 게시글 ID
     * @return 게시글 조회 쿼리
     */
    GetBoardQuery toGetBoardQuery(UUID id);

    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "title", source = "formRequest.title")
    @Mapping(target = "content", source = "formRequest.content")
    CreateBoardCommand toCreateCommand(BoardFormRequest formRequest, UUID authorId);

    @Mapping(target = "boardId", source = "id")
    @Mapping(target = "title", source = "formRequest.title")
    @Mapping(target = "content", source = "formRequest.content")
    UpdateBoardCommand toUpdateCommand(UUID id, BoardFormRequest formRequest);

    BoardFormRequest toFormRequest(BoardResult board);
}
