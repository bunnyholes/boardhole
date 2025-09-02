package bunny.boardhole.shared.config.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Spring AOP를 이용한 메서드 실행 로깅 애스팩트입니다.
 * <p>
 * 이 애스팩트는 Controller, Service, Repository 계층의 public 메서드 호출을 가로채어
 * 다음과 같은 로깅 기능을 제공합니다:
 * <ul>
 *   <li>메서드 시작/종료 로깅 - 매개변수와 반환값 추적</li>
 *   <li>실행 시간 측정 - 성능 모니터링 및 성능 경고</li>
 *   <li>예외 로깅 - 에러 발생 시 상세 정보 기록</li>
 *   <li>MDC 컨텍스트 설정 - 요청 추적을 위한 메타데이터 관리</li>
 *   <li>성능 최적화 - Repository 계층 로깅 최소화 및 레벨별 가드</li>
 * </ul>
 * </p>
 *
 * <p>성능 고려사항:</p>
 * <ul>
 *   <li>Repository 계층은 TRACE 레벨에서만 로깅 (데이터베이스 호출 빈도가 높아 오버헤드 최소화)</li>
 *   <li>10ms 이하 실행 시간은 종료 로깅 생략</li>
 *   <li>로그 포맷팅 오류 처리로 비즈니스 로직 안정성 보장</li>
 * </ul>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 * @see LogFormatter
 * @see MDCUtil
 * @see LoggingProperties
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "boardhole.logging.enabled", havingValue = "true", matchIfMissing = true)
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
public class LoggingAspect {

    private final MessageSource messageSource;
    private final LogFormatter logFormatter;
    private final LoggingProperties loggingProperties;

    @Pointcut("execution(public * bunny.boardhole..controller..*(..))")
    void anyController() {
    }

    @Pointcut("execution(public * bunny.boardhole..service..*(..))")
    void anyService() {
    }

    @Pointcut("execution(public * bunny.boardhole..repository..*(..))")
    void anyRepository() {
    }

    @Around("anyController() || anyService() || anyRepository()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String signature = pjp.getSignature().toShortString();
        String layer = extractLayer(signature);

        // TODO: 성능 최적화 - 실제 부하 발생 시 추가 최적화
        // 1. 특정 메서드만 로깅하도록 @Loggable 어노테이션 기반으로 변경
        // 2. 비동기 로깅으로 전환 (Logback AsyncAppender 사용)
        // 3. 로그 레벨별 필터링 강화

        // 성능 임계값 기반 선택적 로깅 - 로그 레벨 가드 적용
        // Repository 레이어는 기본적으로 로깅 최소화 (DB 호출이 많아 오버헤드 큼)
        // loggingProperties를 통해 성능 기반 로깅 제어도 가능
        boolean shouldLog = !"repository".equals(layer) || log.isTraceEnabled();

        if (!shouldLog) {
            // 로깅 없이 바로 실행 (Repository 레이어 성능 최적화)
            return pjp.proceed();
        }

        MDCUtil.setLayer(layer);
        MDCUtil.setUserId();

        long start = System.nanoTime();

        // 메서드 시작 로깅은 DEBUG 레벨에서만
        if (log.isDebugEnabled()) {
            log.debug(logFormatter.formatMethodStart(signature, pjp.getArgs()));
        }

        try {
            Object result = pjp.proceed();
            long tookMs = (System.nanoTime() - start) / 1_000_000;

            // 성능 경고는 임계값 초과 시에만 (로그 레벨 가드 적용)
            if (logFormatter.shouldWarnPerformance(tookMs) && log.isWarnEnabled()) {
                log.warn(messageSource.getMessage("log.performance.warning",
                        new Object[]{signature, tookMs},
                        org.springframework.context.i18n.LocaleContextHolder.getLocale()));
            }

            // 메서드 종료 로깅은 DEBUG 레벨에서만 (로깅 포맷 오류가 있어도 비즈니스 흐름에 영향 주지 않도록 보호)
            // loggingProperties의 성능 임계값을 활용한 선택적 로깅
            if (log.isDebugEnabled() && !loggingProperties.isFast(tookMs)) { // 빠른 응답이 아닌 경우만 로깅
                try {
                    log.debug(logFormatter.formatMethodEnd(signature, result, tookMs));
                } catch (final Throwable formatEx) {
                    if (log.isWarnEnabled()) {
                        log.warn("Log formatting failed for {}: {}", signature, formatEx.toString());
                    }
                }
            }
            return result;
        } catch (final Throwable ex) {
            final long tookMs = (System.nanoTime() - start) / 1_000_000;

            // 에러는 항상 로깅
            if (log.isErrorEnabled()) {
                log.error(logFormatter.formatMethodError(signature, tookMs, ex.getMessage()));
            }
            throw ex;
        } finally {
            MDCUtil.clearMethod();
        }
    }

    /**
     * 메서드 시그니처에서 애플리케이션 계층을 추출합니다.
     * <p>
     * Spring 아키텍처의 전통적인 3계층 구조를 기반으로 메서드가
     * 어떤 계층에 속하는지 단순한 문자열 포함 검사로 결정합니다.
     * 이 정보는 MDC 컨텍스트 설정과 로깅 레벨 제어에 사용됩니다.
     * </p>
     *
     * @param signature AOP에서 제공하는 메서드 시그니처
     * @return 계층 이름 ("controller", "service", "repository", "unknown")
     */
    private String extractLayer(String signature) {
        // 로그 레벨 가드: TRACE 레벨이 비활성화되면 불필요한 문자열 연산 생략
        if (!log.isTraceEnabled() && signature.length() > 100) {
            // 단순한 경우에만 검사 (성능 최적화)
            if (signature.indexOf("Controller") >= 0) return "controller";
            if (signature.indexOf("Service") >= 0) return "service";
            if (signature.indexOf("Repository") >= 0) return "repository";
        } else {
            // 정확한 검사
            if (signature.contains("Controller")) return "controller";
            if (signature.contains("Service")) return "service";
            if (signature.contains("Repository")) return "repository";
        }
        return "unknown";
    }

}
