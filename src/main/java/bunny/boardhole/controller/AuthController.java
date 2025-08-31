package bunny.boardhole.controller;

import bunny.boardhole.dto.auth.LoginRequest;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.security.AppUserPrincipal;
import bunny.boardhole.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    public void signup(@Validated @ModelAttribute UserCreateRequest req) {
        // Just create user - no automatic login
        userService.create(req);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    public void login(@Validated @ModelAttribute LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        try {
            // Authenticate through Spring Security pipeline
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            
            // Store authentication using SecurityContextRepository (creates session if required)
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            
            // Update domain logic
            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            userService.updateLastLogin(principal.getUser().getId());
        } catch (BadCredentialsException e) {
            throw new bunny.boardhole.exception.UnauthorizedException("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void logout(HttpServletRequest request) {
        // Clear security context and invalidate session
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // 관리자 전용 엔드포인트
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminOnly(@AuthenticationPrincipal AppUserPrincipal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access granted");
        response.put("username", principal.getUsername());
        response.put("role", "ADMIN");
        return response;
    }

    // 일반 사용자도 접근 가능한 엔드포인트 (USER, ADMIN)
    @GetMapping("/user-access")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> userAccess(@AuthenticationPrincipal AppUserPrincipal principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User access granted");
        response.put("username", principal.getUsername());
        response.put("roles", principal.getUser().getRoles());
        return response;
    }

    // 익명 사용자도 접근 가능한 엔드포인트 (공개)
    @GetMapping("/public-access")
    @PermitAll
    public Map<String, Object> publicAccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Public endpoint - accessible to everyone");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

}
