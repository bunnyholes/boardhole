package dev.xiyo.bunnyholes.boardhole.shared.config.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;

@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "boardhole.logging.enabled", havingValue = "true", matchIfMissing = true)
@Order(-1000)
// 보안 로깅은 높은 우선순위
@RequiredArgsConstructor
public class SecurityLoggingAspect {

    /**
     * 인증 관련 메소드 포인트컷
     * Auth 패키지의 모든 메소드를 대상으로 합니다.
     */
    @Pointcut("execution(* dev.xiyo.bunnyholes.boardhole..auth..*(..))")
    void authMethods() {
        // AOP 포인트컷 정의용 빈 메소드
    }

    /**
     * 보안 관련 메소드 포인트컷
     * Security 패키지의 모든 메소드를 대상으로 합니다.
     */
    @Pointcut("execution(* dev.xiyo.bunnyholes.boardhole..security..*(..))")
    void securityMethods() {
        // AOP 포인트컷 정의용 빈 메소드
    }

    /**
     * 권한 검증 메소드 포인트컷
     *
     * @PreAuthorize 어노테이션이 붙은 메소드를 대상으로 합니다.
     */
    @Pointcut("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    void authorizedMethods() {
        // AOP 포인트컷 정의용 빈 메소드
    }

    @Before("authMethods()")
    public static void logAuthAttempt(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();

        log.info(MessageUtils.get("log.security.auth.attempt", method, username));
    }

    @AfterReturning("authMethods()")
    public static void logAuthSuccess(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();

        log.info(MessageUtils.get("log.security.auth.success", method, username));
    }

    @AfterThrowing(value = "authMethods() || securityMethods()", throwing = "ex")
    public static void logSecurityFailure(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();
        String clientIp = MDCUtil.getClientIp();

        log.warn(MessageUtils.get("log.security.auth.failure", method, username, ex.getMessage(), clientIp));
    }

    @Before("authorizedMethods()")
    public static void logAuthorizedAccess(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();

        if (log.isDebugEnabled())
            log.debug(MessageUtils.get("log.security.authorized.access", method, username));
    }

    private static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}
