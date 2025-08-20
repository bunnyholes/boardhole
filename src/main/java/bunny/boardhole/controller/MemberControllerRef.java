package bunny.boardhole.controller;

import bunny.boardhole.service.MemberServiceRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ref")
public class MemberControllerRef {

    @Autowired
    private MemberServiceRef memberServiceRef;

    /**
     * 회원가입 (객체 방식)
     */
    @PostMapping("/join")
    public Map<String, String> join(@RequestParam String memberName, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        
        try {
            memberServiceRef.join(memberName, password);
            response.put("status", "success");
            response.put("message", "회원가입이 완료되었습니다. (객체 방식)");
            response.put("method", "object");
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 회원가입 (플랫 파라미터 방식)
     */
    @PostMapping("/joinFlat")
    public Map<String, String> joinFlat(@RequestParam String memberName, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        
        try {
            memberServiceRef.joinFlat(memberName, password);
            response.put("status", "success");
            response.put("message", "회원가입이 완료되었습니다. (플랫 방식)");
            response.put("method", "flat");
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 로그인 (쿠키 생성)
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String memberName, 
                                   @RequestParam String password,
                                   HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();
        
        if (memberServiceRef.login(memberName, password)) {
            // 로그인 성공 시 쿠키 생성
            Cookie loginCookie = new Cookie("loginUser", memberName);
            loginCookie.setMaxAge(60 * 60); // 1시간
            loginCookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능
            response.addCookie(loginCookie);
            
            result.put("status", "success");
            result.put("message", "로그인 성공");
            result.put("user", memberName);
        } else {
            result.put("status", "error");
            result.put("message", "아이디 또는 비밀번호가 틀렸습니다.");
        }
        
        return result;
    }

    /**
     * 로그아웃 (쿠키 삭제)
     */
    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();
        
        // 로그인 쿠키 삭제
        Cookie loginCookie = new Cookie("loginUser", null);
        loginCookie.setMaxAge(0); // 즉시 만료
        loginCookie.setPath("/");
        response.addCookie(loginCookie);
        
        result.put("status", "success");
        result.put("message", "로그아웃 되었습니다.");
        
        return result;
    }

    /**
     * 로그인 상태 확인
     */
    @GetMapping("/loginStatus")
    public Map<String, String> loginStatus(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        
        Cookie[] cookies = request.getCookies();
        String loginUser = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginUser".equals(cookie.getName())) {
                    loginUser = cookie.getValue();
                    break;
                }
            }
        }
        
        if (loginUser != null) {
            result.put("status", "loggedIn");
            result.put("user", loginUser);
        } else {
            result.put("status", "notLoggedIn");
        }
        
        return result;
    }

    /**
     * 전체 회원 조회
     */
    @GetMapping("/members")
    public String getMembers() {
        return memberServiceRef.getMembers();
    }
}