package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * UserDetailViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = UserDetailViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class)
@Tag("unit")
@Tag("view")
@DisplayName("UserDetailViewController 뷰 테스트")
class UserDetailViewControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USERNAME = "testuser";
    private static final String NAME = "테스트사용자";
    private static final String EMAIL = "test@example.com";
    private static final String ADMIN_USERNAME = "admin";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @MockitoBean
    private dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper userWebMapper;

    @Test
    @DisplayName("비인증 사용자는 마이페이지에 접근할 수 없다")
    void mypage_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/users/me"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("인증된 사용자는 마이페이지에 접근할 수 있다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void mypage_Authenticated_ShouldRenderMyPage() throws Exception {
        // given
        var userResult = createUserResult(USER_ID, USERNAME, NAME, EMAIL, Set.of(Role.USER));

        when(userQueryService.getUser(USERNAME)).thenReturn(userResult);
        when(userWebMapper.toResponse(userResult)).thenReturn(toResponse(userResult));

        // when & then
        mockMvc.perform(get("/users/me"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/detail"))
               .andExpect(model().attributeExists("user"));

        verify(userQueryService).getUser(USERNAME);
    }

    @Test
    @DisplayName("사용자 정보가 없을 때 기본 객체가 설정된다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void mypage_UserNotFound_ShouldSetDefaultObject() throws Exception {
        // given
        when(userQueryService.getUser(USERNAME)).thenReturn(null);
        when(userWebMapper.toResponse(null)).thenReturn(new UserResponse(null, USERNAME, "", null, null, null, Set.of()));

        // when & then
        mockMvc.perform(get("/users/me"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/detail"))
               .andExpect(model().attributeExists("user"));

        verify(userQueryService).getUser(USERNAME);
    }

    @Test
    @DisplayName("프로필 수정이 성공하면 마이페이지로 리디렉트된다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updateProfile_ValidInput_ShouldRedirectToMyPage() throws Exception {
        // given
        final String newName = "새로운이름";
        var userResult = createUserResult(USER_ID, USERNAME, NAME, EMAIL, Set.of(Role.USER));
        when(userQueryService.getUser(USERNAME)).thenReturn(userResult);
        when(userWebMapper.toResponse(userResult)).thenReturn(toResponse(userResult));

        // when & then
        mockMvc.perform(patch("/users/me")
                       .with(csrf())
                       .param("name", newName))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"));

        verify(userCommandService).update(any());
    }

    @Test
    @DisplayName("인증된 사용자는 프로필 수정 폼에 접근할 수 있다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void editProfile_Authenticated_ShouldRenderEditForm() throws Exception {
        // given
        var userResult = createUserResult(USER_ID, USERNAME, NAME, EMAIL, Set.of(Role.USER));

        when(userQueryService.getUser(USERNAME)).thenReturn(userResult);
        when(userWebMapper.toResponse(userResult)).thenReturn(toResponse(userResult));

        // when & then
        mockMvc.perform(get("/users/me/edit"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/edit"))
               .andExpect(model().attributeExists("user"))
               .andExpect(model().attributeExists("updateUserRequest"));

        verify(userQueryService).getUser(USERNAME);
    }

    @Test
    @DisplayName("비인증 사용자는 프로필 수정 폼에 접근할 수 없다")
    void editProfile_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/users/me/edit"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("비인증 사용자는 프로필 수정을 할 수 없다")
    void updateProfile_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(patch("/users/me")
                       .with(csrf())
                       .param("name", "새이름"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("잘못된 입력으로 프로필 수정 시 에러 메시지와 함께 폼으로 돌아간다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updateProfile_InvalidInput_ShouldReturnToForm() throws Exception {
        var userResult = createUserResult(USER_ID, USERNAME, NAME, EMAIL, Set.of(Role.USER));
        when(userQueryService.getUser(USERNAME)).thenReturn(userResult);
        when(userWebMapper.toResponse(userResult)).thenReturn(toResponse(userResult));

        // when & then - 빈 이름으로 요청
        mockMvc.perform(patch("/users/me")
                       .with(csrf())
                       .param("name", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("user/edit"));
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 상세 페이지를 조회할 수 있다")
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ROLE_ADMIN"})
    void userProfile_AdminRole_ShouldRenderDetailPage() throws Exception {
        // given
        String targetUsername = "profileuser";
        var adminResult = createUserResult(UUID.randomUUID(), ADMIN_USERNAME, "관리자", "admin@example.com", Set.of(Role.ADMIN));
        var targetResult = createUserResult(UUID.randomUUID(), targetUsername, "프로필 사용자", "profile@example.com", Set.of(Role.USER));

        when(userQueryService.getUser(ADMIN_USERNAME)).thenReturn(adminResult);
        when(userWebMapper.toResponse(adminResult)).thenReturn(toResponse(adminResult));
        when(userQueryService.getUser(targetUsername)).thenReturn(targetResult);
        when(userWebMapper.toResponse(targetResult)).thenReturn(toResponse(targetResult));

        // when & then
        mockMvc.perform(get("/users/{username}", targetUsername))
               .andExpect(status().isOk())
               .andExpect(view().name("user/detail"))
               .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("관리자 권한이 없으면 다른 사용자의 상세 페이지에 접근할 수 없다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void userProfile_NonAdmin_ShouldBeForbidden() throws Exception {
        // given - 현재 로그인 사용자의 정보는 로딩되어야 함
        var self = createUserResult(USER_ID, USERNAME, NAME, EMAIL, Set.of(Role.USER));
        when(userQueryService.getUser(USERNAME)).thenReturn(self);
        when(userWebMapper.toResponse(self)).thenReturn(toResponse(self));

        // when & then
        mockMvc.perform(get("/users/{username}", "someoneElse"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    @DisplayName("관리자 권한을 가진 사용자도 마이페이지에 접근할 수 있다")
    @WithMockUser(username = ADMIN_USERNAME, authorities = {"ROLE_ADMIN"})
    void mypage_AdminUser_ShouldRenderMyPage() throws Exception {
        // given
        var adminUserResult = createUserResult(USER_ID, ADMIN_USERNAME, "관리자", "admin@example.com", Set.of(Role.ADMIN, Role.USER));

        when(userQueryService.getUser(ADMIN_USERNAME)).thenReturn(adminUserResult);
        when(userWebMapper.toResponse(adminUserResult)).thenReturn(toResponse(adminUserResult));

        // when & then
        mockMvc.perform(get("/users/me"))
               .andExpect(status().isOk())
               .andExpect(view().name("user/detail"))
               .andExpect(model().attributeExists("user"));

        verify(userQueryService).getUser(ADMIN_USERNAME);
    }

    private static UserResult createUserResult(UUID id, String username, String name, String email, Set<Role> roles) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(7);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime lastLogin = LocalDateTime.now().minusHours(2);
        return new UserResult(id, username, name, email, createdAt, updatedAt, lastLogin, roles);
    }

    private static UserResponse toResponse(UserResult result) {
        return new UserResponse(result.id(), result.username(), result.name(), result.email(), result.createdAt(), result.lastLogin(), result.roles());
    }
}
