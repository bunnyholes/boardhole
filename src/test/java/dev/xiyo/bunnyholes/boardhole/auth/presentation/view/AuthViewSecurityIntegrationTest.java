package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

import java.util.Set;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:boardhole-auth-view;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("뷰 보안 흐름 통합 테스트")
@Tag("view")
class AuthViewSecurityIntegrationTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureAdminUser() {
        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            User admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .name("관리자")
                    .email("admin@example.com")
                    .roles(Set.of(Role.ADMIN))
                    .build();
            admin.verifyEmail();
            userRepository.save(admin);
        }
    }

    @Nested
    @DisplayName("로그인 페이지 렌더링")
    class LoginPage {

        @Test
        @DisplayName("로그인 폼과 안내 문구가 노출된다")
        void shouldRenderLoginPage() throws Exception {
            mockMvc.perform(get("/auth/login"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("로그인")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("boardholes")));
        }
    }

    @Nested
    @DisplayName("로그인 흐름")
    class LoginFlow {

        @Test
        @DisplayName("성공 시 /boards 로 리디렉션하고 세션을 생성한다")
        void shouldRedirectToBoardsAfterSuccessfulLogin() throws Exception {
            MvcResult result = mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .param("username", ADMIN_USERNAME)
                            .param("password", ADMIN_PASSWORD))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/boards"))
                    .andReturn();

            HttpSession session = result.getRequest().getSession(false);
            assertThat(session).as("로그인 성공 시 세션이 생성되어야 한다").isNotNull();
            assertThat(session.getAttributeNames().hasMoreElements()).isTrue();
        }

        @Test
        @DisplayName("실패 시 로그인 페이지로 돌아가 오류 메시지를 노출한다")
        void shouldShowErrorWhenCredentialsAreInvalid() throws Exception {
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/auth/login")
                            .session(session)
                            .with(csrf())
                            .param("username", ADMIN_USERNAME)
                            .param("password", "WrongPassword!"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/auth/login?error"));

            mockMvc.perform(get("/auth/login")
                            .session(session)
                            .param("error", ""))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("잘못된 사용자명 또는 비밀번호입니다.")));
        }

        @Test
        @DisplayName("보호된 페이지 접근 후 로그인하면 기본 페이지로 이동한다")
        void shouldReturnToOriginalUrlAfterLoginFromProtectedPage() throws Exception {
            MvcResult protectedResult = mockMvc.perform(get("/boards/write"))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            MockHttpSession session = (MockHttpSession) protectedResult.getRequest().getSession(false);
            assertThat(session).as("RequestCache 저장을 위해 세션이 생성되어야 한다").isNotNull();

            assertThat(protectedResult.getResponse().getRedirectedUrl())
                    .as("로그인은 /auth/login 으로 리디렉션되어야 합니다")
                    .endsWith("/auth/login");

            MvcResult loginResult = mockMvc.perform(post("/auth/login")
                            .session(session)
                            .with(csrf())
                            .param("username", ADMIN_USERNAME)
                            .param("password", ADMIN_PASSWORD))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            assertThat(loginResult.getResponse().getRedirectedUrl())
                    .as("로그인 성공 후 기본 페이지로 이동해야 합니다")
                    .endsWith("/boards");
        }
    }

    @Nested
    @DisplayName("접근 제어")
    class AccessControl {

        @Test
        @DisplayName("공개 페이지는 인증 없이 접근할 수 있다")
        void shouldAllowAccessToPublicPages() throws Exception {
            mockMvc.perform(get("/boards"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("사용자 페이지는 로그인 페이지로 리디렉션된다")
        void shouldRedirectProtectedPageToLogin() throws Exception {
            MvcResult result = mockMvc.perform(get("/users"))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

            assertThat(result.getResponse().getRedirectedUrl())
                    .as("보호된 페이지 접근 시 로그인 페이지로 이동해야 합니다")
                    .endsWith("/auth/login");
        }
    }
}
