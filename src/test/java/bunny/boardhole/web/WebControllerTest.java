package bunny.boardhole.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import bunny.boardhole.board.application.query.BoardQueryService;
import bunny.boardhole.user.application.query.UserQueryService;
import bunny.boardhole.shared.config.SecurityConfig;
import bunny.boardhole.shared.properties.ProblemProperties;
import bunny.boardhole.shared.properties.PropertiesConfiguration;
import org.springframework.security.access.PermissionEvaluator;

import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = WebController.class, properties = {"boardhole.logging.enabled=false", "spring.thymeleaf.enabled=false"})
@Import({SecurityConfig.class, PropertiesConfiguration.class})
@DisplayName("WebController 단위 테스트")
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardQueryService boardQueryService;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @TestConfiguration
    static class TestPropsConfig {
        @Bean
        ProblemProperties problemProperties() {
            return new ProblemProperties("");
        }
    }

    @Nested
    @DisplayName("인증 없이 접근 가능한 페이지")
    class PublicPages {

        @Test
        @DisplayName("루트 경로(/) - 비인증 사용자는 로그인으로 리다이렉트")
        void indexPage_Unauthenticated_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("로그인 페이지(/login) 접근 테스트")
        void loginPage_ShouldReturnLoginView() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"));
        }

        @Test
        @DisplayName("회원가입 페이지(/signup) 접근 테스트")
        void signupPage_ShouldReturnSignupView() throws Exception {
            mockMvc.perform(get("/signup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/signup"));
        }

        @Test
        @DisplayName("게시판 목록(/boards) - 비인증 사용자는 로그인으로 리다이렉트")
        void boardsPage_Unauthenticated_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/boards"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("인증이 필요한 페이지")
    class AuthenticatedPages {

        @Test
        @DisplayName("마이페이지(/mypage) - 인증 없이 접근 시 리다이렉트")
        void mypagePage_WithoutAuth_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/mypage"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("마이페이지(/mypage) - 인증된 사용자 접근")
        void mypagePage_WithAuth_ShouldReturnMypageView() throws Exception {
            mockMvc.perform(get("/mypage"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user/mypage"))
                    .andExpect(model().attributeExists("user"));
        }

        @Test
        @DisplayName("게시글 작성(/boards/write) - 인증 없이 접근 시 리다이렉트")
        void boardWritePage_WithoutAuth_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/boards/write"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("게시글 작성(/boards/write) - 인증된 사용자 접근")
        void boardWritePage_WithAuth_ShouldReturnWriteView() throws Exception {
            mockMvc.perform(get("/boards/write"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("board/write"));
        }

        @Test
        @DisplayName("사용자 관리(/users) - 인증 없이 접근 시 리다이렉트")
        void usersPage_WithoutAuth_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/users"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("사용자 관리(/users) - 인증된 사용자 접근")
        void usersPage_WithAuth_ShouldReturnUserListView() throws Exception {
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("user/list"))
                    .andExpect(model().attributeExists("users"));
        }
    }

    @Nested
    @DisplayName("게시판 상세 페이지")
    class BoardDetailPages {

        @Test
        @DisplayName("게시글 상세(/boards/{id}) - 비인증 사용자는 로그인으로 리다이렉트")
        void boardDetailPage_Unauthenticated_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/boards/1"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("사용자 프로필(/users/{id}) - 비인증 사용자는 로그인으로 리다이렉트")
        void userProfilePage_Unauthenticated_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/users/1"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @DisplayName("게시글 수정(/boards/{id}/edit) - 인증 없이 접근 시 리다이렉트")
        void boardEditPage_WithoutAuth_ShouldRedirect() throws Exception {
            mockMvc.perform(get("/boards/1/edit"))
                    .andExpect(status().is3xxRedirection());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("게시글 수정(/boards/{id}/edit) - 인증된 사용자 접근")
        void boardEditPage_WithAuth_ShouldReturnEditView() throws Exception {
            mockMvc.perform(get("/boards/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("board/edit"))
                    .andExpect(model().attributeExists("board"));
        }
    }
}
