package bunny.boardhole.shared.config.log;

import bunny.boardhole.shared.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 보안 관련 로깅을 담당하는 AOP Aspect 클래스입니다.
 * 
 * <p>Spring Security와 인증/인가 관련 메소드의 실행을 감시하여
 * 보안 이벤트를 체계적으로 로깅합니다. CRLF 인젝션 공격을 방지하고
 * 민감한 정보 노출을 방지하는 보안 로깅 정책을 적용합니다.</p>
 * 
 * <p><strong>로깅 대상 메소드:</strong></p>
 * <ul>
 *   <li><strong>인증 메소드:</strong> {@code bunny.boardhole..auth..*()} 패턴</li>
 *   <li><strong>보안 메소드:</strong> {@code bunny.boardhole..security..*()} 패턴</li>
 *   <li><strong>권한 검사:</strong> {@code @PreAuthorize} 어노테이션이 적용된 메소드</li>
 * </ul>
 * 
 * <p><strong>보안 고려사항:</strong></p>
 * <ul>
 *   <li><strong>CRLF 인젝션 방지:</strong> 모든 로그 메시지는 개행문자 제거 처리</li>
 *   <li><strong>민감정보 보호:</strong> 비밀번호, 토큰 등 민감 정보는 마스킹 처리</li>
 *   <li><strong>로그 가드:</strong> DEBUG 레벨 로그는 성능을 위해 사전 검사</li>
 *   <li><strong>높은 우선순위:</strong> {@code @Order(-1000)}으로 다른 Aspect보다 우선 실행</li>
 * </ul>
 * 
 * <p><strong>로깅 정책:</strong></p>
 * <ul>
 *   <li>인증 시도 → INFO 레벨</li>
 *   <li>인증 성공 → INFO 레벨</li>
 *   <li>보안 실패 → WARN 레벨 (클라이언트 IP 포함)</li>
 *   <li>권한 확인 → DEBUG 레벨</li>
 * </ul>
 * 
 * @author Security Team
 * @version 1.0
 * @since 1.0
 * 
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @see bunny.boardhole.shared.util.MessageUtils
 * @see bunny.boardhole.shared.config.log.MDCUtil
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "boardhole.logging.enabled", havingValue = "true", matchIfMissing = true)
@Order(-1000) // 보안 로깅은 높은 우선순위
@RequiredArgsConstructor
public class SecurityLoggingAspect {

    /** 국제화 메시지 처리를 위한 유틸리티 (CRLF 인젝션 방지 포함) */
    private final MessageUtils messageUtils;

    /**
     * 인증 관련 메소드를 대상으로 하는 포인트컷입니다.
     * 
     * <p>{@code bunny.boardhole} 패키지 하위의 {@code auth} 패키지에 있는
     * 모든 클래스의 모든 메소드를 대상으로 합니다.</p>
     * 
     * <p><strong>대상 메소드 예시:</strong></p>
     * <ul>
     *   <li>AuthController의 로그인/로그아웃 메소드</li>
     *   <li>AuthService의 인증 처리 메소드</li>
     *   <li>JWT 토큰 관련 처리 메소드</li>
     * </ul>
     */
    @Pointcut("execution(* bunny.boardhole..auth..*(..))")
    void authMethods() {
    }

    /**
     * 보안 관련 메소드를 대상으로 하는 포인트컷입니다.
     * 
     * <p>{@code bunny.boardhole} 패키지 하위의 {@code security} 패키지에 있는
     * 모든 클래스의 모든 메소드를 대상으로 합니다.</p>
     * 
     * <p><strong>대상 메소드 예시:</strong></p>
     * <ul>
     *   <li>AppPermissionEvaluator의 권한 평가 메소드</li>
     *   <li>ProblemDetails Handler의 보안 예외 처리 메소드</li>
     *   <li>CurrentUserArgumentResolver의 사용자 정보 추출 메소드</li>
     * </ul>
     */
    @Pointcut("execution(* bunny.boardhole..security..*(..))")
    void securityMethods() {
    }

    /**
     * {@link org.springframework.security.access.prepost.PreAuthorize} 어노테이션이 적용된
     * 권한 검사 메소드를 대상으로 하는 포인트컷입니다.
     * 
     * <p>메소드 실행 전에 권한을 확인하는 모든 보안 어노테이션 적용 메소드를 포함합니다.</p>
     * 
     * <p><strong>대상 메소드 예시:</strong></p>
     * <ul>
     *   <li>관리자 전용 기능 메소드</li>
     *   <li>사용자 본인만 접근 가능한 메소드</li>
     *   <li>특정 역할 권한이 필요한 메소드</li>
     * </ul>
     */
    @Pointcut("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    void authorizedMethods() {
    }

