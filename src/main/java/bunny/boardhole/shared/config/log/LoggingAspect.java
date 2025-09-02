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

        // 성능 임계값 기반 선택적 로깅
        // Repository 레이어는 기본적으로 로깅 최소화 (DB 호출이 많아 오버헤드 큼)
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

            // 성능 경고는 임계값 초과 시에만 (불필요한 로깅 감소)
            if (logFormatter.shouldWarnPerformance(tookMs)) {
                log.warn(messageSource.getMessage("log.performance.warning",
                        new Object[]{signature, tookMs},
                        org.springframework.context.i18n.LocaleContextHolder.getLocale()));
            }

            // 메서드 종료 로깅은 DEBUG 레벨에서만 (로깅 포맷 오류가 있어도 비즈니스 흐름에 영향 주지 않도록 보호)
            if (log.isDebugEnabled() && tookMs > 10) { // 10ms 이상인 경우만 로깅
                try {
                    log.debug(logFormatter.formatMethodEnd(signature, result, tookMs));
                } catch (Throwable formatEx) {
                    log.warn("Log formatting failed for {}: {}", signature, formatEx.toString());
                }
            }
            return result;
        } catch (Throwable ex) {
            long tookMs = (System.nanoTime() - start) / 1_000_000;

            // 에러는 항상 로깅
            log.error(logFormatter.formatMethodError(signature, tookMs, ex.getMessage()));
            throw ex;
        } finally {
            MDCUtil.clearMethod();
        }
    }

    private String extractLayer(String signature) {
        if (signature.contains("Controller")) return "controller";
        else if (signature.contains("Service")) return "service";
        else if (signature.contains("Repository")) return "repository";
        else return "unknown";
    }

}
