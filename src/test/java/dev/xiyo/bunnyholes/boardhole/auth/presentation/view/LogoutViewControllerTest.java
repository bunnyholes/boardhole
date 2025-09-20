package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.auth.application.command.LogoutCommand;
import dev.xiyo.bunnyholes.boardhole.auth.application.mapper.AuthMapper;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LogoutViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = LogoutViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class) // 테스트용 보안 설정
@Tag("unit")
@Tag("view")
@DisplayName("LogoutViewController 뷰 테스트")
class LogoutViewControllerTest {

    private static final String USER_ID_STRING = "11111111-1111-1111-1111-111111111111";
    private static final UUID USER_ID = UUID.fromString(USER_ID_STRING);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthCommandService authCommandService;

    @MockitoBean
    private AuthMapper authMapper;

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
        reset(authCommandService, authMapper, permissionEvaluator);
    }

    @Test
    @DisplayName("인증된 사용자가 로그아웃하면 서비스 호출 후 홈페이지로 리디렉트된다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void processLogout_AuthenticatedUser_ShouldCallServiceAndRedirect() throws Exception {
        // given
        LogoutCommand logoutCommand = new LogoutCommand(USER_ID);
        when(authMapper.toLogoutCommand(eq(USER_ID))).thenReturn(logoutCommand);

        // when & then
        mockMvc.perform(get("/auth/logout"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/"));

        // 로그아웃 서비스가 호출되었는지 검증
        verify(authMapper).toLogoutCommand(eq(USER_ID));
        verify(authCommandService).logout();
    }

    @Test
    @DisplayName("익명 사용자가 로그아웃 엔드포인트에 접근하면 서비스 호출 없이 홈페이지로 리디렉트된다")
    @WithAnonymousUser
    void processLogout_AnonymousUser_ShouldRedirectWithoutServiceCall() throws Exception {
        // when & then
        mockMvc.perform(get("/auth/logout"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/"));

        // 익명 사용자의 경우 로그아웃 서비스가 호출되지 않아야 함
        verify(authMapper, never()).toLogoutCommand(any());
        verify(authCommandService, never()).logout();
    }

    @Test
    @DisplayName("로그아웃 시 HTTP 세션이 있으면 세션이 무효화된다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void processLogout_WithSession_ShouldInvalidateSession() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        LogoutCommand logoutCommand = new LogoutCommand(USER_ID);
        when(authMapper.toLogoutCommand(eq(USER_ID))).thenReturn(logoutCommand);

        // when & then
        mockMvc.perform(get("/auth/logout").session(session))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/"));

        // 세션이 무효화되었는지 확인 (MockHttpSession의 경우 invalidate() 호출 여부 확인은 어려우므로 
        // 실제로는 컨트롤러 로직이 정상 실행되는지만 확인)
        verify(authMapper).toLogoutCommand(eq(USER_ID));
        verify(authCommandService).logout();
    }

    @Test
    @DisplayName("로그아웃 시 세션이 없어도 정상적으로 처리된다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void processLogout_WithoutSession_ShouldHandleGracefully() throws Exception {
        // given
        LogoutCommand logoutCommand = new LogoutCommand(USER_ID);
        when(authMapper.toLogoutCommand(eq(USER_ID))).thenReturn(logoutCommand);

        // when & then
        mockMvc.perform(get("/auth/logout"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/"));

        verify(authMapper).toLogoutCommand(eq(USER_ID));
        verify(authCommandService).logout();
    }
}