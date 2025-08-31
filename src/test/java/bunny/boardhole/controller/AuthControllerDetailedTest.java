package bunny.boardhole.controller;

import bunny.boardhole.dto.auth.LoginRequest;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@DisplayName("인증 컨트롤러 상세 통합 테스트")
class AuthControllerDetailedTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 성공 시 세션에 인증 정보가 저장되는지 검증")
    void loginSuccessStoresAuthenticationInSession() throws Exception {
        // Given - DataInitializer에서 생성된 사용자 사용
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        // When - 로그인 요청
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())  // 요청/응답 출력
                .andExpect(status().isNoContent())
                .andReturn();

        // Then - 세션 검증
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).as("Session should be created after login").isNotNull();
        
        // 로그인 후 인증된 엔드포인트에 접근하여 검증
        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 Unauthorized 반환")
    void loginWithWrongPasswordReturns401() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시 401 Unauthorized 반환")
    void loginWithNonExistentUserReturns401() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 후 인증이 필요한 엔드포인트 접근 가능")
    void authenticatedUserCanAccessProtectedEndpoint() throws Exception {
        // Given - 로그인
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // When - 인증이 필요한 /api/users/me 엔드포인트 접근
        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("로그인하지 않은 사용자가 보호된 엔드포인트 접근 시 401 반환")
    void unauthenticatedUserCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 후 세션이 무효화되고 보호된 엔드포인트 접근 불가")
    void logoutInvalidatesSessionAndDeniesAccess() throws Exception {
        // Given - 로그인
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("test123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // When - 로그아웃
        mockMvc.perform(post("/api/auth/logout")
                        .session(session))
                .andExpect(status().isNoContent());

        // Then - 같은 세션으로 보호된 엔드포인트 접근 시 401
        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원가입 후 즉시 로그인 가능")
    void canLoginImmediatelyAfterSignup() throws Exception {
        // Given - 회원가입
        String uniqueId = String.valueOf(System.currentTimeMillis());
        UserCreateRequest signupRequest = new UserCreateRequest();
        signupRequest.setUsername("newuser" + uniqueId);
        signupRequest.setPassword("newpass123");
        signupRequest.setName("New User");
        signupRequest.setEmail("newuser" + uniqueId + "@example.com");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isNoContent());

        // When - 방금 가입한 계정으로 로그인
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("newuser" + uniqueId);
        loginRequest.setPassword("newpass123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        // Then - 세션 확인
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // 로그인한 사용자 정보 확인
        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser" + uniqueId))
                .andExpect(jsonPath("$.email").value("newuser" + uniqueId + "@example.com"));
    }
}