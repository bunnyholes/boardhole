package bunny.boardhole.auth.domain;

import bunny.boardhole.auth.application.command.LoginCommand;
import bunny.boardhole.auth.application.command.LogoutCommand;
import bunny.boardhole.auth.application.result.AuthResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;

/**
 * 인증 서비스 도메인 인터페이스
 * 구현 세부사항과 독립적인 인증 처리 추상화
 */
public interface AuthenticationService {

    /**
     * 사용자 로그인 처리
     */
    @NonNull
    AuthResult login(@Valid @NonNull LoginCommand cmd, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response);

    /**
     * 사용자 로그아웃 처리
     */
    void logout(@Valid @NonNull LogoutCommand cmd, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response);
}