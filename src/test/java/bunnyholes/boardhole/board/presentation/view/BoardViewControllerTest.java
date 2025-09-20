package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
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
 * BoardViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = BoardViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(dev.xiyo.bunnyholes.boardhole.board.presentation.view.TestSecurityConfig.class) // 테스트용 보안 설정
@DisplayName("BoardViewController 뷰 테스트")
class BoardViewControllerTest {

    private static final String OWNER_ID_STRING = "11111111-1111-1111-1111-111111111111";
    private static final UUID OWNER_ID = UUID.fromString(OWNER_ID_STRING);

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

    @Test
    @DisplayName("비인증 사용자는 글쓰기 버튼을 볼 수 없다")
    @WithAnonymousUser
    void list_Anonymous_ShouldHideWriteButton() throws Exception {
        Page<BoardResult> boardPage = new PageImpl<>(
                List.of(createBoardResult(
                        UUID.randomUUID(),
                        "게스트용 글",
                        "내용",
                        UUID.randomUUID(),
                        "게스트",
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )),
                PageRequest.of(0, 10),
                1
        );

        when(boardQueryService.getBoards(any(), any())).thenReturn(boardPage);

        mockMvc.perform(get("/boards"))
               .andExpect(status().isOk())
               .andExpect(content().string(not(containsString("✍️ 새 글 작성"))));
    }

    @Test
    @DisplayName("게시판 목록 페이지가 정상적으로 렌더링된다")
    @WithMockUser
    void list_ShouldRenderBoardListPage() throws Exception {
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
               // Thymeleaf 렌더링 내용 검증
               .andExpect(content().string(containsString("게시판")))
               .andExpect(content().string(containsString("테스트 게시글 1")))
               .andExpect(content().string(containsString("테스트 게시글 2")))
               .andExpect(content().string(containsString("작성자1")))
               .andExpect(content().string(containsString("작성자2")))
               .andExpect(content().string(containsString("✍️ 새 글 작성")));
    }

    @Test
    @DisplayName("게시글 상세 페이지가 정상적으로 렌더링되고 타이틀이 설정된다")
    @WithAnonymousUser
    void detail_ShouldRenderBoardDetailPageWithTitle() throws Exception {
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
               .andExpect(model().attribute("board", boardDetail))
               // 타이틀이 게시글 제목으로 설정되는지 검증
               .andExpect(xpath("//title").string("Spring Boot 테스트 게시글 - boardholes"))
               // 헤더의 메타 정보 검증 (이모지 제거 확인)
               .andExpect(xpath("//h1").string("Spring Boot 테스트 게시글"))
               .andExpect(content().string(containsString("테스트작성자")))
               .andExpect(content().string(containsString("조회 <span>150</span>")))
               .andExpect(content().string(containsString("09-20 14:30")))
               .andExpect(content().string(containsString("09-20 15:00")))
               // 본문 내용 검증 (줄바꿈 결과 확인)
               .andExpect(content().string(containsString("이것은 테스트 내용입니다.")))
               .andExpect(content().string(containsString("두 번째 줄입니다.")));
    }

    @Test
    @DisplayName("게시글 상세 페이지에서 수정 권한이 있으면 컨텍스트 메뉴가 표시된다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void detail_WithEditPermission_ShouldShowContextMenu() throws Exception {
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
                .thenAnswer(invocation -> OWNER_ID_STRING.equals(((Authentication) invocation.getArgument(0)).getName()));

        // when & then
        mockMvc.perform(get("/boards/{id}", boardId))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("context-menu-anchor")))
               .andExpect(content().string(containsString("context-menu-popover")))
               .andExpect(xpath("//button[@id='context-menu-anchor']").exists())
               .andExpect(xpath("//aside[@id='context-menu-popover']").exists());
    }

    @Test
    @DisplayName("권한이 없으면 컨텍스트 메뉴가 표시되지 않는다")
    @WithMockUser(username = "someone", authorities = {"ROLE_USER"})
    void detail_WithoutPermission_ShouldHideContextMenu() throws Exception {
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

        mockMvc.perform(get("/boards/{id}", boardId))
               .andExpect(status().isOk())
               .andExpect(content().string(not(containsString("context-menu-anchor"))))
               .andExpect(content().string(not(containsString("context-menu-popover"))));
    }

    @Test
    @DisplayName("검색어가 있을 때 게시판 목록이 필터링된다")
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
               // 현재 페이지는 2페이지 (0-indexed이므로 1)
               .andExpect(xpath("//a[@aria-current='page']").string("2"));
    }

    // Helper methods
    private static BoardResult createBoardResult(UUID id, String title, String content, UUID authorId,
                                                 String authorName, Integer viewCount,
                                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BoardResult(id, title, content, authorId, authorName,
                viewCount, createdAt, updatedAt);
    }
}
