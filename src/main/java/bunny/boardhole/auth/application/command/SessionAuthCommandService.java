package bunny.boardhole.auth.application.command;

import bunny.boardhole.auth.application.mapper.AuthMapper;
import bunny.boardhole.auth.application.result.AuthResult;
import bunny.boardhole.auth.application.command.AuthCommandService;
import bunny.boardhole.shared.exception.UnauthorizedException;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Session-based authentication provider implementation.
 * Handles user login and logout by managing authentication state using Spring Security context.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class SessionAuthCommandService implements AuthCommandService {

    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    /**
     * 사용자 로그인 처리
     *
     * @param cmd 로그인 명령
     * @return 인증 결과
     * @throws UnauthorizedException 인증 실패 시
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResult login(@Valid LoginCommand cmd) {
        try {
            // Spring Security를 통한 인증 처리
            Authentication authRequest = new UsernamePasswordAuthenticationToken(
                    cmd.username(),
                    cmd.password()
            );

            Authentication authResult = authenticationManager.authenticate(authRequest);

            // SecurityContext에 인증 정보 저장
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authResult);
            SecurityContextHolder.setContext(context);

            // 사용자 정보 조회
            AppUserPrincipal principal = (AppUserPrincipal) authResult.getPrincipal();
            User user = principal.user();

            return authMapper.toAuthResult(user);

        } catch (BadCredentialsException e) {
            log.warn(MessageUtils.get("log.auth.login-failed", cmd.username()));
            throw new UnauthorizedException(MessageUtils.get("error.auth.invalid-credentials"));
        }
    }

    /**
     * 사용자 로그아웃 처리
     *
     * @param cmd 로그아웃 명령
     */
    @Override
    public void logout(@Valid LogoutCommand cmd) {
        // SecurityContext 정리
        SecurityContextHolder.clearContext();
    }
}