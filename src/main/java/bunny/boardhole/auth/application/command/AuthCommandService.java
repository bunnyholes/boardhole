package bunny.boardhole.auth.application.command;

import bunny.boardhole.auth.application.result.AuthResult;
import bunny.boardhole.shared.exception.UnauthorizedException;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

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
    @NonNull
    public AuthResult login(@Valid @NonNull LoginCommand cmd, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
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

            // SecurityContextRepository를 통한 저장 (Spring Security 통합)
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
     * 사용자 로귳아웃 처리
     *
     * @param cmd      로귳아웃 명령
     * @param request  HTTP 요청
     * @param response HTTP 응답
     */
    public void logout(@Valid @NonNull LogoutCommand cmd, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
        // SecurityContext 정리
        SecurityContextHolder.clearContext();

        // 세션 무효화 - Optional을 사용한 null 체크 제거
        Optional.ofNullable(request.getSession(false))
                .ifPresent(HttpSession::invalidate);

        // SecurityContext 저장소에서도 제거
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);

        log.info(messageUtils.getMessage("log.auth.logout-success", cmd.userId()));
    }
}