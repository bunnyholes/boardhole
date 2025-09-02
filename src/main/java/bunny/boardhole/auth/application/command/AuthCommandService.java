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
    
    /** 로그 새니타이징용 정규식 패턴 */
    private static final String LOG_SANITIZE_PATTERN = "[\r\n]";
    
    /** 로그 새니타이징시 교체 문자 */
    private static final String LOG_REPLACEMENT_CHAR = "_";
    
    /** null 값에 대한 로그 출력 문자열 */
    private static final String NULL_LOG_VALUE = "null";

    /** 스프링 시큐리티 인증 관리자 */
    private final AuthenticationManager authManager;
    
    /** 보안 컨텍스트 저장소 */
    private final SecurityContextRepository contextRepository;
    
    /** 사용자 정보 레포지토리 */
    private final UserRepository userRepository;
    
    /** 메시지 유틸리티 */
    private final MessageUtils messageUtils;

    /**
     * 사용자 로그인 처리
     * @param cmd 로그인 명령
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 인증 결과
     * @throws UnauthorizedException 인증 실패 시
     */
    @Transactional(readOnly = true)
    @NonNull
    public AuthResult login(@Valid @NonNull final LoginCommand cmd, @NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) {
        try {
            // Spring Security를 통한 인증 처리
            final Authentication authRequest = new UsernamePasswordAuthenticationToken(
                    cmd.username(),
                    cmd.password()
            );

            final Authentication authResult = authManager.authenticate(authRequest);

            // SecurityContext에 인증 정보 저장
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authResult);
            SecurityContextHolder.setContext(context);

            // SecurityContextRepository를 통한 저장 (Spring Security 통합)
            contextRepository.saveContext(context, request, response);

            // 사용자 정보 조회
            final AppUserPrincipal principal = (AppUserPrincipal) authResult.getPrincipal();
            final User user = principal.user();

            // CRLF 인젝션 방지 - 사용자 입력값 새니타이징
            final String sanitizedUsername = sanitizeForLog(user.getUsername());
            final Long userId = user.getId();
            
            if (log.isInfoEnabled()) {
                log.info(messageUtils.getMessage("log.auth.login-success", sanitizedUsername, userId));
            }

            return new AuthResult(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getName(),
                    user.getRoles().iterator().next().name(), // 첫 번째 Role 사용
                    true
            );

        } catch (final BadCredentialsException ex) {
            final String sanitizedUsername = sanitizeForLog(cmd.username());
            if (log.isWarnEnabled()) {
                log.warn(messageUtils.getMessage("log.auth.login-failed", sanitizedUsername));
            }
            throw new UnauthorizedException(messageUtils.getMessage("error.auth.invalid-credentials"), ex);
        }
    }

    /**
     * 사용자 로귳아웃 처리
     *
     * @param cmd      로귳아웃 명령
     * @param request  HTTP 요청
     * @param response HTTP 응답
     */
    public void logout(@Valid @NonNull final LogoutCommand cmd, @NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response) {
        // SecurityContext 정리
        SecurityContextHolder.clearContext();

        // 세션 무효화 - Optional을 사용한 null 체크 제거
        Optional.ofNullable(request.getSession(false))
                .ifPresent(HttpSession::invalidate);

        // SecurityContext 저장소에서도 제거
        contextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.auth.logout-success", cmd.userId()));
        }
    }
    
    /**
     * 로그 출력용 문자열 새니타이징
     * CRLF 인젝션 공격을 방지하기 위해 개행 문자를 제거합니다.
     *
     * @param input 새니타이징할 입력 문자열
     * @return 새니타이징된 문자열
     */
    private String sanitizeForLog(final String input) {
        return (input == null) ? NULL_LOG_VALUE : input.replaceAll(LOG_SANITIZE_PATTERN, LOG_REPLACEMENT_CHAR);
    }
}