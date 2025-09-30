package dev.xiyo.bunnyholes.boardhole.board.presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;
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
import dev.xiyo.bunnyholes.boardhole.shared.exception.ConflictException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
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

    private static Stream<Arguments> listSearchArguments() {
        return Stream.of(
                Arguments.of("검색어 없이", Optional.empty()),
                Arguments.of("검색어와 함께", Optional.of("검색어"))
        );
    }

    private static MockHttpServletRequestBuilder form(MockHttpServletRequestBuilder builder) {
        return builder.contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf());
    }

    private Page<BoardResult> singleBoardPage(Pageable pageable) {
        return new PageImpl<>(List.of(boardResult), pageable, 1);
    }

    private BoardCreateRequest validCreateRequest() {
        return new BoardCreateRequest(boardResult.title(), boardResult.content());
    }

    private BoardUpdateRequest validUpdateRequest() {
        return new BoardUpdateRequest("수정된 제목", "수정된 내용");
    }

    @Nested
    @DisplayName("POST /api/boards - 게시글 작성")
    class CreateBoard {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "writer")
            @DisplayName("✅ 인증된 사용자는 게시글을 생성할 수 있다")
            void shouldCreateBoard() throws Exception {
                BoardCreateRequest request = validCreateRequest();
                CreateBoardCommand command = new CreateBoardCommand("writer", request.title(), request.content());

                given(boardWebMapper.toCreateCommand(any(BoardCreateRequest.class), eq("writer"))).willReturn(command);
                given(boardCommandService.create(command)).willReturn(boardResult);
                given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

                mockMvc.perform(form(post(BOARDS_URL))
                                .param("title", request.title())
                                .param("content", request.content()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(boardResponse.id().toString()))
                        .andExpect(jsonPath("$.title").value(boardResponse.title()))
                        .andExpect(jsonPath("$.content").value(boardResponse.content()));

                ArgumentCaptor<BoardCreateRequest> captor = ArgumentCaptor.forClass(BoardCreateRequest.class);
                then(boardWebMapper).should().toCreateCommand(captor.capture(), eq("writer"));
                then(boardCommandService).should().create(command);
                then(boardWebMapper).should().toResponse(boardResult);

                BoardCreateRequest captured = captor.getValue();
                assertThat(captured.title()).isEqualTo(request.title());
                assertThat(captured.content()).isEqualTo(request.content());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 게시글을 생성할 수 없다")
                void shouldRejectAnonymousUser() throws Exception {
                    mockMvc.perform(form(post(BOARDS_URL))
                                    .param("title", "익명")
                                    .param("content", "익명"))
                            .andExpect(status().isForbidden());

                    then(boardCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "writer")
                @DisplayName("❌ 중복 제목이면 409 ProblemDetail을 반환한다")
                void shouldReturnConflictWhenDuplicateTitle() throws Exception {
                    BoardCreateRequest request = validCreateRequest();
                    CreateBoardCommand command = new CreateBoardCommand("writer", request.title(), request.content());

                    given(boardWebMapper.toCreateCommand(any(BoardCreateRequest.class), eq("writer"))).willReturn(command);
                    given(boardCommandService.create(command)).willThrow(new ConflictException("동일한 제목의 게시글이 존재합니다."));

                    mockMvc.perform(form(post(BOARDS_URL))
                                    .param("title", request.title())
                                    .param("content", request.content()))
                            .andExpect(status().isConflict())
                            .andExpect(jsonPath("$.status").value(409));

                    then(boardCommandService).should().create(command);
                    then(boardWebMapper).should(never()).toResponse(any(BoardResult.class));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @WithMockUser(username = "writer")
                @DisplayName("❌ 제목이 비어있으면 422 ProblemDetail을 반환한다")
                void shouldRejectEmptyTitle() throws Exception {
                    mockMvc.perform(form(post(BOARDS_URL))
                                    .param("title", " ")
                                    .param("content", "내용"))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.errors[0].field").value("title"));

                    then(boardCommandService).shouldHaveNoInteractions();
                    then(boardWebMapper).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/boards - 게시글 목록 조회")
    class ListBoards {

        @Nested
        @DisplayName("성공")
        class Success {

            @ParameterizedTest(name = "✅ {0} 게시글 목록을 조회한다")
            @MethodSource("dev.xiyo.bunnyholes.boardhole.board.presentation.BoardControllerTest#listSearchArguments")
            void shouldListBoards(String description, Optional<String> search) throws Exception {
                Pageable pageable = PageRequest.of(0, 10);
                Page<BoardResult> resultPage = singleBoardPage(pageable);

                given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);
                if (search.isPresent()) {
                    given(boardQueryService.listWithPaging(any(Pageable.class), eq(search.get()))).willReturn(resultPage);
                } else {
                    given(boardQueryService.listWithPaging(any(Pageable.class))).willReturn(resultPage);
                }

                MockHttpServletRequestBuilder requestBuilder = get(BOARDS_URL)
                        .param("page", "0")
                        .param("size", "10");
                search.ifPresent(value -> requestBuilder.param("search", value));

                mockMvc.perform(requestBuilder)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].title").value(boardResponse.title()));

                if (search.isPresent()) {
                    then(boardQueryService).should().listWithPaging(any(Pageable.class), eq(search.get()));
                } else {
                    then(boardQueryService).should().listWithPaging(any(Pageable.class));
                }
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @DisplayName("❌ 내부에서 IllegalArgumentException이 발생하면 422 ProblemDetail을 반환한다")
                void shouldHandleIllegalArgumentException() throws Exception {
                    given(boardQueryService.listWithPaging(any(Pageable.class)))
                            .willThrow(new IllegalArgumentException("일시적인 조회 오류"));

                    mockMvc.perform(get(BOARDS_URL))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.status").value(422));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @DisplayName("❌ 잘못된 정렬 방향이면 400 ProblemDetail을 반환한다")
                void shouldRejectInvalidSortDirection() throws Exception {
                    given(boardQueryService.listWithPaging(any(Pageable.class)))
                            .willThrow(new IllegalArgumentException("Invalid sort direction"));

                    mockMvc.perform(get(BOARDS_URL).param("sort", "createdAt,invalid"))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.status").value(400))
                            .andExpect(jsonPath("$.sort[0]").value("createdAt,invalid"));
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/boards/{id} - 게시글 상세 조회")
    class GetBoard {

        @Nested
        @DisplayName("성공")
        class Success {

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
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @DisplayName("❌ 존재하지 않는 게시글이면 404 ProblemDetail을 반환한다")
                void shouldReturnNotFound() throws Exception {
                    GetBoardQuery query = new GetBoardQuery(boardId);

                    given(boardWebMapper.toGetBoardQuery(boardId)).willReturn(query);
                    given(boardQueryService.handle(query)).willThrow(new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

                    mockMvc.perform(get(BOARDS_URL + "/" + boardId))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));

                    then(boardQueryService).should().handle(query);
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @DisplayName("❌ UUID 형식이 아니면 400 ProblemDetail을 반환한다")
                void shouldRejectInvalidUuid() throws Exception {
                    mockMvc.perform(get(BOARDS_URL + "/invalid-uuid"))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.status").value(400))
                            .andExpect(jsonPath("$.property").value("id"));

                    then(boardQueryService).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/boards/{id} - 게시글 수정")
    class UpdateBoard {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "writer")
            @DisplayName("✅ 게시글 수정에 성공한다")
            void shouldUpdateBoard() throws Exception {
                BoardUpdateRequest request = validUpdateRequest();
                UpdateBoardCommand command = new UpdateBoardCommand(boardId, request.title(), request.content());

                given(boardWebMapper.toUpdateCommand(eq(boardId), any(BoardUpdateRequest.class))).willReturn(command);
                given(boardCommandService.update(command)).willReturn(boardResult);
                given(boardWebMapper.toResponse(boardResult)).willReturn(boardResponse);

                mockMvc.perform(form(put(BOARDS_URL + "/" + boardId))
                                .param("title", request.title())
                                .param("content", request.content()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(boardResponse.id().toString()))
                        .andExpect(jsonPath("$.title").value(boardResponse.title()));

                ArgumentCaptor<BoardUpdateRequest> captor = ArgumentCaptor.forClass(BoardUpdateRequest.class);
                then(boardWebMapper).should().toUpdateCommand(eq(boardId), captor.capture());
                then(boardCommandService).should().update(command);
                then(boardWebMapper).should().toResponse(boardResult);

                BoardUpdateRequest captured = captor.getValue();
                assertThat(captured.title()).isEqualTo(request.title());
                assertThat(captured.content()).isEqualTo(request.content());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 게시글을 수정할 수 없다")
                void shouldRejectAnonymousUpdate() throws Exception {
                    mockMvc.perform(form(put(BOARDS_URL + "/" + boardId))
                                    .param("title", "수정")
                                    .param("content", "수정"))
                            .andExpect(status().isForbidden());

                    then(boardCommandService).shouldHaveNoInteractions();
                    then(boardWebMapper).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "writer")
                @DisplayName("❌ 존재하지 않는 게시글을 수정하면 404 ProblemDetail을 반환한다")
                void shouldReturnNotFoundWhenUpdatingMissingBoard() throws Exception {
                    BoardUpdateRequest request = validUpdateRequest();
                    UpdateBoardCommand command = new UpdateBoardCommand(boardId, request.title(), request.content());

                    given(boardWebMapper.toUpdateCommand(eq(boardId), any(BoardUpdateRequest.class))).willReturn(command);
                    given(boardCommandService.update(command)).willThrow(new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

                    mockMvc.perform(form(put(BOARDS_URL + "/" + boardId))
                                    .param("title", request.title())
                                    .param("content", request.content()))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));

                    then(boardCommandService).should().update(command);
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @WithMockUser(username = "writer")
                @DisplayName("❌ 제목이 너무 길면 422 ProblemDetail을 반환한다")
                void shouldRejectTooLongTitle() throws Exception {
                    String longTitle = "가".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + 1);

                    mockMvc.perform(form(put(BOARDS_URL + "/" + boardId))
                                    .param("title", longTitle)
                                    .param("content", "수정된 내용"))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.errors[0].field").value("title"));

                    then(boardCommandService).shouldHaveNoInteractions();
                    then(boardWebMapper).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/boards/{id} - 게시글 삭제")
    class DeleteBoard {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "writer")
            @DisplayName("✅ 게시글 삭제에 성공한다")
            void shouldDeleteBoard() throws Exception {
                mockMvc.perform(delete(BOARDS_URL + "/" + boardId).with(csrf()))
                        .andExpect(status().isNoContent());

                then(boardCommandService).should().delete(boardId);
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 게시글을 삭제할 수 없다")
                void shouldRejectAnonymousDelete() throws Exception {
                    mockMvc.perform(delete(BOARDS_URL + "/" + boardId).with(csrf()))
                            .andExpect(status().isForbidden());

                    then(boardCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "writer")
                @DisplayName("❌ 존재하지 않는 게시글을 삭제하면 404 ProblemDetail을 반환한다")
                void shouldReturnNotFoundWhenDeletingMissingBoard() throws Exception {
                    willThrow(new ResourceNotFoundException("게시글을 찾을 수 없습니다."))
                            .given(boardCommandService)
                            .delete(boardId);

                    mockMvc.perform(delete(BOARDS_URL + "/" + boardId).with(csrf()))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @WithMockUser(username = "writer")
                @DisplayName("❌ UUID 형식이 아니면 400 ProblemDetail을 반환한다")
                void shouldRejectInvalidUuid() throws Exception {
                    mockMvc.perform(delete(BOARDS_URL + "/invalid-uuid").with(csrf()))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.status").value(400))
                            .andExpect(jsonPath("$.property").value("id"));

                    then(boardCommandService).shouldHaveNoInteractions();
                }
            }
        }
    }
}
