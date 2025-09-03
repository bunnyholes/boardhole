package bunny.boardhole.user.presentation;

import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("사용자 API 통합 테스트")
@Tag("integration")
@Tag("user")
class UserControllerTest extends ControllerTestBase {

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    @Tag("read")
    class ListUsers {

        @Test
        @DisplayName("✅ 관리자 - 목록 조회 성공")
        @WithUserDetails("admin")
        void shouldAllowAdminToListUsers() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists())
                    .andExpect(jsonPath("$.pageable").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("🔍 관리자 - 사용자 검색")
        @WithUserDetails("admin")
        void shouldAllowAdminToSearchUsers() throws Exception {
            mockMvc.perform(get("/api/users")
                            .param("search", "test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("📄 관리자 - 페이지네이션 적용")
        @WithUserDetails("admin")
        void shouldApplyPaginationForAdmin() throws Exception {
            mockMvc.perform(get("/api/users")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").exists())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} - 사용자 단일 조회")
    @Tag("read")
    class GetUser {

        @Test
        @DisplayName("✅ 일반 사용자 - 본인 조회 성공")
        @WithUserDetails
        void shouldAllowUserToGetOwnInfo() throws Exception {
            Long userId = findUserIdByUsername(testUserProperties.regularUsername());
            mockMvc.perform(get("/api/users/" + userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.username").value(testUserProperties.regularUsername()))
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 조회 → 404 Not Found")
        @WithUserDetails("admin")
        void shouldReturn404WhenUserNotFound() throws Exception {
            mockMvc.perform(get("/api/users/999999"))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id} - 사용자 정보 수정")
    @Tag("update")
    class UpdateUser {

        @Test
        @DisplayName("✅ 본인 정보 수정 성공")
        @WithUserDetails
        void shouldAllowUserToUpdateOwnInfo() throws Exception {
            Long userId = findUserIdByUsername(testUserProperties.regularUsername());
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            mockMvc.perform(put("/api/users/" + userId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", "Updated Name")
                            .param("email", "updated_" + uniqueId + "@example.com"))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", "Hacked Name")
                            .param("email", "hacked@example.com"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 수정 → 403 Forbidden")
        @WithUserDetails
        void shouldReturn403WhenUserNotFound() throws Exception {
            mockMvc.perform(put("/api/users/999999")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", "New Name")
                            .param("email", "new@example.com"))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id} - 사용자 삭제")
    @Tag("delete")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DeleteUser {

        @Test
        @DisplayName("✅ 본인 삭제 성공")
        void shouldAllowUserToDeleteOwnAccount() throws Exception {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String username = "delete_" + uniqueId;
            Long userId = seedUser(username, "To Delete", "delete_" + uniqueId + "@example.com", "plain", Set.of(bunny.boardhole.user.domain.Role.USER));
            var principal = new AppUserPrincipal(userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + username)));
            var adminPrincipal = new AppUserPrincipal(userRepository.findByUsername(testUserProperties.adminUsername())
                    .orElseThrow(() -> new IllegalStateException("Admin user not found")));

            mockMvc.perform(delete("/api/users/" + userId).with(user(principal)))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            // 삭제 확인
            mockMvc.perform(get("/api/users/" + userId).with(user(adminPrincipal)))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 삭제 → 403 Forbidden")
        @WithUserDetails
        void shouldReturn403WhenUserNotFound() throws Exception {
            mockMvc.perform(delete("/api/users/999999"))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Nested
        @DisplayName("권한별 접근 제어")
        class AccessControl {

            @Test
            @DisplayName("❌ 다른 일반 사용자가 삭제 시도 → 403 Forbidden")
            void shouldDenyOtherUserToDelete() throws Exception {
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                Long userId = seedUser("del_" + uniqueId, "Delete Test User", "del_" + uniqueId + "@example.com", "plain", Set.of(bunny.boardhole.user.domain.Role.USER));
                var otherPrincipal = new AppUserPrincipal(userRepository.findByUsername(testUserProperties.regularUsername())
                        .orElseThrow(() -> new IllegalStateException("Regular user not found")));

                mockMvc.perform(delete("/api/users/" + userId).with(user(otherPrincipal)))
                        .andExpect(status().isForbidden())
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 현재 사용자 정보")
    @Tag("profile")
    class CurrentUser {

        @Test
        @DisplayName("✅ 현재 로그인한 사용자 정보 조회")
        @WithUserDetails
        void shouldGetCurrentUserInfo() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(testUserProperties.regularUsername()))
                    .andExpect(jsonPath("$.roles").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}