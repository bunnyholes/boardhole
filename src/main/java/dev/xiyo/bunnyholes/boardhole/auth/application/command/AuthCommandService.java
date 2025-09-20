package dev.xiyo.bunnyholes.boardhole.auth.application.command;

import java.util.UUID;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

/**
 * 세션 기반 인증 처리 서비스.
 * 로그인 인증 → 보안 컨텍스트 저장 → 결과 매핑 흐름을 단일 클래스로 캡슐화한다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthCommandService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;
    private final UserCommandService userCommandService;

    @Transactional(readOnly = true)
    public void login(@Valid LoginCommand cmd) {
        try {
            AppUserPrincipal principal = authenticate(cmd);
            storeAuthentication(principal);
            userCommandService.updateLastLogin(principal.user().getId());
        } catch (BadCredentialsException e) {
            log.warn(MessageUtils.get("log.auth.login-failed", cmd.username()));
            throw new UnauthorizedException(MessageUtils.get("error.auth.invalid-credentials"));
        }
    }

    @Transactional(readOnly = true)
    public void login(UUID userId) {
        AppUserPrincipal principal = loadPrincipal(userId);
        storeAuthentication(principal);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        persistContext(SecurityContextHolder.createEmptyContext());
    }

    private AppUserPrincipal authenticate(LoginCommand cmd) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(cmd.username(), cmd.password());
        Authentication authResult = authenticationManager.authenticate(authRequest);
        return (AppUserPrincipal) authResult.getPrincipal();
    }

    private AppUserPrincipal loadPrincipal(UUID userId) {
        return userRepository.findById(userId)
                             .map(AppUserPrincipal::new)
                             .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + userId));
    }

    private void storeAuthentication(AppUserPrincipal principal) {
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        persistContext(context);
    }

    private void persistContext(SecurityContext context) {
        if (securityContextRepository == null)
            return;
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null)
            return;
        HttpServletRequest request = attrs.getRequest();
        HttpServletResponse response = attrs.getResponse();
        if (request != null && response != null)
            securityContextRepository.saveContext(context, request, response);
    }
}

