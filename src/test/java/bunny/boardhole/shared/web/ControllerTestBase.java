package bunny.boardhole.shared.web;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.config.TestUserConfig;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestUserConfig.class)
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestUserConfig.TestUserProperties testUserProperties;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected BoardRepository boardRepository;

    protected MockHttpSession loginAsAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.adminUsername())
                        .param("password", testUserProperties.adminPassword()))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }

    protected MockHttpSession loginAsUser() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }

    protected MockHttpSession loginAsCustomUser(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }

    protected void testPublicEndpoint(String url, String method) throws Exception {
        // 익명 사용자
        performRequest(url, method, null)
                .andExpect(status().isOk())
                .andDo(print());

        // 일반 사용자
        MockHttpSession userSession = loginAsUser();
        performRequest(url, method, userSession)
                .andExpect(status().isOk())
                .andDo(print());

        // 관리자
        MockHttpSession adminSession = loginAsAdmin();
        performRequest(url, method, adminSession)
                .andExpect(status().isOk())
                .andDo(print());
    }

    protected void testAuthRequired(String url, String method) throws Exception {
        // 익명 사용자 - 실패
        performRequest(url, method, null)
                .andExpect(status().isUnauthorized())
                .andDo(print());

        // 일반 사용자 - 성공
        MockHttpSession userSession = loginAsUser();
        performRequest(url, method, userSession)
                .andExpect(status().isOk())
                .andDo(print());

        // 관리자 - 성공
        MockHttpSession adminSession = loginAsAdmin();
        performRequest(url, method, adminSession)
                .andExpect(status().isOk())
                .andDo(print());
    }

    protected void testAdminOnly(String url, String method) throws Exception {
        // 익명 사용자 - 실패 (401)
        performRequest(url, method, null)
                .andExpect(status().isUnauthorized())
                .andDo(print());

        // 일반 사용자 - 실패 (403)
        MockHttpSession userSession = loginAsUser();
        performRequest(url, method, userSession)
                .andExpect(status().isForbidden())
                .andDo(print());

        // 관리자 - 성공
        MockHttpSession adminSession = loginAsAdmin();
        performRequest(url, method, adminSession)
                .andExpect(status().isOk())
                .andDo(print());
    }

    protected void testUserOrAdminRole(String url, String method) throws Exception {
        // 익명 사용자 - 실패 (401)
        performRequest(url, method, null)
                .andExpect(status().isUnauthorized())
                .andDo(print());

        // 일반 사용자 - 성공
        MockHttpSession userSession = loginAsUser();
        performRequest(url, method, userSession)
                .andExpect(status().isOk())
                .andDo(print());

        // 관리자 - 성공
        MockHttpSession adminSession = loginAsAdmin();
        performRequest(url, method, adminSession)
                .andExpect(status().isOk())
                .andDo(print());
    }

    private ResultActions performRequest(String url, String method, MockHttpSession session) throws Exception {
        return switch (method.toUpperCase()) {
            case "GET" -> session == null ? mockMvc.perform(get(url)) : mockMvc.perform(get(url).session(session));
            case "POST" -> session == null ? mockMvc.perform(post(url)) : mockMvc.perform(post(url).session(session));
            case "PUT" -> session == null ? mockMvc.perform(put(url)) : mockMvc.perform(put(url).session(session));
            case "DELETE" ->
                    session == null ? mockMvc.perform(delete(url)) : mockMvc.perform(delete(url).session(session));
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }

    protected Long seedBoardOwnedBy(String username, String title, String content) {
        User owner = userRepository.findByUsername(username);
        Board board = Board.builder()
                .title(title)
                .content(content)
                .author(owner)
                .build();
        return boardRepository.save(board).getId();
    }

    protected Long seedUser(String username, String name, String email, String rawPassword, java.util.Set<Role> roles) {
        User existing = userRepository.findByUsername(username);
        if (existing != null) return existing.getId();
        User user = User.builder()
                .username(username)
                .password(rawPassword) // tests should not authenticate with this; used for data only
                .name(name)
                .email(email)
                .roles(new java.util.HashSet<>(roles))
                .build();
        return userRepository.save(user).getId();
    }

    protected Long findUserIdByUsername(String username) {
        User u = userRepository.findByUsername(username);
        return u != null ? u.getId() : null;
    }
}
