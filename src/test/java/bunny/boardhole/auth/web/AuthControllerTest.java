package bunny.boardhole.auth.web;

import bunny.boardhole.common.bootstrap.DataInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("인증 컨트롤러 통합 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== CREATE: 회원가입 테스트 ==========

    @Test
    @DisplayName("01. 회원가입 성공")
    void test_01_signup_success() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "new_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "New User")
                        .param("email", "newuser_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("02. 회원가입 실패 - 필수 필드 누락")
    void test_02_signup_fail_missing_field() throws Exception {
        // username 누락
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("password", "password123")
                        .param("name", "Test User"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // ========== READ: 로그인 및 세션 관리 테스트 ==========

    @Test
    @DisplayName("03. 로그인 성공")
    void test_03_login_success() throws Exception {
        // 먼저 회원가입
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "usr_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "Login User")
                        .param("email", "loginuser_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        // 로그인 시도
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "usr_" + uniqueId)
                        .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn();

        // 세션이 생성되었는지 확인 및 SecurityContext 조회
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        assertThat(session).isNotNull();
        Object ctxAttr = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(ctxAttr).as("SecurityContext should be stored in session").isNotNull();
        SecurityContext context = (SecurityContext) ctxAttr;
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("04. 로그인 실패 - 잘못된 비밀번호")
    void test_04_login_fail_wrong_password() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.ADMIN_USERNAME)
                        .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // ========== UPDATE: 권한별 접근 테스트 ==========

    @Test
    @DisplayName("05. 공개 엔드포인트 접근 - 인증 없이 접근 가능")
    void test_05_public_access_without_auth() throws Exception {
        mockMvc.perform(get("/api/auth/public-access"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("06. 사용자 전용 엔드포인트 - 인증 없이 접근 실패")
    void test_06_user_access_without_auth() throws Exception {
        mockMvc.perform(get("/api/auth/user-access"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("07. 관리자 전용 엔드포인트 - 일반 사용자 접근 실패")
    void test_07_admin_access_with_user_role() throws Exception {
        // 일반 사용자로 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 관리자 엔드포인트 접근 시도
        mockMvc.perform(get("/api/auth/admin-only")
                        .session(session))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("08. 관리자 전용 엔드포인트 - 관리자 접근 성공")
    void test_08_admin_access_with_admin_role() throws Exception {
        // 관리자로 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.ADMIN_USERNAME)
                        .param("password", DataInitializer.ADMIN_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 관리자 엔드포인트 접근
        mockMvc.perform(get("/api/auth/admin-only")
                        .session(session))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    // ========== DELETE: 로그아웃 테스트 ==========

    @Test
    @DisplayName("09. 로그아웃 성공")
    void test_09_logout_success() throws Exception {
        // 먼저 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 로그아웃
        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isNoContent())
                .andDo(print());

        // 로그아웃 후 인증이 필요한 엔드포인트에 접근 실패
        mockMvc.perform(get("/api/auth/user-access")
                        .session(session))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("10. 로그아웃 실패 - 인증되지 않은 상태")
    void test_10_logout_without_auth() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("11. 회원가입 실패 - 중복 사용자명 (409 Conflict)")
    void test_11_signup_fail_duplicate_username() throws Exception {
        // 첫 번째 회원가입
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String duplicateUsername = "dup_" + uniqueId;
        
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", duplicateUsername)
                        .param("password", "password123")
                        .param("name", "First User")
                        .param("email", "first_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        // 동일한 사용자명으로 두 번째 회원가입 시도
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", duplicateUsername)
                        .param("password", "password123")
                        .param("name", "Second User")
                        .param("email", "second_" + uniqueId + "@example.com"))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("12. 회원가입 실패 - 중복 이메일 (409 Conflict)")
    void test_12_signup_fail_duplicate_email() throws Exception {
        // 첫 번째 회원가입
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String duplicateEmail = "duplicate_" + uniqueId + "@example.com";
        
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "first_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "First User")
                        .param("email", duplicateEmail))
                .andExpect(status().isNoContent());

        // 동일한 이메일로 두 번째 회원가입 시도
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "second_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "Second User")
                        .param("email", duplicateEmail))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("13. 로그인 실패 - 존재하지 않는 사용자")
    void test_13_login_fail_nonexistent_user() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "nonexistent_" + uniqueId)
                        .param("password", "password123"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("14. 회원가입 실패 - 잘못된 이메일 형식")
    void test_14_signup_fail_invalid_email() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "invalid_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "Invalid Email User")
                        .param("email", "invalid-email-format"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}
