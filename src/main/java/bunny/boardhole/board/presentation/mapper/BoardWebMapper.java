package bunny.boardhole.board.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mapping;

import bunny.boardhole.board.application.command.CreateBoardCommand;
import bunny.boardhole.board.application.command.UpdateBoardCommand;
import bunny.boardhole.board.application.query.GetBoardQuery;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.presentation.dto.BoardCreateRequest;
import bunny.boardhole.board.presentation.dto.BoardResponse;
import bunny.boardhole.board.presentation.dto.BoardUpdateRequest;

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
    CreateBoardCommand toCreateCommand(BoardCreateRequest req, Long authorId);

    /**
     * 게시글 수정 요청을 명령으로 변환
     *
     * @param id       수정할 게시글 ID
     * @param authorId 작성자 ID
     * @param req      게시글 수정 요청 DTO
     * @return 게시글 수정 명령
     */
    @Mapping(target = "boardId", source = "id")
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "title", source = "req.title")
    @Mapping(target = "content", source = "req.content")
    UpdateBoardCommand toUpdateCommand(Long id, Long authorId, BoardUpdateRequest req);

    /**
     * ID로 게시글 조회 쿼리 생성
     *
     * @param id 게시글 ID
     * @return 게시글 조회 쿼리
     */
    GetBoardQuery toGetBoardQuery(Long id);

}
