package dev.xiyo.bunnyholes.boardhole.web.view;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * IndexViewController 뷰 컨트롤러 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 정적 페이지이므로 서비스 계층 모킹 불필요
 */
@WebMvcTest(
        value = IndexViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class) // 테스트용 보안 설정
@Tag("unit")
@Tag("view")
@DisplayName("IndexViewController 뷰 테스트")
class IndexViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("홈페이지가 정상적으로 렌더링된다")
    @WithAnonymousUser
    void index_ShouldRenderHomePage() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"))
               .andExpect(content().string(containsString("boardholes")));
    }

    @Test
    @DisplayName("401 에러 페이지가 정상적으로 렌더링된다")
    @WithAnonymousUser
    void unauthorized_ShouldRenderErrorPage() throws Exception {
        // when & then
        mockMvc.perform(get("/error/401"))
               .andExpect(status().isOk())
               .andExpect(view().name("error/401"));
    }

    @Test
    @DisplayName("401 에러 페이지에서 리다이렉트 URL이 모델에 추가된다")
    @WithAnonymousUser
    void unauthorized_WithRedirectParam_ShouldAddRedirectUrlToModel() throws Exception {
        // given
        final var redirectUrl = "/boards";

        // when & then
        mockMvc.perform(get("/error/401").param("redirect", redirectUrl))
               .andExpect(status().isOk())
               .andExpect(view().name("error/401"))
               .andExpect(model().attribute("redirectUrl", redirectUrl));
    }

    @Test
    @DisplayName("401 에러 페이지에서 빈 리다이렉트 URL은 모델에 추가되지 않는다")
    @WithAnonymousUser
    void unauthorized_WithEmptyRedirectParam_ShouldNotAddRedirectUrlToModel() throws Exception {
        // when & then
        mockMvc.perform(get("/error/401").param("redirect", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("error/401"))
               .andExpect(model().attributeDoesNotExist("redirectUrl"));
    }
}