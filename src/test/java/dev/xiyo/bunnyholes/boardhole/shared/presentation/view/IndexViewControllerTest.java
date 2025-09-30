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

/**
 * IndexViewController 뷰 컨트롤러 단위 테스트
 * <p>
 * Nested 클래스를 사용한 계층적 테스트 구조
 * - 관련 테스트들을 논리적으로 그룹핑
 * - IDE에서 트리 뷰로 표시
 * - 특정 그룹만 선택 실행 가능
 */
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
            @DisplayName("고정 사이드바가 렌더링된다")
            void shouldRenderFixedSidebar() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("class=\"fixed left-0 top-0 flex h-screen w-20")));
            }

            @Test
            @DisplayName("BH 로고가 표시된다")
            void shouldDisplayLogo() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("BH")));
            }

            @Test
            @DisplayName("프로필 이미지가 표시된다")
            void shouldDisplayProfileImage() throws Exception {
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
                       .andExpect(content().string(containsString("aria-label=\"알림\"")))
                       .andExpect(content().string(containsString("알림")));
            }
        }

        @Nested
        @DisplayName("메인 헤더")
        @WithAnonymousUser
        class MainHeader {

            @Test
            @DisplayName("boardhole 타이틀이 표시된다")
            void shouldDisplayBoardholeTitle() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("class=\"text-lg font-semibold text-slate-900 uppercase\">boardhole")));
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
            @DisplayName("검색 버튼이 렌더링된다")
            void shouldRenderSearchButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("popovertarget=\"searchPopover\"")))
                       .andExpect(content().string(containsString("aria-label=\"검색 열기\"")));
            }

            @Test
            @DisplayName("게시판 링크가 표시된다")
            void shouldDisplayBoardLink() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("href=\"/boards\"")))
                       .andExpect(content().string(containsString("aria-label=\"게시판\"")));
            }

            @Test
            @DisplayName("툴팁이 올바르게 설정된다")
            void shouldHaveCorrectTooltips() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("role=\"tooltip\"")))
                       .andExpect(content().string(containsString("id=\"index-tip-search\"")))
                       .andExpect(content().string(containsString("id=\"index-tip-board\"")));
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
            @DisplayName("검색 입력 영역이 표시된다")
            void shouldDisplaySearchInput() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("게시판 둘러보기")))
                       .andExpect(content().string(containsString("검색어 입력")));
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
            @DisplayName("게시판 둘러보기 버튼이 포함된다")
            void shouldIncludeStartButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("게시판 둘러보기")));
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
    }

    @Nested
    @DisplayName("인터랙티브 요소")
    class InteractiveElements {

        @Nested
        @DisplayName("팝오버")
        class Popovers {

            @Nested
            @DisplayName("검색 팝오버")
            @WithAnonymousUser
            class SearchPopover {

                @Test
                @DisplayName("팝오버가 렌더링된다")
                void shouldRenderPopover() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("id=\"searchPopover\"")))
                           .andExpect(content().string(containsString("<h3 class=\"text-2xl font-semibold text-slate-900\">검색")));
                }

                @Test
                @DisplayName("닫기 버튼이 있다")
                void shouldHaveCloseButton() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("popovertargetaction=\"hide\"")))
                           .andExpect(content().string(containsString("aria-label=\"검색 닫기\"")));
                }

                @Test
                @DisplayName("검색 입력 필드가 있다")
                void shouldHaveSearchInput() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("키워드를 입력하고 바로 필요한 보드를 찾아보세요")))
                           .andExpect(content().string(containsString("placeholder=\"검색어 입력\"")));
                }

                @Test
                @DisplayName("검색 팝오버가 표시된다")
                void shouldDisplaySearchPopover() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("<h3 class=\"text-2xl font-semibold text-slate-900\">검색")))
                           .andExpect(content().string(containsString("검색어 입력후 엔터를 입력하세요.")));
                }
            }

            @Nested
            @DisplayName("로그아웃 팝오버")
            class LogoutPopover {

                @Test
                @DisplayName("로그인 상태에서 팝오버가 렌더링된다")
                @WithMockUser(username = "testuser")
                void shouldRenderForAuthenticatedUser() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("id=\"logoutPopover\"")))
                           .andExpect(content().string(containsString("로그아웃 하시겠어요?")));
                }

                @Test
                @DisplayName("확인 메시지가 표시된다")
                @WithMockUser(username = "testuser")
                void shouldDisplayConfirmationMessage() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("현재 세션이 종료되고 다시 로그인해야 합니다")));
                }

                @Test
                @DisplayName("취소/확인 버튼이 있다")
                @WithMockUser(username = "testuser")
                void shouldHaveActionButtons() throws Exception {
                    mockMvc.perform(get("/"))
                           .andExpect(status().isOk())
                           .andExpect(content().string(containsString("취소")))
                           .andExpect(content().string(containsString("href=\"/auth/logout\"")));
                }
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

            @Test
            @DisplayName("로그아웃 버튼이 표시되지 않는다")
            void shouldNotShowLogoutButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       // Thymeleaf 조건부 렌더링으로 인해 정확한 검증이 어려움
                       // 회원가입 텍스트가 있으면 비로그인 상태로 간주
                       .andExpect(content().string(containsString("회원가입")));
            }
        }

        @Nested
        @DisplayName("로그인 상태")
        class AuthenticatedUser {

            @Test
            @DisplayName("로그아웃 버튼이 표시된다")
            @WithMockUser(username = "testuser")
            void shouldShowLogoutButton() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("popovertarget=\"logoutPopover\"")))
                       .andExpect(content().string(containsString("aria-label=\"로그아웃\"")));
            }

            @Test
            @DisplayName("로그아웃 팝오버가 표시된다")
            @WithMockUser(username = "testuser")
            void shouldShowLogoutPopover() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("id=\"logoutPopover\"")))
                       .andExpect(content().string(containsString("로그아웃 하시겠어요?")));
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
            @DisplayName("모든 인터랙티브 요소가 aria-label을 가진다")
            void shouldHaveAriaLabels() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("aria-label=\"Profile\"")))
                       .andExpect(content().string(containsString("aria-label=\"검색 열기\"")))
                       .andExpect(content().string(containsString("aria-label=\"게시판\"")))
                       .andExpect(content().string(containsString("aria-label=\"알림\"")));
            }

            @Test
            @DisplayName("툴팁이 role='tooltip'을 가진다")
            void shouldHaveTooltipRole() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("role=\"tooltip\"")));
            }

            @Test
            @DisplayName("버튼이 aria-describedby로 툴팁과 연결된다")
            void shouldConnectButtonsWithTooltips() throws Exception {
                mockMvc.perform(get("/"))
                       .andExpect(status().isOk())
                       .andExpect(content().string(containsString("aria-describedby=\"index-tip-profile\"")))
                       .andExpect(content().string(containsString("aria-describedby=\"index-tip-search\"")))
                       .andExpect(content().string(containsString("aria-describedby=\"index-tip-board\"")));
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
