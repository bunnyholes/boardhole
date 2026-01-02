package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;
import dev.xiyo.bunnyholes.boardhole.board.presentation.mapper.BoardWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ViewControllerAdvice;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * BoardEditViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = BoardEditViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import({ViewSecurityConfig.class, ViewControllerAdvice.class}) // 테스트용 보안 설정 및 예외 처리
@Tag("unit")
@Tag("view")
@DisplayName("BoardEditViewController 뷰 테스트")
class BoardEditViewControllerTest {

    private static final String OWNER_ID_STRING = "11111111-1111-1111-1111-111111111111";
    private static final UUID OWNER_ID = UUID.fromString(OWNER_ID_STRING);
    private static final UUID BOARD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardQueryService boardQueryService;

    @MockitoBean
    private BoardCommandService boardCommandService;

    @MockitoBean
    private BoardWebMapper boardWebMapper;

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
    @DisplayName("수정 권한이 있는 사용자는 수정 폼을 볼 수 있다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void showEditForm_WithPermission_ShouldShowEditForm() throws Exception {
        // given
        var boardDetail = createBoardResult(
                BOARD_ID,
                "수정할 게시글",
                "기존 내용",
                OWNER_ID,
                "작성자",
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        var formRequest = new dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardFormRequest(
                "수정할 게시글",
                "기존 내용"
        );

        when(boardQueryService.getBoard(BOARD_ID)).thenReturn(boardDetail);
        when(boardWebMapper.toFormRequest(boardDetail)).thenReturn(formRequest);
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(get("/boards/{id}/edit", BOARD_ID))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/edit"))
               .andExpect(model().attributeExists("board"))
               .andExpect(model().attribute("board", formRequest));
    }

    @Test
    @DisplayName("수정 권한이 없는 사용자는 403 응답을 받는다")
    @WithMockUser(username = "other-user", authorities = {"ROLE_USER"})
    void showEditForm_WithoutPermission_ShouldReturn403() throws Exception {
        // given
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(false);

        // when & then
        mockMvc.perform(get("/boards/{id}/edit", BOARD_ID))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    @DisplayName("게시글 수정 폼이 기존 데이터로 미리 채워진다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void showEditForm_ShouldPreFillFormWithExistingData() throws Exception {
        // given
        var boardDetail = createBoardResult(
                BOARD_ID,
                "기존 제목",
                "기존 내용\n여러 줄 내용",
                OWNER_ID,
                "작성자",
                15,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        var formRequest = new dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardFormRequest(
                "기존 제목",
                "기존 내용\n여러 줄 내용"
        );

        when(boardQueryService.getBoard(BOARD_ID)).thenReturn(boardDetail);
        when(boardWebMapper.toFormRequest(boardDetail)).thenReturn(formRequest);
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(get("/boards/{id}/edit", BOARD_ID))
               .andExpect(status().isOk())
               .andExpect(model().attribute("board", formRequest))
               .andExpect(content().string(containsString("기존 제목")))
               .andExpect(content().string(containsString("기존 내용")))
               .andExpect(content().string(containsString("여러 줄 내용")));
    }

    @Test
    @DisplayName("유효한 데이터로 게시글 수정이 성공한다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithValidData_ShouldUpdateSuccessfully() throws Exception {
        // given
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "수정된 제목")
                       .param("content", "수정된 내용"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID))
               .andExpect(flash().attribute("success", "게시글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("유효성 검증 실패 시 수정 폼으로 돌아간다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithInvalidData_ShouldReturnToEditForm() throws Exception {
        // given
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then - 빈 제목으로 유효성 검증 실패
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "") // 빈 제목
                       .param("content", "수정된 내용"))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/edit"))
               .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("수정 권한이 없는 사용자는 POST 요청 시 403 응답을 받는다")
    @WithMockUser(username = "other-user", authorities = {"ROLE_USER"})
    void processEdit_WithoutPermission_ShouldReturn403() throws Exception {
        // given
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(false);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "수정된 제목")
                       .param("content", "수정된 내용"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    @DisplayName("긴 제목과 내용으로 수정이 가능하다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithLongContent_ShouldUpdateSuccessfully() throws Exception {
        // given
        var longTitle = "아주 긴 제목".repeat(10); // 최대 길이 내에서
        var longContent = "아주 긴 내용\n".repeat(50); // 여러 줄의 긴 내용

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", longTitle)
                       .param("content", longContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID))
               .andExpect(flash().attribute("success", "게시글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("특수문자가 포함된 제목과 내용으로 수정이 가능하다")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithSpecialCharacters_ShouldUpdateSuccessfully() throws Exception {
        // given
        final var specialTitle = "특수문자 제목! @#$%^&*()";
        final var specialContent = "특수문자 내용:\n<script>alert('test')</script>\n\"quotes\" 'single'";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", specialTitle)
                       .param("content", specialContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID))
               .andExpect(flash().attribute("success", "게시글이 성공적으로 수정되었습니다."));
    }

    // ====== 엣지 케이스 테스트 추가 ======

    @Test
    @DisplayName("제목이 정확히 최대 길이일 때 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_TitleExactMaxLength_ShouldSuccess() throws Exception {
        // given
        String maxLengthTitle = "a".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH);

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", maxLengthTitle)
                       .param("content", "정상 내용"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("제목이 최대 길이 + 1일 때 유효성 검증 실패")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_TitleExceedsMaxLength_ShouldFail() throws Exception {
        // given
        String tooLongTitle = "a".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + 1);

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", tooLongTitle)
                       .param("content", "정상 내용"))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/edit"))
               .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("제목이 공백만 있을 때 유효성 검증 실패")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_TitleWithOnlySpaces_ShouldFail() throws Exception {
        // given
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "   ")  // 공백만
                       .param("content", "정상 내용"))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/edit"))
               .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("내용이 정확히 최대 길이일 때 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_ContentExactMaxLength_ShouldSuccess() throws Exception {
        // given
        String maxLengthContent = "a".repeat(BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH);

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "정상 제목")
                       .param("content", maxLengthContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("내용이 최대 길이 + 1일 때 유효성 검증 실패")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_ContentExceedsMaxLength_ShouldFail() throws Exception {
        // given
        String tooLongContent = "a".repeat(BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH + 1);

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "정상 제목")
                       .param("content", tooLongContent))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/edit"))
               .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("내용이 공백만 있을 때 유효성 검증 실패")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_ContentWithOnlySpaces_ShouldFail() throws Exception {
        // given
        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "정상 제목")
                       .param("content", "   "))  // 공백만
               .andExpect(status().isOk())
               .andExpect(view().name("boards/edit"))
               .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("HTML 태그가 포함된 내용으로 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithHtmlTags_ShouldSuccess() throws Exception {
        // given
        final String htmlContent = "<h1>제목</h1><p>본문</p><script>alert('test');</script>";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "HTML 테스트")
                       .param("content", htmlContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("SQL 인젝션 패턴이 포함된 내용으로 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithSqlInjectionPattern_ShouldSuccess() throws Exception {
        // given
        final String sqlContent = "'; DROP TABLE boards; --";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "SQL 테스트")
                       .param("content", sqlContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("이모지가 포함된 내용으로 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithEmoji_ShouldSuccess() throws Exception {
        // given
        final String emojiTitle = "이모지 테스트 😀 🎉 🔥";
        final String emojiContent = "내용에도 이모지 포함 👍 ❤️ 🌟";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", emojiTitle)
                       .param("content", emojiContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("멀티바이트 문자(한글, 중국어, 일본어)로 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithMultibyteCharacters_ShouldSuccess() throws Exception {
        // given
        final String multibyteTitle = "한글 제목 中文标题 日本語タイトル";
        final String multibyteContent = "한글 내용입니다. 中文内容。日本語の内容です。";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", multibyteTitle)
                       .param("content", multibyteContent))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("여러 줄바꿈이 포함된 내용으로 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithMultipleLineBreaks_ShouldSuccess() throws Exception {
        // given
        final String contentWithLineBreaks = "첫 번째 줄\n\n\n두 번째 줄\r\n\r\n세 번째 줄";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "줄바꿈 테스트")
                       .param("content", contentWithLineBreaks))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("탭 문자가 포함된 내용으로 수정 성공")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithTabCharacters_ShouldSuccess() throws Exception {
        // given
        final String contentWithTabs = "탭\t문자가\t\t포함된\t\t\t내용";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", "탭 문자 테스트")
                       .param("content", contentWithTabs))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 제목과 내용으로 수정")
    @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
    void processEdit_WithLeadingAndTrailingSpaces_ShouldSuccess() throws Exception {
        // given
        final String titleWithSpaces = "  앞뒤 공백 제목  ";
        final String contentWithSpaces = "  앞뒤 공백 내용  ";

        when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/boards/{id}", BOARD_ID)
                       .param("_method", "put")
                       .with(csrf())
                       .param("title", titleWithSpaces)
                       .param("content", contentWithSpaces))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + BOARD_ID));
    }

    // Helper methods
    private static BoardResult createBoardResult(UUID id, String title, String content, UUID authorId,
                                                 String authorName, Integer viewCount,
                                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BoardResult(id, title, content, authorId, authorName,
                viewCount, createdAt, updatedAt);
    }
}
