package bunny.boardhole.controller;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.auth.LoginRequest;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import bunny.boardhole.security.AppUserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signup(@Valid @RequestBody UserCreateRequest req) {
        // Just create user - no automatic login
        userService.create(req);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(@Valid @RequestBody LoginRequest req) {
        try {
            // Authenticate through Spring Security pipeline
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            
            // Store in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Update domain logic
            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            userService.updateLastLogin(principal.getUser().getId());
        } catch (BadCredentialsException e) {
            throw new bunny.boardhole.exception.UnauthorizedException("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        // Clear security context and invalidate session
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
