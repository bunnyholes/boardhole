package bunny.boardhole.controller;

import bunny.boardhole.dto.auth.LoginRequest;
import bunny.boardhole.dto.user.UserCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bunny.boardhole.support.AbstractIntegrationTest;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/signup 성공 - 204 No Content")
    void signup_success() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("user" + unique);
        req.setPassword("pass1234");
        req.setName("New User");
        req.setEmail("user" + unique + "@example.com");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/auth/login 및 /api/auth/logout 성공 - 204 No Content")
    void login_and_logout_success() throws Exception {
        // DataInitializer creates admin/admin123
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isNoContent());
    }
}
