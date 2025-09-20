package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 기본 로그인/로그아웃 뷰 매핑만 제공한다.
 * 로그인 처리와 오류 노출은 Spring Security 기본 기능에 위임한다.
 */
@Controller
public class LoginViewController {

    @GetMapping("/auth/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "session-timeout", required = false, name = "session-timeout") String sessionTimeout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        if (sessionTimeout != null) {
            model.addAttribute("session-timeout", true);
        }
        return "auth/login";
    }

    @GetMapping("/auth/logout/success")
    public String logoutSuccess() {
        return "auth/logout-success";
    }
}
