package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * LoginViewController Thymeleaf 뷰 테스트
 *
 * @WebMvcTest를 활용해 로그인 관련 페이지의 렌더링을 검증한다.
 */
@WebMvcTest(
        value = LoginViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class)
@Tag("unit")
@Tag("view")
@DisplayName("LoginViewController 뷰 테스트")
class LoginViewControllerTest {

    private static final String LOGIN_URL = "/auth/login";
    private static final String LOGOUT_SUCCESS_URL = "/auth/logout/success";
    private static final String LOGIN_VIEW = "auth/login";
    private static final String LOGOUT_VIEW = "auth/logout-success";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String LOGOUT_ATTRIBUTE = "logout";
    private static final String SESSION_TIMEOUT_ATTRIBUTE = "session-timeout";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("익명 사용자는 로그인 페이지를 볼 수 있다")
    @WithAnonymousUser
    void loginPage_Anonymous_ShouldRenderLoginView() throws Exception {
        mockMvc.perform(get(LOGIN_URL))
               .andExpect(status().isOk())
               .andExpect(view().name(LOGIN_VIEW))
               .andExpect(model().attributeDoesNotExist(
                       ERROR_ATTRIBUTE,
                       LOGOUT_ATTRIBUTE,
                       SESSION_TIMEOUT_ATTRIBUTE
               ));
    }

    @Test
    @DisplayName("인증된 사용자도 로그인 페이지를 볼 수 있다")
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void loginPage_Authenticated_ShouldRenderLoginView() throws Exception {
        mockMvc.perform(get(LOGIN_URL))
               .andExpect(status().isOk())
               .andExpect(view().name(LOGIN_VIEW))
               .andExpect(model().attributeDoesNotExist(
                       ERROR_ATTRIBUTE,
                       LOGOUT_ATTRIBUTE,
                       SESSION_TIMEOUT_ATTRIBUTE
               ));
    }

    @Test
    @DisplayName("에러 파라미터가 전달되면 에러 메시지가 모델에 포함된다")
    @WithAnonymousUser
    void loginPage_WithErrorParam_ShouldExposeErrorAttribute() throws Exception {
        mockMvc.perform(get(LOGIN_URL).param(ERROR_ATTRIBUTE, "true"))
               .andExpect(status().isOk())
               .andExpect(view().name(LOGIN_VIEW))
               .andExpect(model().attributeExists(ERROR_ATTRIBUTE))
               .andExpect(model().attribute(ERROR_ATTRIBUTE, true));
    }

    @Test
    @DisplayName("로그아웃 파라미터가 전달되면 로그아웃 메시지가 모델에 포함된다")
    @WithAnonymousUser
    void loginPage_WithLogoutParam_ShouldExposeLogoutAttribute() throws Exception {
        mockMvc.perform(get(LOGIN_URL).param(LOGOUT_ATTRIBUTE, "true"))
               .andExpect(status().isOk())
               .andExpect(view().name(LOGIN_VIEW))
               .andExpect(model().attributeExists(LOGOUT_ATTRIBUTE))
               .andExpect(model().attribute(LOGOUT_ATTRIBUTE, true));
    }

    @Test
    @DisplayName("세션 만료 파라미터가 전달되면 안내 메시지가 모델에 포함된다")
    @WithAnonymousUser
    void loginPage_WithSessionTimeoutParam_ShouldExposeSessionTimeoutAttribute() throws Exception {
        mockMvc.perform(get(LOGIN_URL).param(SESSION_TIMEOUT_ATTRIBUTE, "true"))
               .andExpect(status().isOk())
               .andExpect(view().name(LOGIN_VIEW))
               .andExpect(model().attributeExists(SESSION_TIMEOUT_ATTRIBUTE))
               .andExpect(model().attribute(SESSION_TIMEOUT_ATTRIBUTE, true));
    }

    @Test
    @DisplayName("로그아웃 성공 페이지는 정상적으로 렌더링된다")
    @WithAnonymousUser
    void logoutSuccess_ShouldRenderLogoutView() throws Exception {
        mockMvc.perform(get(LOGOUT_SUCCESS_URL))
               .andExpect(status().isOk())
               .andExpect(view().name(LOGOUT_VIEW));
    }
}
