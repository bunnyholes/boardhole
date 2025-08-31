package bunny.boardhole.controller;

import bunny.boardhole.config.DataInitializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("인증 컨트롤러 통합 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    // ========== CREATE: 회원가입 테스트 ==========
    
    @Test
    @Order(1)
    @DisplayName("01. 회원가입 성공")
    void test_01_signup_success() throws Exception {
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000); // 4자리로 제한

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "new" + uniqueId)
                .param("password", "password123")
                .param("name", "New User")
                .param("email", "newuser" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @Order(2)
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
    @Order(3)
    @DisplayName("03. 로그인 성공")
    void test_03_login_success() throws Exception {
        // 먼저 회원가입
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000); // 4자리로 제한

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "usr" + uniqueId)
                .param("password", "password123")
                .param("name", "Login User")
                .param("email", "loginuser" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        // 로그인 시도
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "usr" + uniqueId)
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
    @Order(4)
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
    @Order(5)
    @DisplayName("05. 공개 엔드포인트 접근 - 인증 없이 접근 가능")
    void test_05_public_access_without_auth() throws Exception {
        mockMvc.perform(get("/api/auth/public-access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Public endpoint - accessible to everyone"))
                .andDo(print());
    }

    @Test
    @Order(6)
    @DisplayName("06. 사용자 전용 엔드포인트 - 인증 없이 접근 실패")
    void test_06_user_access_without_auth() throws Exception {
        mockMvc.perform(get("/api/auth/user-access"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @Order(7)
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
    @Order(8)
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin access granted"))
                .andExpect(jsonPath("$.username").value(DataInitializer.ADMIN_USERNAME))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andDo(print());
    }

    // ========== DELETE: 로그아웃 테스트 ==========

    @Test
    @Order(9)
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
    @Order(10)
    @DisplayName("10. 로그아웃 실패 - 인증되지 않은 상태")
    void test_10_logout_without_auth() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
