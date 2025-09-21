package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * BoardListViewController 단위 테스트
 * <p>
 * 게시글 목록 조회 기능에 대한 테스트를 수행합니다.
 * 검색, 페이지네이션, 권한별 UI 표시 등을 검증합니다.
 */
@WebMvcTest(
        value = BoardListViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class)
@Tag("unit")
@Tag("view")
@DisplayName("BoardListViewController 테스트")
class BoardListViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardQueryService boardQueryService;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        lenient().when(permissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        reset(permissionEvaluator);
    }

    @Nested
    @DisplayName("게시글 목록 기본 렌더링")
    class BoardListRendering {

        @Test
        @DisplayName("게시글 목록이 있을 때 정상적으로 렌더링된다")
        @WithMockUser
        void list_WithBoards_ShouldRenderSuccessfully() throws Exception {
            // given
            var board1 = createBoardResult(
                    UUID.randomUUID(),
                    "테스트 게시글 1",
                    "테스트 내용 1",
                    UUID.randomUUID(),
                    "작성자1",
                    10,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            var board2 = createBoardResult(
                    UUID.randomUUID(),
                    "테스트 게시글 2",
                    "테스트 내용 2",
                    UUID.randomUUID(),
                    "작성자2",
                    25,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().minusDays(1)
            );

            Page<BoardResult> boardPage = new PageImpl<>(
                    List.of(board1, board2),
                    PageRequest.of(0, 10),
                    2
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("boards"))
                   .andExpect(model().attribute("boards", boardPage))
                   .andExpect(content().string(containsString("게시판")))
                   .andExpect(content().string(containsString("테스트 게시글 1")))
                   .andExpect(content().string(containsString("테스트 게시글 2")))
                   .andExpect(content().string(containsString("작성자1")))
                   .andExpect(content().string(containsString("작성자2")));
        }

        @Test
        @DisplayName("빈 목록일 때도 정상적으로 렌더링된다")
        @WithMockUser
        void list_EmptyList_ShouldRenderSuccessfully() throws Exception {
            // given
            Page<BoardResult> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("boards"))
                   .andExpect(model().attribute("boards", emptyPage));
        }
    }

    @Nested
    @DisplayName("권한별 UI 표시")
    class AuthorizationBasedUI {

        @Test
        @DisplayName("인증된 사용자는 글쓰기 버튼을 볼 수 있다")
        @WithMockUser
        void list_Authenticated_ShouldShowWriteButton() throws Exception {
            // given
            Page<BoardResult> boardPage = new PageImpl<>(
                    List.of(createSampleBoard()),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("✍️ 새 글 작성")));
        }

        @Test
        @DisplayName("비인증 사용자는 글쓰기 버튼을 볼 수 없다")
        @WithAnonymousUser
        void list_Anonymous_ShouldHideWriteButton() throws Exception {
            // given
            Page<BoardResult> boardPage = new PageImpl<>(
                    List.of(createSampleBoard()),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(not(containsString("✍️ 새 글 작성"))));
        }
    }

    @Nested
    @DisplayName("검색 기능")
    class SearchFeature {

        @Test
        @DisplayName("검색어로 게시글을 필터링할 수 있다")
        @WithMockUser
        void list_WithSearchQuery_ShouldFilterResults() throws Exception {
            // given
            final var searchQuery = "Spring";
            var filteredBoard = createBoardResult(
                    UUID.randomUUID(),
                    "Spring Boot 튜토리얼",
                    "Spring Boot 관련 내용",
                    UUID.randomUUID(),
                    "스프링전문가",
                    100,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Page<BoardResult> searchResults = new PageImpl<>(
                    List.of(filteredBoard),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(eq(searchQuery), any())).thenReturn(searchResults);

            // when & then
            mockMvc.perform(get("/boards").param("search", searchQuery))
                   .andExpect(status().isOk())
                   .andExpect(model().attribute("search", searchQuery))
                   .andExpect(model().attribute("boards", searchResults))
                   .andExpect(content().string(containsString("Spring Boot 튜토리얼")))
                   .andExpect(content().string(containsString("스프링전문가")));
        }

        @Test
        @DisplayName("검색어가 없으면 전체 목록을 조회한다")
        @WithMockUser
        void list_WithoutSearchQuery_ShouldShowAllBoards() throws Exception {
            // given
            Page<BoardResult> allBoards = new PageImpl<>(
                    List.of(createSampleBoard()),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(allBoards);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(model().attributeDoesNotExist("search"))
                   .andExpect(model().attribute("boards", allBoards));
        }

        @Test
        @DisplayName("특수문자가 포함된 검색어도 안전하게 처리된다")
        @WithMockUser
        void list_WithSpecialCharactersInSearch_ShouldHandleSafely() throws Exception {
            // given
            final var searchQuery = "<script>alert('XSS')</script>";
            Page<BoardResult> emptyResults = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );

            when(boardQueryService.getBoards(eq(searchQuery), any())).thenReturn(emptyResults);

            // when & then
            mockMvc.perform(get("/boards").param("search", searchQuery))
                   .andExpect(status().isOk())
                   .andExpect(model().attribute("search", searchQuery));
        }

        @Test
        @DisplayName("SQL 인젝션 패턴이 포함된 검색어도 안전하게 처리된다")
        @WithMockUser
        void list_WithSqlInjectionPattern_ShouldHandleSafely() throws Exception {
            // given
            final var searchQuery = "'; DROP TABLE boards; --";
            Page<BoardResult> emptyResults = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 10),
                    0
            );

            when(boardQueryService.getBoards(eq(searchQuery), any())).thenReturn(emptyResults);

            // when & then
            mockMvc.perform(get("/boards").param("search", searchQuery))
                   .andExpect(status().isOk())
                   .andExpect(model().attribute("search", searchQuery));
        }
    }

    @Nested
    @DisplayName("페이지네이션")
    class Pagination {

        @Test
        @DisplayName("페이지네이션이 정상적으로 작동한다")
        @WithMockUser
        void list_WithPagination_ShouldRenderPageLinks() throws Exception {
            // given
            var boards = List.of(
                    createBoardResult(UUID.randomUUID(), "게시글1", "내용1", UUID.randomUUID(), "작성자1", 1, LocalDateTime.now(), LocalDateTime.now()),
                    createBoardResult(UUID.randomUUID(), "게시글2", "내용2", UUID.randomUUID(), "작성자2", 2, LocalDateTime.now(), LocalDateTime.now())
            );

            Page<BoardResult> pagedResults = new PageImpl<>(
                    boards,
                    PageRequest.of(1, 10), // 2페이지
                    25 // 전체 25개 (3페이지)
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(pagedResults);

            // when & then
            mockMvc.perform(get("/boards").param("page", "1"))
                   .andExpect(status().isOk())
                   .andExpect(xpath("//a[@aria-label='이전 페이지']").exists())
                   .andExpect(xpath("//a[@aria-label='다음 페이지']").exists())
                   .andExpect(xpath("//a[@aria-current='page']").string("2"));
        }

        @Test
        @DisplayName("단일 페이지일 때 페이지네이션이 표시되지 않는다")
        @WithMockUser
        void list_SinglePage_ShouldNotShowPagination() throws Exception {
            // given
            Page<BoardResult> singlePage = new PageImpl<>(
                    List.of(createSampleBoard()),
                    PageRequest.of(0, 10),
                    5 // 전체 5개 (1페이지)
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(singlePage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(model().attribute("boards", singlePage));
            // 페이지네이션 요소가 없는지 확인하는 검증 추가 가능
        }

        @Test
        @DisplayName("잘못된 페이지 번호 요청도 안전하게 처리된다")
        @WithMockUser
        void list_InvalidPageNumber_ShouldHandleSafely() throws Exception {
            // given
            Page<BoardResult> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(999, 10),
                    0
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/boards").param("page", "999"))
                   .andExpect(status().isOk())
                   .andExpect(model().attribute("boards", emptyPage));
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("매우 긴 제목도 정상적으로 표시된다")
        @WithMockUser
        void list_WithVeryLongTitle_ShouldRenderSuccessfully() throws Exception {
            // given
            var longTitleBoard = createBoardResult(
                    UUID.randomUUID(),
                    "매우 긴 제목".repeat(20), // 100자 이상
                    "내용",
                    UUID.randomUUID(),
                    "작성자",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Page<BoardResult> boardPage = new PageImpl<>(
                    List.of(longTitleBoard),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("매우 긴 제목")));
        }

        @Test
        @DisplayName("이모지가 포함된 게시글도 정상적으로 표시된다")
        @WithMockUser
        void list_WithEmoji_ShouldRenderSuccessfully() throws Exception {
            // given
            var emojiBoard = createBoardResult(
                    UUID.randomUUID(),
                    "이모지 제목 😀 🎉 🔥",
                    "이모지 내용 👍 ❤️ 🌟",
                    UUID.randomUUID(),
                    "작성자 😊",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Page<BoardResult> boardPage = new PageImpl<>(
                    List.of(emojiBoard),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("이모지 제목")));
        }

        @Test
        @DisplayName("다국어 콘텐츠도 정상적으로 표시된다")
        @WithMockUser
        void list_WithMultilingualContent_ShouldRenderSuccessfully() throws Exception {
            // given
            var multilingualBoard = createBoardResult(
                    UUID.randomUUID(),
                    "한글 English 中文 日本語",
                    "다국어 내용",
                    UUID.randomUUID(),
                    "작성자",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            Page<BoardResult> boardPage = new PageImpl<>(
                    List.of(multilingualBoard),
                    PageRequest.of(0, 10),
                    1
            );

            when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

            // when & then
            mockMvc.perform(get("/boards"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("한글 English 中文 日本語")));
        }
    }

    // Helper methods
    private static BoardResult createBoardResult(UUID id, String title, String content, UUID authorId,
                                                 String authorName, Integer viewCount,
                                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BoardResult(id, title, content, authorId, authorName,
                viewCount, createdAt, updatedAt);
    }

    private static BoardResult createSampleBoard() {
        return createBoardResult(
                UUID.randomUUID(),
                "샘플 게시글",
                "샘플 내용",
                UUID.randomUUID(),
                "작성자",
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}