package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.time.LocalDateTime;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * BoardDetailViewController 단위 테스트
 * <p>
 * 게시글 상세 조회 기능에 대한 테스트를 수행합니다.
 * 콘텐츠 렌더링, 권한별 UI, 예외 처리 등을 검증합니다.
 */
@WebMvcTest(
        value = BoardDetailViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class)
@Tag("unit")
@Tag("view")
@DisplayName("BoardDetailViewController 테스트")
class BoardDetailViewControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String OWNER_USERNAME = "owner";

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
    @DisplayName("게시글 상세 기본 렌더링")
    class BoardDetailRendering {

        @Test
        @DisplayName("게시글 상세 정보가 정상적으로 표시된다")
        @WithAnonymousUser
        void detail_ShouldRenderBoardDetails() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "Spring Boot 테스트 게시글",
                    "이것은 테스트 내용입니다.\n두 번째 줄입니다.",
                    UUID.randomUUID(),
                    "테스트작성자",
                    150,
                    LocalDateTime.of(2024, 9, 20, 14, 30),
                    LocalDateTime.of(2024, 9, 20, 15, 0)
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(view().name("board/detail"))
                   .andExpect(model().attributeExists("board"))
                   .andExpect(model().attribute("board", boardDetail));
        }

        @Test
        @DisplayName("게시글 제목이 페이지 타이틀에 설정된다")
        @WithAnonymousUser
        void detail_ShouldSetPageTitle() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "Spring Boot 테스트 게시글",
                    "내용",
                    UUID.randomUUID(),
                    "작성자",
                    100,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(xpath("//title").string("Spring Boot 테스트 게시글 - boardholes"));
        }

        @Test
        @DisplayName("작성자, 조회수, 작성일, 수정일이 표시된다")
        @WithAnonymousUser
        void detail_ShouldShowMetaInfo() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "테스트 게시글",
                    "테스트 내용",
                    UUID.randomUUID(),
                    "테스트작성자",
                    150,
                    LocalDateTime.of(2024, 9, 20, 14, 30),
                    LocalDateTime.of(2024, 9, 20, 15, 0)
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("테스트작성자")))
                   .andExpect(content().string(containsString("조회 <span>150</span>")))
                   .andExpect(content().string(containsString("09-20 14:30")))
                   .andExpect(content().string(containsString("09-20 15:00")));
        }
    }

    @Nested
    @DisplayName("컨텍스트 메뉴 표시")
    class ContextMenuDisplay {

        @Test
        @DisplayName("작성자 본인은 수정/삭제 메뉴를 볼 수 있다")
        @WithMockUser(username = OWNER_USERNAME, authorities = {"ROLE_USER"})
        void detail_OwnerShouldSeeContextMenu() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "내가 작성한 게시글",
                    "내용",
                    OWNER_ID,
                    "author",
                    5,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(boardId), eq("BOARD"), eq("WRITE")))
                    .thenAnswer(invocation -> OWNER_USERNAME.equalsIgnoreCase(((Authentication) invocation.getArgument(0)).getName()));

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("context-menu-anchor")))
                   .andExpect(content().string(containsString("context-menu-popover")))
                   .andExpect(xpath("//button[@id='context-menu-anchor']").exists())
                   .andExpect(xpath("//aside[@id='context-menu-popover']").exists());
        }

        @Test
        @DisplayName("다른 사용자는 컨텍스트 메뉴를 볼 수 없다")
        @WithMockUser(username = "someone", authorities = {"ROLE_USER"})
        void detail_OtherUserShouldNotSeeContextMenu() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "다른 사람이 쓴 글",
                    "내용",
                    OWNER_ID,
                    "author",
                    5,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(content().string(not(containsString("context-menu-anchor"))))
                   .andExpect(content().string(not(containsString("context-menu-popover"))));
        }

        @Test
        @DisplayName("관리자는 모든 게시글의 컨텍스트 메뉴를 볼 수 있다")
        @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
        void detail_AdminShouldSeeContextMenu() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "일반 게시글",
                    "내용",
                    UUID.randomUUID(),
                    "author",
                    5,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(boardId), eq("BOARD"), eq("WRITE")))
                    .thenReturn(true); // 관리자는 항상 true

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("context-menu-anchor")))
                   .andExpect(content().string(containsString("context-menu-popover")));
        }

        @Test
        @DisplayName("비인증 사용자는 컨텍스트 메뉴를 볼 수 없다")
        @WithAnonymousUser
        void detail_AnonymousUserShouldNotSeeContextMenu() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "공개 게시글",
                    "내용",
                    UUID.randomUUID(),
                    "author",
                    5,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(content().string(not(containsString("context-menu-anchor"))))
                   .andExpect(content().string(not(containsString("context-menu-popover"))));
        }
    }

    @Nested
    @DisplayName("콘텐츠 렌더링")
    class ContentRendering {

        @Test
        @DisplayName("줄바꿈이 포함된 내용이 정상적으로 표시된다")
        @WithAnonymousUser
        void detail_WithLineBreaks_ShouldRenderCorrectly() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "제목",
                    "첫 번째 줄\n두 번째 줄\n\n네 번째 줄",
                    UUID.randomUUID(),
                    "작성자",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("첫 번째 줄")))
                   .andExpect(content().string(containsString("두 번째 줄")))
                   .andExpect(content().string(containsString("네 번째 줄")));
        }

        @Test
        @DisplayName("HTML 태그가 포함된 내용도 처리된다")
        @WithAnonymousUser
        void detail_WithHtmlTags_ShouldBeProcessed() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "제목",
                    "<script>alert('XSS')</script><h1>제목</h1>",
                    UUID.randomUUID(),
                    "작성자",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(view().name("board/detail"))
                   .andExpect(model().attribute("board", boardDetail));
        }

        @Test
        @DisplayName("이모지가 포함된 내용이 정상적으로 표시된다")
        @WithAnonymousUser
        void detail_WithEmoji_ShouldRenderCorrectly() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var boardDetail = createBoardResult(
                    boardId,
                    "이모지 제목 😀 🎉",
                    "이모지 내용 👍 ❤️ 🌟",
                    UUID.randomUUID(),
                    "작성자 😊",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(view().name("board/detail"));
        }

        @Test
        @DisplayName("매우 긴 내용도 정상적으로 표시된다")
        @WithAnonymousUser
        void detail_WithVeryLongContent_ShouldRenderSuccessfully() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            var longContent = "긴 내용 ".repeat(1000); // 5000자 이상
            var boardDetail = createBoardResult(
                    boardId,
                    "제목",
                    longContent,
                    UUID.randomUUID(),
                    "작성자",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(boardQueryService.getBoard(boardId)).thenReturn(boardDetail);

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isOk())
                   .andExpect(view().name("board/detail"));
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("존재하지 않는 게시글 접근 시 404 에러가 발생한다")
        @WithAnonymousUser
        void detail_NotFound_ShouldReturn404() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            when(boardQueryService.getBoard(boardId))
                    .thenThrow(new ResourceNotFoundException("게시글을 찾을 수 없습니다"));

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 요청 시 400 에러 페이지로 리디렉트된다")
        @WithAnonymousUser
        void detail_InvalidUuidFormat_ShouldRedirectTo400() throws Exception {
            // when & then
            mockMvc.perform(get("/boards/invalid-uuid"))
                   .andExpect(status().isFound())
                   .andExpect(redirectedUrl("/error/400"));
        }

        @Test
        @DisplayName("서비스 예외 발생 시 적절히 처리된다")
        @WithAnonymousUser
        void detail_ServiceException_ShouldBeHandled() throws Exception {
            // given
            var boardId = UUID.randomUUID();
            when(boardQueryService.getBoard(boardId))
                    .thenThrow(new RuntimeException("서비스 오류"));

            // when & then
            mockMvc.perform(get("/boards/{id}", boardId))
                   .andExpect(status().is5xxServerError());
        }
    }

    // Helper methods
    private static BoardResult createBoardResult(UUID id, String title, String content, UUID authorId,
                                                 String authorName, Integer viewCount,
                                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BoardResult(id, title, content, authorId, authorName,
                viewCount, createdAt, updatedAt);
    }
}