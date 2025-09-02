package bunny.boardhole.auth.application.command;

import bunny.boardhole.auth.application.result.AuthResult;
import bunny.boardhole.auth.infrastructure.SessionAuthenticationProvider;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class AuthCommandService {

    private final SessionAuthenticationProvider authProvider;

    public AuthResult login(@Valid @NonNull LoginCommand cmd, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        return authProvider.login(cmd, request, response);
    }

    public void logout(@Valid @NonNull LogoutCommand cmd, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        authProvider.logout(cmd, request, response);
    }
}