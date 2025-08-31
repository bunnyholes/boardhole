package bunny.boardhole.auth.application.command;

import bunny.boardhole.auth.application.dto.AuthResult;
import bunny.boardhole.common.exception.UnauthorizedException;
import bunny.boardhole.common.security.AppUserPrincipal;
import bunny.boardhole.common.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 인증 명령 서비스
 * CQRS 패턴의 Command 측면으로 로그인, 로그아웃 등 인증 상태 변경 작업을 담당합니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthCommandService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;
    private final MessageUtils messageUtils;

    /**
     * 사용자 로그인 처리
     *
     * @param cmd      로그인 명령
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @return 인증 결과
     * @throws UnauthorizedException 인증 실패 시
     */
    @Transactional(readOnly = true)
    public AuthResult login(@Valid LoginCommand cmd, HttpServletRequest request, HttpServletResponse response) {
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

            // 세션에 SecurityContext 저장
            securityContextRepository.saveContext(context, request, response);

            // 사용자 정보 조회
            AppUserPrincipal principal = (AppUserPrincipal) authResult.getPrincipal();
            User user = principal.user();

            log.info(messageUtils.getMessage("log.auth.login-success", user.getUsername(), user.getId()));

            return new AuthResult(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getName(),
                    user.getRoles().iterator().next().name(), // 첫 번째 Role 사용
                    true
            );

        } catch (BadCredentialsException e) {
            log.warn(messageUtils.getMessage("log.auth.login-failed", cmd.username()));
            throw new UnauthorizedException(messageUtils.getMessage("error.auth.invalid-credentials"));
        }
    }

    /**
     * 사용자 로그아웃 처리
     *
     * @param cmd      로그아웃 명령
     * @param request  HTTP 요청
     * @param response HTTP 응답
     */
    public void logout(@Valid LogoutCommand cmd, HttpServletRequest request, HttpServletResponse response) {
        // SecurityContext 정리
        SecurityContextHolder.clearContext();

        // 세션 무효화
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }

        // SecurityContext 저장소에서도 제거
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);

        log.info(messageUtils.getMessage("log.auth.logout-success", cmd.userId()));
    }
}