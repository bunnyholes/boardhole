package bunny.boardhole.config.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class LoggingAspect {

    private static final Set<String> SENSITIVE_FIELD_NAMES = new HashSet<>(Arrays.asList(
            "password", "pwd", "secret", "token"
    ));

    @Pointcut("execution(public * bunny.boardhole..controller..*(..))")
    void anyController() {}

    @Pointcut("execution(public * bunny.boardhole..service..*(..))")
    void anyService() {}

    @Pointcut("execution(public * bunny.boardhole..repository..*(..))")
    void anyRepository() {}

    @Around("anyController() || anyService() || anyRepository()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String signature = pjp.getSignature().toShortString();
        String argsString = Arrays.stream(pjp.getArgs())
                .map(this::safeToString)
                .collect(Collectors.joining(", "));
        long start = System.nanoTime();
        log.info("-> {}({})", signature, argsString);
        try {
            Object result = pjp.proceed();
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info("<- {} = {} ({}ms)", signature, safeResultToString(result), tookMs);
            return result;
        } catch (Throwable ex) {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.error("!! {} threw ({})ms)", signature, tookMs, ex);
            throw ex;
        }
    }

    private String safeResultToString(Object result) {
        if (result == null) return "null";
        String cn = result.getClass().getName();
        if (isProblematicType(result)) return cn;
        try {
            return sanitizeObject(result);
        } catch (Exception e) {
            return cn;
        }
    }

    private String safeToString(Object arg) {
        if (arg == null) return "null";
        if (isProblematicType(arg)) return arg.getClass().getName();
        try {
            return sanitizeObject(arg);
        } catch (Exception e) {
            return arg.getClass().getName();
        }
    }

    private boolean isProblematicType(Object o) {
        return o instanceof jakarta.servlet.ServletRequest
                || o instanceof jakarta.servlet.ServletResponse
                || o instanceof org.springframework.web.multipart.MultipartFile
                || o instanceof byte[]
                || o instanceof java.io.InputStream
                || o instanceof java.io.OutputStream
                || o instanceof java.io.File;
    }

    private String sanitizeObject(Object obj) throws IllegalAccessException {
        Class<?> cls = obj.getClass();
        if (isJdkType(cls)) {
            return String.valueOf(obj);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cls.getSimpleName()).append('{');
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            Object v = f.get(obj);
            sb.append(f.getName()).append('=');
            if (SENSITIVE_FIELD_NAMES.contains(f.getName().toLowerCase())) {
                sb.append("***");
            } else if (v == null) {
                sb.append("null");
            } else if (isProblematicType(v)) {
                sb.append(v.getClass().getName());
            } else if (isJdkType(v.getClass())) {
                sb.append(v);
            } else {
                sb.append(v.getClass().getSimpleName());
            }
            if (i < fields.length - 1) sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }

    private boolean isJdkType(Class<?> cls) {
        Package p = cls.getPackage();
        String pn = (p != null) ? p.getName() : "";
        return cls.isPrimitive() || pn.startsWith("java.") || pn.startsWith("jakarta.") || pn.startsWith("org.springframework.");
    }
}
