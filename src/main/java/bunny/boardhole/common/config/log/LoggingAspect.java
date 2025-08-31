package bunny.boardhole.common.config.log;

import bunny.boardhole.common.util.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
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
        
        MDCUtil.setLayer(layer);
        MDCUtil.setUserId();
        
        long start = System.nanoTime();
        
        if (log.isDebugEnabled()) {
            log.debug(logFormatter.formatMethodStart(signature, pjp.getArgs()));
        }
        
        try {
            Object result = pjp.proceed();
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            
            if (logFormatter.shouldWarnPerformance(tookMs)) {
                log.warn(MessageUtils.getMessageStatic("log.performance.warning", signature, tookMs));
            }
            
            if (log.isDebugEnabled()) {
                log.debug(logFormatter.formatMethodEnd(signature, result, tookMs));
            }
            return result;
        } catch (Throwable ex) {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            
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
