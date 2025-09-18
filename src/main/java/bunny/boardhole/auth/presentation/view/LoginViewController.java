package bunny.boardhole.auth.presentation.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 기본 로그인/로그아웃 뷰 매핑만 제공한다.
 * 로그인 처리와 오류 노출은 Spring Security 기본 기능에 위임한다.
 */
@Controller
public class LoginViewController {

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/logout/success")
    public String logoutSuccess() {
        return "auth/logout-success";
    }
}
