package bunny.boardhole.auth.application.command;

import bunny.boardhole.auth.domain.AuthenticationService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthCommandService {

    private final AuthenticationService authProvider;

    public void login(@Valid LoginCommand cmd, HttpServletRequest request, HttpServletResponse response) {
        authProvider.login(cmd, request, response);
    }

    public void logout(@Valid LogoutCommand cmd, HttpServletRequest request, HttpServletResponse response) {
        authProvider.logout(cmd, request, response);
    }
}