    /**
     * 인증 메소드 실행 전에 인증 시도를 로깅합니다.
     * 
     * <p>인증 관련 메소드가 호출될 때마다 시도 정보를 기록하여
     * 보안 모니터링 및 감사 추적을 지원합니다.</p>
     * 
     * <p><strong>로깅 정보:</strong></p>
     * <ul>
     *   <li>실행 메소드명 (CRLF 인젝션 방지 처리됨)</li>
     *   <li>현재 사용자명 (익명 사용자의 경우 "anonymous")</li>
     * </ul>
     * 
     * @param joinPoint AOP 조인포인트 - 실행되는 메소드 정보 포함
     */
    @Before("authMethods()")
    public void logAuthAttempt(final JoinPoint joinPoint) {
        final String method = sanitizeLogInput(joinPoint.getSignature().toShortString());
        final String username = sanitizeLogInput(getCurrentUsername());

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.security.auth.attempt", method, username));
        }
    }

    /**
     * 인증 메소드 성공 실행 후에 인증 성공을 로깅합니다.
     * 
     * <p>인증이 성공적으로 완료된 후 성공 정보를 기록하여
     * 보안 감사 및 사용자 활동 추적을 지원합니다.</p>
     * 
     * <p><strong>로깅 정보:</strong></p>
     * <ul>
     *   <li>성공한 메소드명 (CRLF 인젝션 방지 처리됨)</li>
     *   <li>인증된 사용자명 (CRLF 인젝션 방지 처리됨)</li>
     * </ul>
     * 
     * @param joinPoint AOP 조인포인트 - 성공적으로 실행된 메소드 정보 포함
     */
    @AfterReturning("authMethods()")
    public void logAuthSuccess(final JoinPoint joinPoint) {
        final String method = sanitizeLogInput(joinPoint.getSignature().toShortString());
        final String username = sanitizeLogInput(getCurrentUsername());

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.security.auth.success", method, username));
        }
    }

    /**
     * 인증 또는 보안 메소드에서 예외 발생 시 보안 실패를 로깅합니다.
     * 
     * <p>보안 관련 메소드에서 예외가 발생했을 때 상세한 실패 정보를 기록하여
     * 보안 침해 시도 감지 및 문제 해결을 지원합니다.</p>
     * 
     * <p><strong>보안 로깅 정보:</strong></p>
     * <ul>
     *   <li>실패한 메소드명 (CRLF 인젝션 방지 처리됨)</li>
     *   <li>예외 메시지 (CRLF 인젝션 방지 처리됨)</li>
     *   <li>클라이언트 IP 주소 (공격 추적용)</li>
     *   <li>사용자명 (실패 시점의 인증 상태)</li>
     * </ul>
     * 
     * <p><strong>보안 고려사항:</strong></p>
     * <ul>
     *   <li>민감한 예외 정보는 마스킹 처리</li>
     *   <li>WARN 레벨로 기록하여 즉시 알람 대상</li>
     *   <li>클라이언트 IP로 공격 패턴 추적 가능</li>
     * </ul>
     * 
     * @param joinPoint AOP 조인포인트 - 예외가 발생한 메소드 정보 포함
     * @param ex 발생한 예외 객체 - 보안 실패 원인 정보 포함
     */
    @AfterThrowing(value = "authMethods() || securityMethods()", throwing = "ex")
    public void logSecurityFailure(final JoinPoint joinPoint, final Throwable ex) {
        final String method = sanitizeLogInput(joinPoint.getSignature().toShortString());
        final String username = sanitizeLogInput(getCurrentUsername());
        final String clientIp = sanitizeLogInput(MDCUtil.getClientIp());
        final String exceptionMessage = sanitizeLogInput(maskSensitiveInfo(ex.getMessage()));

        if (log.isWarnEnabled()) {
            log.warn(messageUtils.getMessage("log.security.auth.failure",
                    method, exceptionMessage, clientIp));
        }
    }

    /**
     * 권한이 필요한 메소드 실행 전에 권한 검사 시작을 로깅합니다.
     * 
     * <p>{@code @PreAuthorize} 어노테이션이 적용된 메소드가 호출될 때
     * 권한 검사 과정을 디버그 레벨로 로깅하여 권한 관련 문제 해결을 지원합니다.</p>
     * 
     * <p><strong>로깅 정보:</strong></p>
     * <ul>
     *   <li>권한 검사 대상 메소드명 (CRLF 인젝션 방지 처리됨)</li>
     *   <li>권한 검사를 받는 사용자명 (CRLF 인젝션 방지 처리됨)</li>
     * </ul>
     * 
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>DEBUG 레벨 로그로 운영 환경에서는 성능 영향 최소화</li>
     *   <li>{@code log.isDebugEnabled()} 사전 검사로 불필요한 문자열 연산 방지</li>
     * </ul>
     * 
     * @param joinPoint AOP 조인포인트 - 권한 검사 대상 메소드 정보 포함
     */
    @Before("authorizedMethods()")
    public void logAuthorizedAccess(final JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            final String method = sanitizeLogInput(joinPoint.getSignature().toShortString());
            final String username = sanitizeLogInput(getCurrentUsername());
            
            log.debug(messageUtils.getMessage("log.security.authorized.access", method, username));
        }
    }

    /**
     * 현재 보안 컨텍스트에서 인증된 사용자명을 안전하게 추출합니다.
     * 
     * <p>Spring Security의 SecurityContextHolder에서 현재 인증 정보를 조회하여
     * 사용자명을 반환합니다. 인증되지 않은 상태이거나 인증 정보가 없는 경우
     * "anonymous"를 반환하여 로깅 안정성을 보장합니다.</p>
     * 
     * <p><strong>보안 고려사항:</strong></p>
     * <ul>
     *   <li>null 안전성: 인증 객체가 null인 경우 예외 방지</li>
     *   <li>인증 상태 검증: 인증되지 않은 상태 처리</li>
     *   <li>민감정보 보호: 사용자명만 추출하여 개인정보 노출 방지</li>
     * </ul>
     * 
     * @return 현재 인증된 사용자명 또는 "anonymous" (null 반환하지 않음)
     * 
     * @implNote 이 메소드는 로깅 전용으로 설계되어 보안에 민감하지 않은 사용자 식별자만 반환
     */
    private String getCurrentUsername() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
    
    /**
     * CRLF 인젝션 공격을 방지하기 위해 로그 입력값을 정화합니다.
     * 
     * <p>로그 메시지에 포함되는 모든 사용자 입력값에서 개행 문자(CR, LF)와
     * 기타 제어 문자를 제거하여 로그 조작 공격을 방지합니다.</p>
     * 
     * <p><strong>제거 대상 문자:</strong></p>
     * <ul>
     *   <li>\r (Carriage Return, ASCII 13)</li>
     *   <li>\n (Line Feed, ASCII 10)</li>
     *   <li>\t (Tab, ASCII 9) - 로그 형식 보호</li>
     *   <li>기타 ASCII 제어 문자 (0-31, 127)</li>
     * </ul>
     * 
     * @param input 정화할 입력 문자열 (null 안전)
     * @return 정화된 문자열 (null 입력 시 빈 문자열 반환)
     * 
     * @implNote 성능을 위해 정규식 대신 문자 단위 처리 사용
     */
    private String sanitizeLogInput(final String input) {
        if (input == null) {
            return "";
        }
        // CRLF 인젝션 방지: 개행문자 및 제어문자 제거
        return input.replaceAll("[\\r\\n\\t\\p{Cntrl}]", "_");
    }
    
    /**
     * 로그에서 민감한 정보를 마스킹 처리합니다.
     * 
     * <p>예외 메시지나 기타 로그 정보에서 비밀번호, 토큰, 개인정보 등
     * 민감한 데이터가 노출되지 않도록 마스킹 처리합니다.</p>
     * 
     * <p><strong>마스킹 대상:</strong></p>
     * <ul>
     *   <li>password, pwd 관련 정보</li>
     *   <li>token, jwt 관련 정보</li>
     *   <li>secret, key 관련 정보</li>
     * </ul>
     * 
     * @param message 마스킹할 메시지 (null 안전)
     * @return 민감정보가 마스킹된 메시지 (null 입력 시 빈 문자열 반환)
     */
    private String maskSensitiveInfo(final String message) {
        if (message == null) {
            return "";
        }
        return message
            .replaceAll("(?i)(password|pwd|token|jwt|secret|key)=[^\\s,}]+", "$1=***")
            .replaceAll("(?i)(password|pwd|token|jwt|secret|key)\\s*[:=]\\s*[^\\s,}]+", "$1=***");
    }
}
