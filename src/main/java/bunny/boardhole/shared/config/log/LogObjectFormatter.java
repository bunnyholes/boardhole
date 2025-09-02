package bunny.boardhole.shared.config.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 로그 메시지의 객체 포맷팅과 보안 처리를 담당하는 전용 포맷터 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 보안 및 안전 기능을 제공합니다:
 * <ul>
 *   <li>민감정보 마스킹 - password, token 등 민감정보 보호</li>
 *   <li>CRLF 인젝션 방지 - 개행문자 및 탭 문자 제거</li>
 *   <li>예외 안전 처리 - 로그 포맷팅 오류 시 안전한 대안 제공</li>
 *   <li>성능 최적화 - StringBuilder 초기 용량 최적화로 메모리 할당 최소화</li>
 * </ul>
 * </p>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class LogObjectFormatter {

    /** 민감정보 필드명 집합 - 로깅 시 마스킹 대상 */
    private static final Set<String> SENSITIVE_FIELDS = LogConstants.SENSITIVE_FIELDS;
    
    /** CRLF 인젝션 공격을 방지하기 위한 패턴 */
    private static final Pattern CRLF_PATTERN = Pattern.compile("[\\r\\n\\t]");
    
    /** 다국어 메시지 지원을 위한 MessageSource */
    private final MessageSource messageSource;

    /**
     * LogObjectFormatter를 생성합니다.
     *
     * @param messageSource 다국어 메시지 소스 (final)
     */
    public LogObjectFormatter(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 객체를 안전하게 문자열로 변환합니다.
     *
     * @param arg 변환할 객체 (final)
     * @return 안전하게 변환된 문자열
     */
    public String safeToString(final Object arg) {
        if (arg == null) {
            return "null";
        }
        if (isProblematicType(arg)) {
            return arg.getClass().getName();
        }
        try {
            return sanitizeObject(arg);
        } catch (final Exception e) {
            return arg.getClass().getName();
        }
    }

    /**
     * 결과 객체를 안전하게 문자열로 변환합니다.
     *
     * @param result 변환할 결과 객체 (final)
     * @return 안전하게 변환된 결과 문자열
     */
    public String safeResultToString(final Object result) {
        if (result == null) {
            return "null";
        }
        final String className = result.getClass().getName();
        if (isProblematicType(result)) {
            return className;
        }
        try {
            return sanitizeObject(result);
        } catch (final Exception e) {
            return className;
        }
    }

    /**
     * 로깅 예외를 안전하게 처리하여 로그 포맷팅 오류가 비즈니스 로직에 영향을 주지 않도록 합니다.
     * <p>
     * 이 메소드는 다음과 같은 기능을 제공합니다:
     * <ul>
     *   <li>메시지 포맷팅 예외 처리 - 포맷팅 실패 시 원본 메시지 반환</li>
     *   <li>인자 안전 변환 - 모든 인자를 안전한 문자열로 변환</li>
     *   <li>CRLF 인젝션 방지 - 개행문자 등 악성 입력 차단</li>
     *   <li>성능 최적화 - 로그 레벨 가드를 통한 불필요한 처리 방지</li>
     * </ul>
     * </p>
     * 
     * @param message 포맷팅할 메시지 템플릿 (null 허용) (final)
     * @param args 메시지에 삽입할 인자들 (null 허용) (final)
     * @return 안전하게 포맷팅된 로그 메시지
     */
    public String safeLog(final String message, final Object... args) {
        // 로그 레벨 가드 - DEBUG 레벨이 비활성화되면 불필요한 처리 생략
        if (!log.isDebugEnabled() && (message == null || message.isEmpty())) {
            return "";
        }
        
        try {
            if (message == null) {
                return "null";
            }
            
            if (args == null || args.length == 0) {
                return sanitizeForLog(message);
            }
            
            // 성능 최적화: 스트림 대신 전통적인 루프 사용 (작은 배열에서 더 효율적)
            final Object[] sanitizedArgs = sanitizeArguments(args);
            
            return sanitizeForLog(String.format(message, sanitizedArgs));
        } catch (final Exception e) {
            handleLogFormatError(e);
            return sanitizeForLog(message) + " [LOG_FORMAT_ERROR]";
        }
    }

    /**
     * 문제가 될 수 있는 타입인지 확인합니다.
     *
     * @param obj 확인할 객체 (final)
     * @return 문제가 될 수 있는 타입이면 true
     */
    private boolean isProblematicType(final Object obj) {
        return obj instanceof jakarta.servlet.ServletRequest
                || obj instanceof jakarta.servlet.ServletResponse
                || obj instanceof org.springframework.web.multipart.MultipartFile
                || obj instanceof byte[]
                || obj instanceof java.io.InputStream
                || obj instanceof java.io.OutputStream
                || obj instanceof java.io.File;
    }

    /**
     * 객체 정보를 안전하게 추출합니다 (StringBuilder 최적화).
     *
     * @param obj 추출할 객체 (final)
     * @return 안전하게 추출된 객체 정보 문자열
     * @throws IllegalAccessException 필드 접근 시 발생할 수 있는 예외
     */
    private String sanitizeObject(final Object obj) throws IllegalAccessException {
        final Class<?> cls = obj.getClass();
        if (isJdkType(cls)) {
            return String.valueOf(obj);
        }

        // TODO: 성능 최적화 - 실제 부하 발생 시 추가 개선
        // 1. StringBuilder 풀링 사용 고려 (ThreadLocal<StringBuilder>)
        // 2. Reflection 캐싱으로 Field 접근 최적화
        // 3. 자주 사용되는 객체 타입에 대한 캐싱

        final Field[] fields = cls.getDeclaredFields();
        final StringBuilder sb = createOptimalStringBuilder(cls, fields);
        
        appendClassNameAndOpenBrace(sb, cls);
        appendFields(sb, fields, obj);
        sb.append('}');
        
        return sb.toString();
    }

    /**
     * JDK 타입인지 확인합니다.
     *
     * @param cls 확인할 클래스 (final)
     * @return JDK 타입이면 true
     */
    private boolean isJdkType(final Class<?> cls) {
        final Package pkg = cls.getPackage();
        final String packageName = (pkg != null) ? pkg.getName() : "";
        return cls.isPrimitive()
                || packageName.startsWith("java.")
                || packageName.startsWith("jakarta.")
                || packageName.startsWith("org.springframework.");
    }

    /**
     * 민감정보 마스킹 문자열을 반환합니다 (강화된 보안).
     *
     * @param fieldName 필드명 (final)
     * @return 마스킹 처리된 문자열
     */
    private String getSensitiveFieldMask(final String fieldName) {
        final String lowerName = fieldName.toLowerCase(Locale.ROOT);
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

    /**
     * CRLF 인젝션 공격을 방지하기 위해 입력 문자열을 살균 처리합니다.
     * <p>
     * 개행문자(\r, \n)와 탭 문자(\t)를 제거하여 로그 파싱에 문제를 일으킬 수 있는
     * 악성 입력을 차단합니다. 또한 1000자로 제한하여 DoS 공격을 방지합니다.
     * </p>
     * 
     * @param input 살균 처리할 입력 문자열 (final)
     * @return 살균 처리된 문자열
     */
    private String sanitizeForLog(final String input) {
        if (input == null) {
            return "null";
        }
        
        String sanitized = CRLF_PATTERN.matcher(input).replaceAll("_");
        
        // DoS 공격 방지를 위한 문자열 길이 제한
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 997) + "...";
        }
        
        return sanitized;
    }

    /**
     * 인자들을 안전하게 살균 처리합니다.
     *
     * @param args 살균 처리할 인자 배열 (final)
     * @return 살균 처리된 인자 배열
     */
    private Object[] sanitizeArguments(final Object... args) {
        final Object[] sanitizedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            sanitizedArgs[i] = safeToString(args[i]);
        }
        return sanitizedArgs;
    }

    /**
     * 로그 포맷 오류를 처리합니다.
     *
     * @param e 발생한 예외 (final)
     */
    private void handleLogFormatError(final Exception e) {
        // 로그 레벨 가드 적용
        if (log.isWarnEnabled()) {
            log.warn(messageSource.getMessage("log.format.error",
                    new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * 최적화된 StringBuilder를 생성합니다.
     *
     * @param cls 클래스 (final)
     * @param fields 필드 배열 (final)
     * @return 최적화된 StringBuilder
     */
    private StringBuilder createOptimalStringBuilder(final Class<?> cls, final Field[] fields) {
        // StringBuilder 초기 용량 최적화 (필드당 평균 20자 예상)
        final int estimatedSize = cls.getSimpleName().length() + 2 + (fields.length * 20);
        return new StringBuilder(estimatedSize);
    }

    /**
     * StringBuilder에 클래스명과 열린 중괄호를 추가합니다.
     *
     * @param sb StringBuilder (final)
     * @param cls 클래스 (final)
     */
    private void appendClassNameAndOpenBrace(final StringBuilder sb, final Class<?> cls) {
        sb.append(cls.getSimpleName()).append('{');
    }

    /**
     * StringBuilder에 필드 정보를 추가합니다.
     *
     * @param sb StringBuilder (final)
     * @param fields 필드 배열 (final)
     * @param obj 대상 객체 (final)
     * @throws IllegalAccessException 필드 접근 시 발생할 수 있는 예외
     */
    private void appendFields(final StringBuilder sb, final Field[] fields, final Object obj)
            throws IllegalAccessException {
        for (int i = 0; i < fields.length; i++) {
            final Field field = fields[i];
            field.setAccessible(true);
            final Object value = field.get(obj);
            
            appendFieldInfo(sb, field, value);
            
            if (i < fields.length - 1) {
                sb.append(',');
            }
        }
    }

    /**
     * StringBuilder에 개별 필드 정보를 추가합니다.
     *
     * @param sb StringBuilder (final)
     * @param field 필드 (final)
     * @param value 필드 값 (final)
     */
    private void appendFieldInfo(final StringBuilder sb, final Field field, final Object value) {
        sb.append(field.getName()).append('=');
        
        if (SENSITIVE_FIELDS.contains(field.getName().toLowerCase(Locale.ROOT))) {
            sb.append(getSensitiveFieldMask(field.getName()));
        } else if (value == null) {
            sb.append("null");
        } else if (isProblematicType(value)) {
            sb.append(value.getClass().getName());
        } else if (isJdkType(value.getClass())) {
            appendJdkTypeValue(sb, value);
        } else {
            sb.append(value.getClass().getSimpleName());
        }
    }

    /**
     * StringBuilder에 JDK 타입 값을 추가합니다.
     *
     * @param sb StringBuilder (final)
     * @param value JDK 타입 값 (final)
     */
    private void appendJdkTypeValue(final StringBuilder sb, final Object value) {
        final String strValue = String.valueOf(value);
        if (strValue.length() > 100) {
            sb.append(strValue, 0, 97).append("...");
        } else {
            sb.append(strValue);
        }
    }
}
