package dev.xiyo.bunnyholes.boardhole.board.presentation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.command.CreateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.command.UpdateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.application.query.GetBoardQuery;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardCreateRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardResponse;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.mapper.BoardWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardController 단위 테스트")
@Tag("unit")
@Tag("board")
class BoardControllerTest {

    @Mock
    private BoardCommandService boardCommandService;

    @Mock
    private BoardQueryService boardQueryService;

    @Mock
    private BoardWebMapper boardWebMapper;

    @InjectMocks
    private BoardController boardController;

    private User testUser;
    private AppUserPrincipal testPrincipal;
    private BoardResult testBoardResult;
    private BoardResponse testBoardResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                       .username("testuser")
                       .password("encoded_password")
                       .name("Test User")
                       .email("test@example.com")
                       .roles(Set.of(Role.USER))
                       .build();
        testPrincipal = new AppUserPrincipal(testUser);

        testBoardResult = new BoardResult(
                UUID.randomUUID(), "Test Title", "Test Content", UUID.randomUUID(),
                "testuser", 0, LocalDateTime.now(), null
        );

        testBoardResponse = new BoardResponse(
                UUID.randomUUID(), "Test Title", "Test Content", UUID.randomUUID(),
                "testuser", 0, LocalDateTime.now(), null
        );

        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
    }

    @Nested
    @DisplayName("POST /api/boards - 게시글 작성")
    class CreateBoard {

        @Test
        @DisplayName("✅ 인증된 사용자가 게시글 작성 성공")
        void shouldCreateBoardSuccessfully() {
            // given
            BoardCreateRequest request = new BoardCreateRequest("Test Title", "Test Content");
            CreateBoardCommand command = new CreateBoardCommand(testUser.getId(), "Test Title", "Test Content");

            given(boardWebMapper.toCreateCommand(request, testUser.getId())).willReturn(command);
            given(boardCommandService.create(command)).willReturn(testBoardResult);
            given(boardWebMapper.toResponse(testBoardResult)).willReturn(
                    testBoardResponse);

            // when
            BoardResponse result = boardController.create(request, testPrincipal);

            // then
            assertThat(result).isEqualTo(testBoardResponse);
            then(boardWebMapper).should().toCreateCommand(request, testUser.getId());
            then(boardCommandService).should().create(command);
            then(boardWebMapper).should().toResponse(testBoardResult);
        }
    }

    @Nested
    @DisplayName("GET /api/boards - 게시글 목록 조회")
    class ListBoards {

        @Test
        @DisplayName("✅ 검색어 없이 전체 게시글 목록 조회")
        void shouldListAllBoards() {
            // given
            Page<BoardResult> resultPage = new PageImpl<>(Collections.singletonList(testBoardResult),
                    pageable, 1);
            Page<BoardResponse> responsePage = new PageImpl<>(Collections.singletonList(testBoardResponse),
                    pageable, 1);

            given(boardQueryService.listWithPaging(pageable)).willReturn(resultPage);
            given(boardWebMapper.toResponse(testBoardResult)).willReturn(
                    testBoardResponse);

            // when
            Page<BoardResponse> result = boardController.list(pageable, null);

            // then
            assertThat(result).isEqualTo(responsePage);
            then(boardQueryService).should().listWithPaging(pageable);
            then(boardWebMapper).should().toResponse(testBoardResult);
        }

        @Test
        @DisplayName("✅ 검색어로 게시글 검색")
        void shouldSearchBoards() {
            // given
            final String searchTerm = "test";
            Page<BoardResult> resultPage = new PageImpl<>(Collections.singletonList(testBoardResult),
                    pageable, 1);
            Page<BoardResponse> responsePage = new PageImpl<>(Collections.singletonList(testBoardResponse),
                    pageable, 1);

            given(boardQueryService.listWithPaging(pageable, searchTerm)).willReturn(resultPage);
            given(boardWebMapper.toResponse(testBoardResult)).willReturn(
                    testBoardResponse);

            // when
            Page<BoardResponse> result = boardController.list(pageable, searchTerm);

            // then
            assertThat(result).isEqualTo(responsePage);
            then(boardQueryService).should().listWithPaging(pageable, searchTerm);
            then(boardWebMapper).should().toResponse(testBoardResult);
        }
    }

    @Nested
    @DisplayName("GET /api/boards/{id} - 게시글 상세 조회")
    class GetBoard {

        @Test
        @DisplayName("✅ 게시글 ID로 조회 성공")
        void shouldGetBoardById() {
            // given
            UUID boardId = UUID.randomUUID();
            GetBoardQuery query = new GetBoardQuery(boardId);

            given(boardWebMapper.toGetBoardQuery(boardId)).willReturn(query);
            given(boardQueryService.handle(query)).willReturn(testBoardResult);
            given(boardWebMapper.toResponse(testBoardResult)).willReturn(
                    testBoardResponse);

            // when
            BoardResponse result = boardController.get(boardId);

            // then
            assertThat(result).isEqualTo(testBoardResponse);
            then(boardWebMapper).should().toGetBoardQuery(boardId);
            then(boardQueryService).should().handle(query);
            then(boardWebMapper).should().toResponse(testBoardResult);
        }
    }

    @Nested
    @DisplayName("PUT /api/boards/{id} - 게시글 수정")
    class UpdateBoard {

        @Test
        @DisplayName("✅ 게시글 수정 성공")
        void shouldUpdateBoardSuccessfully() {
            // given
            UUID boardId = UUID.randomUUID();
            BoardUpdateRequest request = new BoardUpdateRequest("Updated Title", "Updated Content");
            UpdateBoardCommand command = new UpdateBoardCommand(boardId, "Updated Title", "Updated Content");
            BoardResult updatedResult = new BoardResult(
                    boardId, "Updated Title", "Updated Content", UUID.randomUUID(),
                    "testuser", 0, LocalDateTime.now(), LocalDateTime.now()
            );
            BoardResponse updatedResponse = new BoardResponse(
                    boardId, "Updated Title", "Updated Content", UUID.randomUUID(),
                    "testuser", 0, LocalDateTime.now(), LocalDateTime.now()
            );

            given(boardWebMapper.toUpdateCommand(boardId, request)).willReturn(command);
            given(boardCommandService.update(command)).willReturn(updatedResult);
            given(boardWebMapper.toResponse(updatedResult)).willReturn(updatedResponse);

            // when
            BoardResponse result = boardController.update(boardId, request, testPrincipal);

            // then
            assertThat(result).isEqualTo(updatedResponse);
            then(boardWebMapper).should().toUpdateCommand(boardId, request);
            then(boardCommandService).should().update(command);
            then(boardWebMapper).should().toResponse(updatedResult);
        }
    }

    @Nested
    @DisplayName("DELETE /api/boards/{id} - 게시글 삭제")
    class DeleteBoard {

        @Test
        @DisplayName("✅ 게시글 삭제 성공")
        void shouldDeleteBoardSuccessfully() {
            // given
            UUID boardId = UUID.randomUUID();
            willDoNothing().given(boardCommandService).delete(boardId);

            // when
            boardController.delete(boardId);

            // then
            then(boardCommandService).should().delete(boardId);
        }
    }
}
