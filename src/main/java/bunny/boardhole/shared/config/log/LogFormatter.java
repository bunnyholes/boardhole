package bunny.boardhole.shared.config.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LogFormatter {

    private static final Set<String> SENSITIVE_FIELD_NAMES = new HashSet<>(Arrays.asList(LogConstants.SENSITIVE_FIELDS));
    private final MessageSource messageSource;
    private final LoggingProperties loggingProperties;

    public LogFormatter(MessageSource messageSource, LoggingProperties loggingProperties) {
        this.messageSource = messageSource;
        this.loggingProperties = loggingProperties;
    }

    public String formatMethodStart(String signature, Object[] args) {
        String layerColor = getLayerColor(signature);
        String layerIcon = getLayerIcon(signature);
        String argsString = Arrays.stream(args)
                .map(this::safeToString)
                .collect(Collectors.joining(", "));

        return messageSource.getMessage("log.method.start",
                new Object[]{layerColor + layerIcon + signature + LogConstants.RESET, argsString},
                LocaleContextHolder.getLocale());
    }

    public String formatMethodEnd(String signature, Object result, long tookMs) {
        String layerColor = getLayerColor(signature);
        String layerIcon = getLayerIcon(signature);
        String perfColor = getPerformanceColor(tookMs);
        String perfIcon = getPerformanceIcon(tookMs);

        return messageSource.getMessage("log.method.end",
                new Object[]{layerColor + layerIcon + signature + LogConstants.RESET,
                        safeResultToString(result), perfColor + perfIcon + tookMs + LogConstants.RESET},
                LocaleContextHolder.getLocale());
    }

    public String formatMethodError(String signature, long tookMs, String errorMessage) {
        return messageSource.getMessage("log.method.error",
                new Object[]{LogConstants.RED + signature + LogConstants.RESET, tookMs, errorMessage},
                LocaleContextHolder.getLocale());
    }

    public String formatRequestStart(String method, String uri, String remoteAddr) {
        return messageSource.getMessage("log.request.start",
                new Object[]{method, uri, remoteAddr},
                LocaleContextHolder.getLocale());
    }

    public String formatRequestEnd(String method, String uri, int status, long tookMs) {
        String statusColor = getStatusColor(status);
        String perfIcon = getPerformanceIcon(tookMs);

        return messageSource.getMessage("log.request.end",
                new Object[]{method, uri, statusColor + status + LogConstants.RESET, perfIcon + tookMs},
                LocaleContextHolder.getLocale());
    }

    // 레이어별 색상
    public String getLayerColor(String signature) {
        if (signature.contains("Controller")) return LogConstants.BLUE;
        else if (signature.contains("Service")) return LogConstants.CYAN;
        else if (signature.contains("Repository")) return LogConstants.PURPLE;
        else return LogConstants.RESET;
    }

    // 레이어별 아이콘
    public String getLayerIcon(String signature) {
        if (signature.contains("Controller")) return LogConstants.CONTROLLER_ICON;
        else if (signature.contains("Service")) return LogConstants.SERVICE_ICON;
        else if (signature.contains("Repository")) return LogConstants.REPOSITORY_ICON;
        else return LogConstants.DEFAULT_ICON;
    }

    // 성능 기반 색상 (설정 값 활용)
    public String getPerformanceColor(long ms) {
        if (loggingProperties.isFast(ms)) return LogConstants.GREEN;
        else if (loggingProperties.isNormal(ms)) return LogConstants.YELLOW;
        else return LogConstants.RED;
    }

    // 성능 기반 아이콘 (설정 값 활용)
    public String getPerformanceIcon(long ms) {
        if (loggingProperties.isFast(ms)) return LogConstants.FAST_ICON;
        else if (loggingProperties.isNormal(ms)) return LogConstants.NORMAL_ICON;
        else return LogConstants.SLOW_ICON;
    }

    // 성능 경고 확인
    public boolean shouldWarnPerformance(long ms) {
        return loggingProperties.isSlow(ms);
    }

    // HTTP 상태별 색상
    public String getStatusColor(int status) {
        if (status >= 200 && status < 300) return LogConstants.GREEN;
        else if (status >= 300 && status < 400) return LogConstants.YELLOW;
        else if (status >= 400 && status < 500) return LogConstants.RED;
        else if (status >= 500) return LogConstants.PURPLE;
        else return LogConstants.RESET;
    }

    // 안전한 객체 문자열 변환
    public String safeToString(Object arg) {
        if (arg == null) return "null";
        if (isProblematicType(arg)) return arg.getClass().getName();
        try {
            return sanitizeObject(arg);
        } catch (Exception e) {
            return arg.getClass().getName();
        }
    }

    // 안전한 결과 문자열 변환
    public String safeResultToString(Object result) {
        if (result == null) return "null";
        String className = result.getClass().getName();
        if (isProblematicType(result)) return className;
        try {
            return sanitizeObject(result);
        } catch (Exception e) {
            return className;
        }
    }

    // 문제가 될 수 있는 타입 체크
    private boolean isProblematicType(Object o) {
        return o instanceof jakarta.servlet.ServletRequest
                || o instanceof jakarta.servlet.ServletResponse
                || o instanceof org.springframework.web.multipart.MultipartFile
                || o instanceof byte[]
                || o instanceof java.io.InputStream
                || o instanceof java.io.OutputStream
                || o instanceof java.io.File;
    }

    // 객체 정보 안전하게 추출 (StringBuilder 최적화)
    private String sanitizeObject(Object obj) throws IllegalAccessException {
        Class<?> cls = obj.getClass();
        if (isJdkType(cls)) {
            return String.valueOf(obj);
        }

        // TODO: 성능 최적화 - 실제 부하 발생 시 추가 개선
        // 1. StringBuilder 풀링 사용 고려 (ThreadLocal<StringBuilder>)
        // 2. Reflection 캐싱으로 Field 접근 최적화
        // 3. 자주 사용되는 객체 타입에 대한 캐싱

        Field[] fields = cls.getDeclaredFields();
        // StringBuilder 초기 용량 최적화 (필드당 평균 20자 예상)
        int estimatedSize = cls.getSimpleName().length() + 2 + (fields.length * 20);
        StringBuilder sb = new StringBuilder(estimatedSize);

        sb.append(cls.getSimpleName()).append('{');

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            Object value = field.get(obj);
            sb.append(field.getName()).append('=');

            if (SENSITIVE_FIELD_NAMES.contains(field.getName().toLowerCase())) {
                sb.append(getSensitiveFieldMask(field.getName()));
            } else if (value == null) {
                sb.append("null");
            } else if (isProblematicType(value)) {
                sb.append(value.getClass().getName());
            } else if (isJdkType(value.getClass())) {
                String strValue = String.valueOf(value);
                if (strValue.length() > 100) {
                    sb.append(strValue, 0, 97).append("...");
                } else {
                    sb.append(strValue);
                }
            } else {
                sb.append(value.getClass().getSimpleName());
            }

            if (i < fields.length - 1) sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }

    // JDK 타입 체크
    private boolean isJdkType(Class<?> cls) {
        Package pkg = cls.getPackage();
        String packageName = (pkg != null) ? pkg.getName() : "";
        return cls.isPrimitive()
                || packageName.startsWith("java.")
                || packageName.startsWith("jakarta.")
                || packageName.startsWith("org.springframework.");
    }

    // 민감정보 마스킹 (강화된 보안)
    private String getSensitiveFieldMask(String fieldName) {
        String lowerName = fieldName.toLowerCase();
        if (lowerName.contains("password") || lowerName.contains("pwd")) {
            return messageSource.getMessage("mask.password", null, LocaleContextHolder.getLocale());
        } else if (lowerName.contains("token")) {
            return messageSource.getMessage("mask.token", null, LocaleContextHolder.getLocale());
        } else if (lowerName.contains("secret")) {
            return messageSource.getMessage("mask.secret", null, LocaleContextHolder.getLocale());
        } else if (lowerName.contains("credential")) {
            return messageSource.getMessage("mask.credential", null, LocaleContextHolder.getLocale());
        } else if (lowerName.contains("key")) {
            return messageSource.getMessage("mask.key", null, LocaleContextHolder.getLocale());
        } else {
            return messageSource.getMessage("mask.sensitive", null, LocaleContextHolder.getLocale());
        }
    }

    // 로깅 예외 안전 처리
    public String safeLog(String message, Object... args) {
        try {
            if (args == null || args.length == 0) {
                return message;
            }
            Object[] sanitizedArgs = Arrays.stream(args)
                    .map(this::safeToString)
                    .toArray();
            return String.format(message, sanitizedArgs);
        } catch (Exception e) {
            log.warn(messageSource.getMessage("log.format.error", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
            return message + " [LOG_FORMAT_ERROR]";
        }
    }
}