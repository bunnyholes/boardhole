package bunny.boardhole.auth.domain;

import bunny.boardhole.auth.application.command.*;
import bunny.boardhole.auth.application.result.AuthResult;
import jakarta.servlet.http.*;
import org.springframework.lang.NonNull;

/**
 * 인증 서비스 도메인 인터페이스
 * Clean Architecture의 Domain 레이어에서 인증 처리의 핵심 동작을 정의합니다.
 */
public interface AuthenticationService {

    /**
     * 사용자 로그인을 처리합니다.
     */
    @NonNull
    AuthResult login(@NonNull LoginCommand command, 
                    @NonNull HttpServletRequest request, 
                    @NonNull HttpServletResponse response);

    /**
     * 사용자 로그아웃을 처리합니다.
     */
    void logout(@NonNull LogoutCommand command, 
               @NonNull HttpServletRequest request, 
               @NonNull HttpServletResponse response);
}