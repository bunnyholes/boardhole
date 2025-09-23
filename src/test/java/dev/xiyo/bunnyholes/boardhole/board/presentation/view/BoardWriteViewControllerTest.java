package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.command.CreateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardFormRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.mapper.BoardWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * BoardWriteViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = BoardWriteViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class) // 테스트용 보안 설정
@Tag("unit")
@Tag("view")
@DisplayName("BoardWriteViewController 뷰 테스트")
class BoardWriteViewControllerTest {

    private static final String USERNAME = "testuser";
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

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

    private static User createMockUser() {
        var user = User.builder()
                       .username(USERNAME)
                       .password("password123")
                       .name("Test User")
                       .email("test@example.com")
                       .roles(Set.of(Role.USER))
                       .build();

        // Use reflection to set the ID for testing purposes
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, USER_ID);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }

        return user;
    }

    @Test
    @DisplayName("비인증 사용자는 글쓰기 폼에 접근할 수 없다")
    @WithAnonymousUser
    void showWriteForm_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/boards/write"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("인증된 사용자는 글쓰기 폼을 볼 수 있다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void showWriteForm_Authenticated_ShouldRenderWriteForm() throws Exception {
        mockMvc.perform(get("/boards/write"))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/write"))
               .andExpect(model().attributeExists("board"))
               .andExpect(model().attribute("board", BoardFormRequest.empty()))
               .andExpect(content().string(containsString("게시글 작성하기")))
               .andExpect(content().string(containsString("제목")))
               .andExpect(content().string(containsString("내용")));
    }

    @Test
    @DisplayName("비인증 사용자는 글쓰기를 제출할 수 없다")
    @WithAnonymousUser
    void processWrite_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/boards/write")
                       .with(csrf())
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("title", "테스트 제목")
                       .param("content", "테스트 내용"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("유효한 게시글 작성 시 성공적으로 저장되고 상세 페이지로 리디렉트된다")
    void processWrite_ValidData_ShouldCreateBoardAndRedirect() throws Exception {
        // given
        var boardId = UUID.randomUUID();
        var expectedResult = new BoardResult(
                boardId,
                "테스트 제목",
                "테스트 내용",
                USER_ID,
                "testuser",
                0,
                null,
                null
        );
        var mockUser = createMockUser();
        UserDetails mockPrincipal = org.springframework.security.core.userdetails.User.withUsername(USERNAME)
                                                                                      .password(mockUser.getPassword())
                                                                                      .authorities("ROLE_USER")
                                                                                      .build();
        var mockCommand = new CreateBoardCommand(USERNAME, "테스트 제목", "테스트 내용");

        when(boardWebMapper.toCreateCommand(any(BoardFormRequest.class), eq(USERNAME))).thenReturn(mockCommand);
        when(boardCommandService.create(mockCommand)).thenReturn(expectedResult);

        // when & then
        mockMvc.perform(post("/boards/write")
                       .with(csrf())
                       .with(user(mockPrincipal))
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("title", "테스트 제목")
                       .param("content", "테스트 내용"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards/" + boardId));
    }

    @Test
    @DisplayName("제목이 비어있으면 유효성 검증에 실패하고 폼으로 돌아간다")
    void processWrite_EmptyTitle_ShouldReturnToForm() throws Exception {
        // given
        var mockUser = createMockUser();
        UserDetails mockPrincipal = org.springframework.security.core.userdetails.User.withUsername(USERNAME)
                                                                                      .password(mockUser.getPassword())
                                                                                      .authorities("ROLE_USER")
                                                                                      .build();

        // when & then
        mockMvc.perform(post("/boards/write")
                       .with(csrf())
                       .with(user(mockPrincipal))
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("title", "")
                       .param("content", "테스트 내용"))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/write"))
               .andExpect(model().attributeHasFieldErrors("board", "title"));
    }

    @Test
    @DisplayName("내용이 비어있으면 유효성 검증에 실패하고 폼으로 돌아간다")
    void processWrite_EmptyContent_ShouldReturnToForm() throws Exception {
        // given
        var mockUser = createMockUser();
        UserDetails mockPrincipal = org.springframework.security.core.userdetails.User.withUsername(USERNAME)
                                                                                      .password(mockUser.getPassword())
                                                                                      .authorities("ROLE_USER")
                                                                                      .build();

        // when & then
        mockMvc.perform(post("/boards/write")
                       .with(csrf())
                       .with(user(mockPrincipal))
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("title", "테스트 제목")
                       .param("content", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/write"))
               .andExpect(model().attributeHasFieldErrors("board", "content"));
    }

    @Test
    @DisplayName("제목과 내용이 모두 비어있으면 두 필드 모두 유효성 검증에 실패한다")
    void processWrite_EmptyTitleAndContent_ShouldReturnToFormWithErrors() throws Exception {
        // given
        var mockUser = createMockUser();
        UserDetails mockPrincipal = org.springframework.security.core.userdetails.User.withUsername(USERNAME)
                                                                                      .password(mockUser.getPassword())
                                                                                      .authorities("ROLE_USER")
                                                                                      .build();

        // when & then
        mockMvc.perform(post("/boards/write")
                       .with(csrf())
                       .with(user(mockPrincipal))
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("title", "")
                       .param("content", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("boards/write"))
               .andExpect(model().attributeHasFieldErrors("board", "title"))
               .andExpect(model().attributeHasFieldErrors("board", "content"));
    }
}
