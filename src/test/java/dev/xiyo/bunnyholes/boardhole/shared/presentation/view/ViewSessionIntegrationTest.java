package dev.xiyo.bunnyholes.boardhole.shared.presentation.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:boardhole-view-session;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("뷰 세션 생성 동작 통합 테스트")
@Tag("view")
class ViewSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("홈 페이지 접근만으로는 세션이 생성되지 않는다")
    void homePageDoesNotCreateSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).as("홈은 익명 사용자를 위한 공개 페이지입니다").isNull();
    }

    @Test
    @DisplayName("로그인 페이지는 시큐리티 컨텍스트 초기화를 위해 세션을 생성한다")
    void loginPageCreatesSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).as("로그인 페이지는 인증 처리에 필요한 세션을 초기화합니다").isNotNull();
    }

    @Test
    @DisplayName("정적 리소스 접근 시 세션이 생성되지 않는다")
    void staticResourceDoesNotCreateSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getRequest().getSession(false)).as("정적 리소스는 세션 없이 제공되어야 합니다").isNull();
    }
}
