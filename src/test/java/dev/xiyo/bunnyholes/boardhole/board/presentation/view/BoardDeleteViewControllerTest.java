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

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BoardDeleteViewController 단위 테스트
 * <p>
 * 게시글 삭제 기능에 대한 테스트를 수행합니다.
 * 권한 검증, 삭제 처리, 예외 처리 등을 검증합니다.
 */
@WebMvcTest(
        value = BoardDeleteViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class)
@Tag("unit")
@Tag("view")
@DisplayName("BoardDeleteViewController 테스트")
class BoardDeleteViewControllerTest {

    private static final String OWNER_ID_STRING = "11111111-1111-1111-1111-111111111111";
    private static final UUID OWNER_ID = UUID.fromString(OWNER_ID_STRING);
    private static final UUID BOARD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardCommandService boardCommandService;

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
    @DisplayName("권한 검증")
    class AuthorizationTests {

        @Test
        @DisplayName("작성자 본인은 게시글을 삭제할 수 있다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_ByOwner_ShouldSucceed() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doNothing().when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/boards"))
                   .andExpect(flash().attribute("success", "게시글이 성공적으로 삭제되었습니다."));

            verify(boardCommandService).delete(BOARD_ID);
        }

        @Test
        @DisplayName("관리자는 모든 게시글을 삭제할 수 있다")
        @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
        void delete_ByAdmin_ShouldSucceed() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doNothing().when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/boards"))
                   .andExpect(flash().attribute("success", "게시글이 성공적으로 삭제되었습니다."));

            verify(boardCommandService).delete(BOARD_ID);
        }

        @Test
        @DisplayName("다른 사용자는 게시글을 삭제할 수 없다")
        @WithMockUser(username = "other-user", authorities = {"ROLE_USER"})
        void delete_ByOtherUser_ShouldBeDenied() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(false);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/error/403"));

            verify(boardCommandService, never()).delete(any());
        }

        @Test
        @DisplayName("비인증 사용자는 게시글을 삭제할 수 없다")
        @WithAnonymousUser
        void delete_ByAnonymousUser_ShouldRedirectToLogin() throws Exception {
            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/auth/login"));

            verify(boardCommandService, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("삭제 처리")
    class DeleteProcessing {

        @Test
        @DisplayName("DELETE 메서드로 직접 요청 시 삭제가 처리된다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_WithDeleteMethod_ShouldSucceed() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doNothing().when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(delete("/boards/{id}", BOARD_ID)
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/boards"))
                   .andExpect(flash().attribute("success", "게시글이 성공적으로 삭제되었습니다."));

            verify(boardCommandService).delete(BOARD_ID);
        }

        @Test
        @DisplayName("POST with _method=delete로 요청 시 삭제가 처리된다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_WithHiddenMethodField_ShouldSucceed() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doNothing().when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/boards"))
                   .andExpect(flash().attribute("success", "게시글이 성공적으로 삭제되었습니다."));

            verify(boardCommandService).delete(BOARD_ID);
        }

        @Test
        @DisplayName("삭제 성공 시 목록 페이지로 리다이렉트된다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_Success_ShouldRedirectToList() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doNothing().when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/boards"));
        }

        @Test
        @DisplayName("삭제 성공 시 성공 메시지가 설정된다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_Success_ShouldSetSuccessMessage() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doNothing().when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(flash().attribute("success", "게시글이 성공적으로 삭제되었습니다."));
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 404 에러가 발생한다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_NotFound_ShouldReturn404() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doThrow(new ResourceNotFoundException("게시글을 찾을 수 없습니다"))
                    .when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("잘못된 UUID 형식으로 삭제 요청 시 400 에러 페이지로 리디렉트된다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_InvalidUuid_ShouldRedirectTo400() throws Exception {
            // when & then
            mockMvc.perform(post("/boards/invalid-uuid")
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().isFound())
                   .andExpect(redirectedUrl("/error/400"));
        }

        @Test
        @DisplayName("CSRF 토큰 없이 삭제 요청 시 403 에러가 발생한다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_WithoutCsrf_ShouldReturn403() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete"))
                   // CSRF 토큰 없이 요청
                   .andExpect(status().isForbidden());

            verify(boardCommandService, never()).delete(any());
        }

        @Test
        @DisplayName("서비스 예외 발생 시 적절히 처리된다")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_ServiceException_ShouldBeHandled() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doThrow(new RuntimeException("서비스 오류"))
                    .when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("동일한 게시글에 대한 중복 삭제 요청 처리")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_DuplicateRequest_ShouldHandleGracefully() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            
            // 첫 번째 요청은 성공, 두 번째 요청은 Not Found
            doNothing()
                    .doThrow(new ResourceNotFoundException("이미 삭제된 게시글입니다"))
                    .when(boardCommandService).delete(BOARD_ID);

            // when & then - 첫 번째 요청
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().is3xxRedirection())
                   .andExpect(redirectedUrl("/boards"));

            // when & then - 두 번째 요청 (이미 삭제됨)
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
                   .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("트랜잭션 롤백 시나리오 처리")
        @WithMockUser(username = OWNER_ID_STRING, authorities = {"ROLE_USER"})
        void delete_TransactionRollback_ShouldHandleGracefully() throws Exception {
            // given
            when(permissionEvaluator.hasPermission(any(Authentication.class), eq(BOARD_ID), eq("BOARD"), eq("DELETE")))
                    .thenReturn(true);
            doThrow(new RuntimeException("트랜잭션 롤백"))
                    .when(boardCommandService).delete(BOARD_ID);

            // when & then
            mockMvc.perform(post("/boards/{id}", BOARD_ID)
                           .param("_method", "delete")
                           .with(csrf()))
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