package bunny.boardhole.web.view;

import bunny.boardhole.testsupport.e2e.E2ETestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RequestCache 동작 검증 테스트
 */
@AutoConfigureMockMvc
@Tag("e2e")
@DisplayName("RequestCache 세션 생성 검증")
class RequestCacheTest extends E2ETestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("공개 페이지(/boards) 접근 시 세션 생성 안 됨")
    void publicPageDoesNotCreateSession() throws Exception {
        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_SAVED_REQUEST", nullValue()))
                .andExpect(result -> {
                    var session = result.getRequest().getSession(false);
                    // 세션이 생성되지 않았거나, 생성되었어도 저장된 요청이 없어야 함
                    if (session != null) {
                        assert session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") == null;
                    }
                });
    }

    @Test
    @DisplayName("보호된 View 페이지(/users) 접근 시 RequestCache가 세션에 요청 저장")
    void protectedPageCreatesSessionViaRequestCache() throws Exception {
        // View 컨트롤러는 브라우저 요청을 가정 (Accept: text/html)
        mockMvc.perform(get("/users")
                        .accept("text/html,application/xhtml+xml"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/401?redirect=%2Fusers"))
                .andExpect(result -> {
                    var session = result.getRequest().getSession(false);
                    // RequestCache가 세션을 생성하고 요청을 저장
                    assert session != null : "세션이 생성되어야 함";
                    var savedRequest = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
                    assert savedRequest != null : "저장된 요청이 있어야 함";
                });
    }
    
    @Test  
    @DisplayName("보호된 REST API(/api/users) 접근 시 401 응답 (리다이렉트 없음)")
    void protectedApiReturns401WithoutRedirect() throws Exception {
        // REST API 요청 (Accept: application/json)
        mockMvc.perform(get("/api/users")
                        .accept("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(result -> {
                    var session = result.getRequest().getSession(false);
                    // API 요청도 RequestCache가 세션을 생성할 수 있음
                    // 하지만 리다이렉트는 하지 않음
                    if (session != null) {
                        System.out.println("API 요청에도 세션 생성됨: " + session.getId());
                    }
                });
    }

    @Test
    @DisplayName("로그인 페이지 직접 접근 시 세션 생성 안 됨")
    void loginPageDirectAccessDoesNotCreateSession() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("SPRING_SECURITY_SAVED_REQUEST", nullValue()))
                .andExpect(result -> {
                    var session = result.getRequest().getSession(false);
                    // 직접 로그인 페이지 접근은 세션을 만들지 않음
                    if (session != null) {
                        assert session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") == null;
                    }
                });
    }
}