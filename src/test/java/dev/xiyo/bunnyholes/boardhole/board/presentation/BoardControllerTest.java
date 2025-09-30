package dev.xiyo.bunnyholes.boardhole.board.presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import dev.xiyo.bunnyholes.boardhole.shared.config.ApiSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@Import(ApiSecurityConfig.class)
@DisplayName("BoardController MockMvc 테스트")
@Tag("unit")
@Tag("board")
class BoardControllerTest {

    private static final String BOARDS_URL = ApiPaths.BOARDS;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardCommandService boardCommandService;

    @MockitoBean
    private BoardQueryService boardQueryService;

    @MockitoBean
    private BoardWebMapper boardWebMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private EntityManager entityManager;

    private UUID boardId;
    private BoardResult boardResult;
    private BoardResponse boardResponse;

    @BeforeEach
    void setUp() {
        boardId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        boardResult = new BoardResult(boardId, "테스트 제목", "테스트 내용", UUID.randomUUID(), "작성자", 10,
                LocalDateTime.now(), LocalDateTime.now());
        boardResponse = new BoardResponse(boardResult.id(), boardResult.title(), boardResult.content(), boardResult.authorId(),
                boardResult.authorName(), boardResult.viewCount(), boardResult.createdAt(), boardResult.updatedAt());
    }

    @Nested
    @DisplayName("POST /api/boards - 게시글 작성")
    class CreateBoard {

        @Test
        @WithMockUser(username = "writer", roles = "USER")
        @DisplayName("✅ 인증된 사용자는 게시글을 생성할 수 있다")
        void shouldCreateBoard() throws Exception {
            BoardCreateRequest request = new BoardCreateRequest(boardResult.title(), boardResult.content());
            CreateBoardCommand command = new CreateBoardCommand("writer", request.title(), request.content());

            given(boardWebMapper.toCreateCommand(any(BoardCreateRequest.class), eq("writer"))).willReturn(command);
            given(boardCommandService.create(command)).willReturn(boardResult);
            given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

            mockMvc.perform(post(BOARDS_URL)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", request.title())
                            .param("content", request.content())
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(boardResponse.id().toString()))
                    .andExpect(jsonPath("$.title").value(boardResponse.title()))
                    .andExpect(jsonPath("$.content").value(boardResponse.content()));

            then(boardWebMapper).should().toCreateCommand(any(BoardCreateRequest.class), eq("writer"));
            then(boardCommandService).should().create(command);
            then(boardWebMapper).should().toResponse(boardResult);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 게시글을 생성할 수 없다")
        void shouldRejectAnonymousUser() throws Exception {
            mockMvc.perform(post(BOARDS_URL)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", "익명")
                            .param("content", "익명")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            then(boardCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("GET /api/boards - 게시글 목록 조회")
    class ListBoards {

        @Test
        @DisplayName("✅ 검색어 없이 게시글 목록을 조회한다")
        void shouldListBoardsWithoutSearch() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BoardResult> resultPage = new PageImpl<>(List.of(boardResult), pageable, 1);

            given(boardQueryService.listWithPaging(any(Pageable.class))).willReturn(resultPage);
            given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

            mockMvc.perform(get(BOARDS_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value(boardResponse.title()))
                    .andExpect(jsonPath("$.content[0].authorName").value(boardResponse.authorName()));

            then(boardQueryService).should().listWithPaging(any(Pageable.class));
            then(boardQueryService).should(never()).listWithPaging(any(Pageable.class), anyString());
        }

        @Test
        @DisplayName("✅ 검색어를 사용해 게시글을 조회한다")
        void shouldListBoardsWithSearch() throws Exception {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BoardResult> resultPage = new PageImpl<>(List.of(boardResult), pageable, 1);
            String keyword = "검색어";

            given(boardQueryService.listWithPaging(any(Pageable.class), eq(keyword))).willReturn(resultPage);
            given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

            mockMvc.perform(get(BOARDS_URL)
                            .param("search", keyword))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value(boardResponse.title()));

            then(boardQueryService).should().listWithPaging(any(Pageable.class), eq(keyword));
        }
    }

    @Nested
    @DisplayName("GET /api/boards/{id} - 게시글 상세 조회")
    class GetBoard {

        @Test
        @DisplayName("✅ 게시글 상세 정보를 조회한다")
        void shouldGetBoard() throws Exception {
            GetBoardQuery query = new GetBoardQuery(boardId);

            given(boardWebMapper.toGetBoardQuery(boardId)).willReturn(query);
            given(boardQueryService.handle(query)).willReturn(boardResult);
            given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

            mockMvc.perform(get(BOARDS_URL + "/" + boardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(boardResponse.id().toString()))
                    .andExpect(jsonPath("$.authorName").value(boardResponse.authorName()));

            then(boardWebMapper).should().toGetBoardQuery(boardId);
            then(boardQueryService).should().handle(query);
            then(boardWebMapper).should().toResponse(boardResult);
        }
    }

    @Nested
    @DisplayName("PUT /api/boards/{id} - 게시글 수정")
    class UpdateBoard {

        @Test
        @WithMockUser(username = "writer", roles = "USER")
        @DisplayName("✅ 게시글 수정에 성공한다")
        void shouldUpdateBoard() throws Exception {
            BoardUpdateRequest request = new BoardUpdateRequest("수정된 제목", "수정된 내용");
            UpdateBoardCommand command = new UpdateBoardCommand(boardId, request.title(), request.content());

            given(boardWebMapper.toUpdateCommand(eq(boardId), any(BoardUpdateRequest.class))).willReturn(command);
            given(boardCommandService.update(command)).willReturn(boardResult);
            given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

            mockMvc.perform(put(BOARDS_URL + "/" + boardId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", request.title())
                            .param("content", request.content())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(boardResponse.id().toString()))
                    .andExpect(jsonPath("$.title").value(boardResponse.title()));

            then(boardWebMapper).should().toUpdateCommand(eq(boardId), any(BoardUpdateRequest.class));
            then(boardCommandService).should().update(command);
            then(boardWebMapper).should().toResponse(boardResult);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 게시글을 수정할 수 없다")
        void shouldRejectAnonymousUpdate() throws Exception {
            mockMvc.perform(put(BOARDS_URL + "/" + boardId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", "수정")
                            .param("content", "수정")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            then(boardCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("DELETE /api/boards/{id} - 게시글 삭제")
    class DeleteBoard {

        @Test
        @WithMockUser(username = "writer", roles = "USER")
        @DisplayName("✅ 게시글 삭제에 성공한다")
        void shouldDeleteBoard() throws Exception {
            mockMvc.perform(delete(BOARDS_URL + "/" + boardId).with(csrf()))
                    .andExpect(status().isNoContent());

            then(boardCommandService).should().delete(boardId);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 게시글을 삭제할 수 없다")
        void shouldRejectAnonymousDelete() throws Exception {
            mockMvc.perform(delete(BOARDS_URL + "/" + boardId).with(csrf()))
                    .andExpect(status().isUnauthorized());

            then(boardCommandService).shouldHaveNoInteractions();
        }
    }
}
