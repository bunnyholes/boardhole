package dev.xiyo.bunnyholes.boardhole.auth.application;

import jakarta.validation.Valid;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.LoginCommand;
import dev.xiyo.bunnyholes.boardhole.auth.application.command.LogoutCommand;
import dev.xiyo.bunnyholes.boardhole.auth.application.result.AuthResult;

/**
 * 인증 서비스 도메인 인터페이스
 * 구현 세부사항과 독립적인 인증 처리 추상화
 */
public interface AuthCommandService {

    /**
     * 사용자 로그인 처리
     */
    AuthResult login(@Valid LoginCommand cmd);

    /**
     * 사용자 로그아웃 처리
     */
    void logout(@Valid LogoutCommand cmd);
}
