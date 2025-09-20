package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import jakarta.persistence.EntityManager;

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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ViewControllerAdvice;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * LoginViewController 테스트
 * <p>
 * 단위 테스트 및 Spring MVC 통합 테스트를 포함
 */
@DisplayName("LoginViewController 뷰 테스트")
class LoginViewControllerTest {

    @Nested
    @Tag("unit")
    @DisplayName("단위 테스트")
    class UnitTests {
        private final LoginViewController controller = new LoginViewController();

        @Test
        @DisplayName("로그인 페이지 매핑이 정상 동작한다")
        void loginPage_ShouldReturnCorrectView() {
            // when
            String viewName = controller.loginPage();

            // then
            assertEquals("auth/login", viewName);
        }

        @Test
        @DisplayName("로그아웃 성공 페이지 매핑이 정상 동작한다")
        void logoutSuccess_ShouldReturnCorrectView() {
            // when
            String viewName = controller.logoutSuccess();

            // then
            assertEquals("auth/logout-success", viewName);
        }
    }

    @Nested
    @WebMvcTest(
            value = LoginViewController.class,
            excludeFilters = {
                    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
            }
    )
    @Import({ViewSecurityConfig.class, ViewControllerAdvice.class})
    @Tag("unit")
    @Tag("view")
    @DisplayName("통합 테스트")
    class IntegrationTests {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private EntityManager entityManager;

        @MockitoBean
        private JpaMetamodelMappingContext jpaMetamodelMappingContext;

        @MockitoBean
        private PermissionEvaluator permissionEvaluator;

        @Test
        @DisplayName("익명 사용자가 로그인 페이지에 접근할 수 있다")
        @WithAnonymousUser
        void loginPage_AnonymousUser_ShouldAccessSuccessfully() throws Exception {
            mockMvc.perform(get("/auth/login"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/login"))
                   .andExpect(content().string(containsString("로그인")));
        }

        @Test
        @DisplayName("인증된 사용자가 로그인 페이지에 접근해도 정상 표시된다")
        @WithMockUser
        void loginPage_AuthenticatedUser_ShouldAccessSuccessfully() throws Exception {
            mockMvc.perform(get("/auth/login"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/login"));
        }

        @Test
        @DisplayName("로그아웃 성공 페이지에 접근할 수 있다")
        @WithAnonymousUser
        void logoutSuccess_ShouldAccessSuccessfully() throws Exception {
            mockMvc.perform(get("/auth/logout/success"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/logout-success"))
                   .andExpect(content().string(containsString("로그아웃")));
        }

        @Test
        @DisplayName("로그인 페이지에 에러 파라미터가 있을 때 에러 메시지가 표시된다")
        @WithAnonymousUser
        void loginPage_WithErrorParam_ShouldShowErrorMessage() throws Exception {
            mockMvc.perform(get("/auth/login").param("error", "true"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/login"))
                   .andExpect(model().attributeExists("error"));
        }

        @Test
        @DisplayName("로그인 페이지에 로그아웃 파라미터가 있을 때 로그아웃 메시지가 표시된다")
        @WithAnonymousUser
        void loginPage_WithLogoutParam_ShouldShowLogoutMessage() throws Exception {
            mockMvc.perform(get("/auth/login").param("logout", "true"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/login"))
                   .andExpect(model().attributeExists("logout"));
        }

        @Test
        @DisplayName("로그인 페이지 URL에 대소문자 구분이 적용된다")
        @WithAnonymousUser
        void loginPage_CaseSensitiveUrl_ShouldReturn404() throws Exception {
            mockMvc.perform(get("/Auth/Login"))
                   .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("로그인 페이지에 POST 요청은 허용되지 않는다")
        @WithAnonymousUser
        void loginPage_PostRequest_ShouldReturn405() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login"))
                   .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("로그아웃 성공 페이지에 쿼리 파라미터가 있어도 정상 동작한다")
        @WithAnonymousUser
        void logoutSuccess_WithQueryParams_ShouldIgnoreAndSuccess() throws Exception {
            mockMvc.perform(get("/auth/logout/success")
                           .param("unknown", "value")
                           .param("test", "123"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/logout-success"));
        }

        @Test
        @DisplayName("잘못된 경로로 로그인 페이지 접근 시 404 응답")
        @WithAnonymousUser
        void loginPage_WrongPath_ShouldReturn404() throws Exception {
            mockMvc.perform(get("/auth/login/"))  // 마지막에 슬래시 추가
                   .andExpect(status().isNotFound());

            mockMvc.perform(get("/auth//login"))  // 이중 슬래시
                   .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("로그인 페이지 헤더에 따라 다른 컨텐츠 타입 반환")
        @WithAnonymousUser
        void loginPage_WithAcceptHeader_ShouldReturnCorrectContentType() throws Exception {
            mockMvc.perform(get("/auth/login")
                           .accept("text/html"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentTypeCompatibleWith("text/html"));
        }

        @Test
        @DisplayName("특수 문자가 포함된 쿼리 파라미터 처리")
        @WithAnonymousUser
        void loginPage_WithSpecialCharactersInQuery_ShouldHandleCorrectly() throws Exception {
            mockMvc.perform(get("/auth/login")
                           .param("redirect", "/boards?id=123&sort=desc")
                           .param("message", "로그인이 필요합니다!"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/login"));
        }

        @Test
        @DisplayName("세션 타임아웃 파라미터가 있을 때 적절한 메시지 표시")
        @WithAnonymousUser
        void loginPage_WithSessionTimeoutParam_ShouldShowTimeoutMessage() throws Exception {
            mockMvc.perform(get("/auth/login")
                           .param("session-timeout", "true"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("auth/login"))
                   .andExpect(model().attributeExists("session-timeout"));
        }
    }
}