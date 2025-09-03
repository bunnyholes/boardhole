package bunny.boardhole.auth.domain;

import bunny.boardhole.auth.application.command.*;
import bunny.boardhole.auth.application.result.AuthResult;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;

/**
 * 인증 서비스 도메인 인터페이스
 * 구현 세부사항과 독립적인 인증 처리 추상화
 */
public interface AuthenticationService {

    /**
     * 사용자 로그인 처리
     */
    AuthResult login(@Valid LoginCommand cmd, HttpServletRequest request, HttpServletResponse response);

    /**
     * 사용자 로그아웃 처리
     */
    void logout(@SuppressWarnings("unused") @Valid LogoutCommand cmd, HttpServletRequest request, HttpServletResponse response);
}