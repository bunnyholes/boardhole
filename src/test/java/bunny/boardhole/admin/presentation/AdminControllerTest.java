package bunny.boardhole.admin.presentation;

import bunny.boardhole.shared.config.TestUserConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("관리자 컨트롤러 통합 테스트")
@Import(TestUserConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUserConfig.TestUserProperties testUserProperties;

    // ========== READ: 관리자 통계 조회 테스트 ==========

    @Test
    @DisplayName("01. 관리자 통계 조회 성공")
    void test_01_admin_stats_success() throws Exception {
        // 관리자로 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.adminUsername())
                        .param("password", testUserProperties.adminPassword()))
                .andExpect(status().isNoContent())
                .andReturn();

        // 세션 ID를 쿠키에서 가져오기
        String sessionId = loginResult.getResponse().getCookie("JSESSIONID").getValue();

        // 관리자 통계 조회 - 쿠키로 세션 전달
        mockMvc.perform(get("/api/admin/stats")
                        .cookie(new MockCookie("JSESSIONID", sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.totalBoards").exists())
                .andExpect(jsonPath("$.totalViews").exists())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalBoards").isNumber())
                .andExpect(jsonPath("$.totalViews").isNumber())
                .andDo(print());
    }

    @Test
    @DisplayName("02. 관리자 통계 조회 실패 - 인증되지 않은 사용자")
    void test_02_admin_stats_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("03. 관리자 통계 조회 실패 - 일반 사용자 접근")
    void test_03_admin_stats_forbidden() throws Exception {
        // 일반 사용자로 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
                .andExpect(status().isNoContent())
                .andReturn();

        // 세션 ID를 쿠키에서 가져오기
        String sessionId = loginResult.getResponse().getCookie("JSESSIONID").getValue();

        // 일반 사용자가 관리자 통계에 접근 시도
        mockMvc.perform(get("/api/admin/stats")
                        .cookie(new MockCookie("JSESSIONID", sessionId)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
}