package dev.xiyo.bunnyholes.boardhole.shared.presentation.view;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(
        value = IndexViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class)
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

    @Nested
    @DisplayName("홈페이지 기본 렌더링")
    class HomePage {

        @Test
        @DisplayName("페이지가 정상적으로 렌더링된다")
        @WithAnonymousUser
        void shouldRenderSuccessfully() throws Exception {
            mockMvc.perform(get("/"))
                   .andExpect(status().isOk())
                   .andExpect(view().name("index"));
        }

        @Test
        @DisplayName("페이지 타이틀 'boardhole'가 표시된다")
        @WithAnonymousUser
        void shouldDisplayPageTitle() throws Exception {
            mockMvc.perform(get("/"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(containsString("boardhole")));
        }
    }

    @Nested
    @DisplayName("레이아웃 구조")
    class LayoutStructure {

        @Nested
        @DisplayName("사이드바")
        @WithAnonymousUser
        class Sidebar {

            @Test
            @DisplayName("사이드바가 렌더링된다")
            void shouldRenderSidebar() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("id=\"sidebar\"")));
            }

            @Test
            @DisplayName("사이드바 토글 버튼이 표시된다")
            void shouldDisplaySidebarToggle() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("popovertarget=\"sidebar\"")));
            }

            @Test
            @DisplayName("프로필 링크가 표시된다")
            void shouldDisplayProfileLink() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("aria-label=\"Profile\"")))
                       .andExpect(content().string(containsString("lucide-user-round")));
            }

            @Test
            @DisplayName("알림 버튼이 표시된다")
            void shouldDisplayNotificationButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("aria-label=\"알림\"")));
            }
        }

        @Nested
        @DisplayName("메인 헤더")
        @WithAnonymousUser
        class MainHeader {

            @Test
            @DisplayName("BOARDHOLE 타이틀이 표시된다")
            void shouldDisplayBoardholeTitle() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("BOARDHOLE")));
            }

            @Test
            @DisplayName("GitHub 링크가 올바른 속성을 가진다")
            void shouldHaveCorrectGitHubLink() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("href=\"https://github.com/bunnyholes/boardhole\"")))
                       .andExpect(content().string(containsString("target=\"_blank\"")))
                       .andExpect(content().string(containsString("rel=\"noreferrer noopener\"")));
            }
        }

        @Nested
        @DisplayName("네비게이션")
        @WithAnonymousUser
        class Navigation {

            @Test
            @DisplayName("게시판 링크가 표시된다")
            void shouldDisplayBoardLink() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("href=\"/boards\"")))
                       .andExpect(content().string(containsString("aria-label=\"Boards\"")));
            }

            @Test
            @DisplayName("프로필 링크가 표시된다")
            void shouldDisplayProfileLink() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("href=\"/users/me\"")))
                       .andExpect(content().string(containsString("aria-label=\"Profile\"")));
            }
        }
    }

    @Nested
    @DisplayName("메인 콘텐츠 영역")
    class MainContent {

        @Nested
        @DisplayName("히어로 섹션")
        @WithAnonymousUser
        class HeroSection {

            @Test
            @DisplayName("버전 배지가 표시된다")
            void shouldDisplayVersionBadge() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("boardhole v")));
            }

            @Test
            @DisplayName("메인 타이틀이 표시된다")
            void shouldDisplayMainTitle() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("Spring Boot 게시판")))
                       .andExpect(content().string(containsString("학습 프로젝트")));
            }

            @Test
            @DisplayName("설명 문구가 표시된다")
            void shouldDisplayDescription() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("Spring Boot와 최신 웹 기술을 활용한 게시판 학습 프로젝트입니다.")));
            }

            @Test
            @DisplayName("게시판 둘러보기 버튼이 표시된다")
            void shouldDisplayBrowseBoardsButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("게시판 둘러보기")));
            }
        }

        @Nested
        @DisplayName("기능 카드")
        @WithAnonymousUser
        class FeatureCards {

            @Test
            @DisplayName("인증 시스템 카드가 표시된다")
            void shouldDisplayAuthCard() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("인증 시스템")))
                       .andExpect(content().string(containsString("Spring Security 폼 기반 로그인")))
                       .andExpect(content().string(containsString("Redis 세션 스토어")));
            }

            @Test
            @DisplayName("게시판 기능 카드가 표시된다")
            void shouldDisplayBoardCard() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("게시판 기능")))
                       .andExpect(content().string(containsString("게시글 CRUD")))
                       .andExpect(content().string(containsString("페이지네이션, 검색")));
            }

            @Test
            @DisplayName("회원가입 버튼이 포함된다")
            void shouldIncludeSignupButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("회원가입")));
            }
        }

        @Nested
        @DisplayName("주요 기능 섹션")
        @WithAnonymousUser
        class MainFeatures {

            @Test
            @DisplayName("검증 & 예외처리 카드가 표시된다")
            void shouldDisplayValidationCard() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("검증 & 예외처리")))
                       .andExpect(content().string(containsString("커스텀 검증 어노테이션")));
            }

            @Test
            @DisplayName("개발 환경 카드가 표시된다")
            void shouldDisplayDevEnvironmentCard() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("개발 환경")))
                       .andExpect(content().string(containsString("Docker Compose")));
            }

            @Test
            @DisplayName("아키텍처 섹션이 표시된다")
            void shouldDisplayArchitectureSection() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("계층형 아키텍처 구조")))
                       .andExpect(content().string(containsString("DDD 기반")));
            }
        }

        @Nested
        @DisplayName("기술 스택")
        @WithAnonymousUser
        class TechStack {

            @Test
            @DisplayName("Tech Stack 섹션이 표시된다")
            void shouldDisplayTechStackSection() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("Tech Stack")));
            }

            @Test
            @DisplayName("Core Backend 기술이 표시된다")
            void shouldDisplayCoreBackendTech() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("Core Backend")))
                       .andExpect(content().string(containsString("Java")))
                       .andExpect(content().string(containsString("Spring Boot")));
            }

            @Test
            @DisplayName("Data Storage 기술이 표시된다")
            void shouldDisplayDataStorageTech() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("Data Storage")))
                       .andExpect(content().string(containsString("PostgreSQL")))
                       .andExpect(content().string(containsString("Redis")));
            }
        }
    }

    @Nested
    @DisplayName("인증 상태별 UI")
    class AuthenticationBasedUI {

        @Nested
        @DisplayName("비로그인 상태")
        @WithAnonymousUser
        class AnonymousUser {

            @Test
            @DisplayName("회원가입 버튼이 표시된다")
            void shouldShowSignupButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("회원가입")))
                       .andExpect(content().string(containsString("href=\"/auth/signup\"")));
            }
        }

        @Nested
        @DisplayName("로그인 상태")
        class AuthenticatedUser {

            @Test
            @DisplayName("프로필 링크가 표시된다")
            @WithMockUser(username = "testuser")
            void shouldShowProfileLink() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("href=\"/users/me\"")));
            }
        }
    }

    @Nested
    @DisplayName("접근성 (Accessibility)")
    class Accessibility {

        @Nested
        @DisplayName("ARIA 속성")
        @WithAnonymousUser
        class AriaAttributes {

            @Test
            @DisplayName("인터랙티브 요소가 aria-label을 가진다")
            void shouldHaveAriaLabels() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("aria-label=\"Profile\"")))
                       .andExpect(content().string(containsString("aria-label=\"Boards\"")))
                       .andExpect(content().string(containsString("aria-label=\"알림\"")));
            }
        }

        @Nested
        @DisplayName("시맨틱 HTML")
        @WithAnonymousUser
        class SemanticHtml {

            @Test
            @DisplayName("SVG 아이콘이 aria-hidden='true'를 가진다")
            void shouldHideDecorativeSvgs() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("<svg aria-hidden=\"true\"")));
            }
        }
    }

    @Nested
    @DisplayName("에러 페이지")
    class ErrorPages {

        @Nested
        @DisplayName("401 Unauthorized")
        class UnauthorizedError {

            @Test
            @DisplayName("에러 페이지가 렌더링된다")
            @WithAnonymousUser
            void shouldRenderErrorPage() throws Exception {
                mockMvc.perform(get("/error/401"))
                       .andExpect(status().isOk())
                       .andExpect(view().name("error/401"));
            }

            @Test
            @DisplayName("리다이렉트 파라미터가 처리된다")
            @WithAnonymousUser
            void shouldHandleRedirectParameter() throws Exception {
                final var redirectUrl = "/boards";

                mockMvc.perform(get("/error/401").param("redirect", redirectUrl))
                       .andExpect(status().isOk())
                       .andExpect(view().name("error/401"))
                       .andExpect(model().attribute("redirectUrl", redirectUrl));
            }

            @Test
            @DisplayName("빈 리다이렉트는 무시된다")
            @WithAnonymousUser
            void shouldIgnoreEmptyRedirect() throws Exception {
                mockMvc.perform(get("/error/401").param("redirect", ""))
                       .andExpect(status().isOk())
                       .andExpect(view().name("error/401"))
                       .andExpect(model().attributeDoesNotExist("redirectUrl"));
            }
        }
    }
}
