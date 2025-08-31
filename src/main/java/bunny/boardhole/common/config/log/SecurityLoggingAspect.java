package bunny.boardhole.common.config.log;

import bunny.boardhole.common.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(-1000) // 보안 로깅은 높은 우선순위
@RequiredArgsConstructor
public class SecurityLoggingAspect {

    // 인증 관련 메소드
    @Pointcut("execution(* bunny.boardhole..auth..*(..))")
    void authMethods() {}
    
    // 보안 관련 예외
    @Pointcut("execution(* bunny.boardhole..security..*(..))")
    void securityMethods() {}
    
    // 관리자 전용 기능
    @Pointcut("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    void authorizedMethods() {}
    
    @Before("authMethods()")
    public void logAuthAttempt(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();
        
        log.info(MessageUtils.getMessageStatic("log.security.auth.attempt", method, username));
    }
    
    @AfterReturning("authMethods()")
    public void logAuthSuccess(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();
        
        log.info(MessageUtils.getMessageStatic("log.security.auth.success", method, username));
    }
    
    @AfterThrowing(value = "authMethods() || securityMethods()", throwing = "ex")
    public void logSecurityFailure(JoinPoint joinPoint, Throwable ex) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();
        String clientIp = MDCUtil.getClientIp();
        
        log.warn(MessageUtils.getMessageStatic("log.security.auth.failure", 
            method, ex.getMessage(), clientIp));
    }
    
    @Before("authorizedMethods()")
    public void logAuthorizedAccess(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().toShortString();
        String username = getCurrentUsername();
        
        if (log.isDebugEnabled()) {
            log.debug(MessageUtils.getMessageStatic("log.security.authorized.access", method, username));
        }
    }
    
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}