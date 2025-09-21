package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * UserViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = UserViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class)
        }
)
@Import(ViewSecurityConfig.class) // 테스트용 보안 설정
@Tag("unit")
@Tag("view")
@DisplayName("UserViewController 뷰 테스트")
class UserViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @MockitoBean
    private UserWebMapper userWebMapper;

    @BeforeEach
    void setUp() {
        lenient().when(permissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        reset(permissionEvaluator);
    }

    @Test
    @DisplayName("관리자는 사용자 목록 페이지를 정상적으로 조회할 수 있다")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void userList_WithAdminRole_ShouldRenderUserListPage() throws Exception {
        // given
        var user1 = createUserResult(
                UUID.randomUUID(),
                "testuser1",
                "테스트 사용자1",
                "user1@example.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().minusHours(1),
                Set.of(Role.USER)
        );
        var user2 = createUserResult(
                UUID.randomUUID(),
                "testuser2",
                "테스트 사용자2",
                "user2@example.com",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(2),
                Set.of(Role.USER, Role.ADMIN)
        );

        Page<UserResult> userPage = new PageImpl<>(
                List.of(user1, user2),
                PageRequest.of(0, 10),
                2
        );

        when(userQueryService.getUsers(any())).thenReturn(userPage);

        // when & then
        mockMvc.perform(get("/users"))
               .andExpect(status().isOk())
               .andExpect(view().name("users"))
               .andExpect(model().attribute("users", userPage));
    }

    @Test
    @DisplayName("관리자 권한이 없으면 사용자 목록 페이지에 접근할 수 없다")
    @WithMockUser(authorities = {"ROLE_USER"})
    void userList_WithoutAdminRole_ShouldDenyAccess() throws Exception {
        mockMvc.perform(get("/users"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    @DisplayName("사용자 목록 페이지네이션이 정상적으로 작동한다")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void userList_WithPagination_ShouldRenderPageLinks() throws Exception {
        // given
        var users = List.of(
                createUserResult(UUID.randomUUID(), "user1", "사용자1", "user1@test.com",
                        LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), Set.of(Role.USER)),
                createUserResult(UUID.randomUUID(), "user2", "사용자2", "user2@test.com",
                        LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), Set.of(Role.USER))
        );

        Page<UserResult> pagedResults = new PageImpl<>(
                users,
                PageRequest.of(1, 10), // 2페이지
                25 // 전체 25개 (3페이지)
        );

        when(userQueryService.getUsers(any())).thenReturn(pagedResults);

        // when & then
        mockMvc.perform(get("/users").param("page", "1"))
               .andExpect(status().isOk())
               .andExpect(view().name("users"))
               .andExpect(model().attribute("users", pagedResults));
    }

    @Test
    @DisplayName("빈 사용자 목록도 정상적으로 렌더링된다")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void userList_EmptyList_ShouldRenderEmptyPage() throws Exception {
        // given
        Page<UserResult> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(userQueryService.getUsers(any())).thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/users"))
               .andExpect(status().isOk())
               .andExpect(view().name("users"))
               .andExpect(model().attribute("users", emptyPage));
    }

    // Helper methods
    private static UserResult createUserResult(UUID id, String username, String name, String email,
                                               LocalDateTime createdAt, LocalDateTime updatedAt,
                                               LocalDateTime lastLogin, Set<Role> roles) {
        return new UserResult(id, username, name, email, createdAt, updatedAt, lastLogin, roles);
    }
}
