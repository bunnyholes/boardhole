package bunny.boardhole.admin.presentation;

import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("관리자 API 통합 테스트")
@Tag("integration")
@Tag("admin")
class AdminControllerTest extends ControllerTestBase {

    @Nested
    @DisplayName("GET /api/admin/stats - 관리자 통계 조회")
    @Tag("stats")
    class AdminStats {

        @Test
        @DisplayName("✅ 관리자 - 통계 조회 성공")
        @WithUserDetails("admin")
        void shouldAllowAdminToGetStats() throws Exception {
            mockMvc.perform(get("/api/admin/stats"))
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
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/stats"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 일반 사용자 → 403 Forbidden")
        @WithUserDetails
        void shouldReturn403WhenRegularUser() throws Exception {
            mockMvc.perform(get("/api/admin/stats"))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }
}