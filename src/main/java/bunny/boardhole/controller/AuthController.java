package bunny.boardhole.controller;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.auth.CurrentUserResponse;
import bunny.boardhole.dto.auth.LoginRequest;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.dto.user.UserResponse;
import bunny.boardhole.exception.UnauthorizedException;
import bunny.boardhole.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody UserCreateRequest req) {
        User user = userService.create(req);
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        User user = userService.login(req.getUsername(), req.getPassword());
        if (user == null) {
            throw new UnauthorizedException("invalid credentials");
        }
        // 세션에 사용자 정보 저장
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("user", user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new UnauthorizedException("not logged in");
        }
        return CurrentUserResponse.from(user);
    }
}

