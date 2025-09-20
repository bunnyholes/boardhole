package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.auth.application.mapper.AuthMapper;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;

/**
 * 로그아웃 뷰 컨트롤러
 * <p>
 * 웹 브라우저에서의 로그아웃 처리를 담당합니다.
 * 세션 무효화 및 홈페이지로 리디렉트합니다.
 * </p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LogoutViewController {

    private final AuthCommandService authCommandService;
    private final AuthMapper authMapper;

    /**
     * 로그아웃 처리
     * <p>
     * 사용자 로그아웃을 처리하고 세션을 무효화한 후 홈페이지로 리디렉트합니다.
     * </p>
     */
    @GetMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public String processLogout(
            HttpServletRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        authCommandService.logout();
        log.info("로그아웃 성공: username={}", principal.user().getUsername());

        // HTTP 세션 처리
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null)
            session.invalidate();

        // 홈페이지로 리디렉트
        return "redirect:/";
    }
}