package bunny.boardhole.shared.config.log;

import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.result.UserResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Generic business logging powered by AOP with i18n support.
 * Logs method start/end at DEBUG, errors at WARN, and success events at INFO.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BusinessLogAspect {

    private final MessageUtils messageUtils;

    @Around("execution(* bunny.boardhole..application.command.*.*(..))")
    public Object logCommands(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String methodName = signature.getName();
        long start = System.currentTimeMillis();
        log.debug(messageUtils.getMessage("log.method.start", methodName));
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.debug(messageUtils.getMessage("log.method.end", methodName, elapsed));
            logSuccess(signature, result);
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn(messageUtils.getMessage("log.method.error", methodName, elapsed, t.getMessage()), t);
            throw t;
        }
    }

    private void logSuccess(MethodSignature signature, Object result) {
        String entity = signature.getDeclaringType().getSimpleName()
                .replace("CommandService", "").toLowerCase(Locale.ROOT);
        String action = switch (signature.getName()) {
            case "create" -> "created";
            case "update" -> "updated";
            case "delete" -> "deleted";
            default -> null;
        };
        if (action == null) {
            return;
        }
        String key = "log." + entity + "." + action;
        Object[] logArgs = extractArgs(result);
        log.info(messageUtils.getMessage(key, logArgs));
    }

    private Object[] extractArgs(Object result) {
        if (result instanceof UserResult user) {
            return new Object[]{user.username(), user.email()};
        }
        if (result instanceof BoardResult board) {
            return new Object[]{board.id(), board.title(), board.authorName()};
        }
        return new Object[0];
    }
}
