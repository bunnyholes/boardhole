package bunny.boardhole.web;

import lombok.RequiredArgsConstructor;

import java.util.Collections;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import bunny.boardhole.board.application.query.BoardQueryService;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.application.query.UserQueryService;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final BoardQueryService boardQueryService;
    private final UserQueryService userQueryService;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        // 비인증 사용자도 기본 모델 속성을 제공하여 뷰 렌더링/단위 테스트 안정화
        if (principal == null) {
            model.addAttribute("recentBoards", Collections.emptyList());
            model.addAttribute("stats", new DashboardStats(0L, 0L, 0L, 0L));
        } else {
            // 인증된 사용자의 대시보드 데이터 로드
            var recentBoards = boardQueryService.getRecentBoards(5);
            model.addAttribute("recentBoards", recentBoards != null ? recentBoards : Collections.emptyList());

            // 통계 데이터 (간단한 버전)
            var stats = new DashboardStats(
                    defaultLong(boardQueryService.getTotalBoardCount()),
                    defaultLong(userQueryService.getActiveUserCount()),
                    defaultLong(boardQueryService.getTodayBoardCount()),
                    defaultLong(boardQueryService.getMyBoardCount(principal.user().getId()))
            );
            model.addAttribute("stats", stats);
        }
        return "index";
    }

    @GetMapping("/index")
    public String indexAlias(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        return index(principal, model);
    }

    /**
     * 대시보드 통계 데이터 DTO
     */
    public record DashboardStats(
            Long totalBoards,
            Long activeUsers,
            Long todayBoards,
            Long myBoards
    ) {
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";
    }

    @GetMapping("/boards")
    public String boards(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        var boards = boardQueryService.getBoards(search, pageable);
        model.addAttribute("boards", boards != null ? boards : Collections.emptyList());
        model.addAttribute("search", search);
        return "board/list";
    }

    @GetMapping("/boards/{id}")
    public String boardDetail(@PathVariable Long id, Model model) {
        var board = boardQueryService.getBoard(id);
        model.addAttribute("board", board != null ? board : new Object());
        return "board/detail";
    }

    @GetMapping("/boards/write")
    @PreAuthorize("isAuthenticated()")
    public String boardWrite() {
        return "board/write";
    }

    @GetMapping("/boards/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String boardEdit(@PathVariable Long id, Model model) {
        var board = boardQueryService.getBoard(id);
        model.addAttribute("board", board != null ? board : new Object());
        return "board/edit";
    }

    @GetMapping("/users")
    public String users(
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        var users = userQueryService.getUsers(pageable);
        model.addAttribute("users", users != null ? users : Collections.emptyList());
        return "user/list";
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable Long id, Model model) {
        var user = userQueryService.getUser(id);
        model.addAttribute("user", user != null ? user : new Object());
        return "user/profile";
    }

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        if (principal == null) {
            model.addAttribute("user", new Object());
        } else {
            var user = userQueryService.getUser(principal.user().getId());
            model.addAttribute("user", user != null ? user : new Object());
        }
        return "user/mypage";
    }

    private static Long defaultLong(Long val) {
        return val != null ? val : 0L;
    }
}
