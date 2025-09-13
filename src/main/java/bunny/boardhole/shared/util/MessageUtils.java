package bunny.boardhole.shared.util;

import lombok.experimental.UtilityClass;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 메시지 유틸리티 클래스
 * - 순수 정적 유틸리티(@UtilityClass)
 * - 기본 베이스네임: messages (application.yml과 일치)
 */
@UtilityClass
public class MessageUtils {

    // Suppress final warning: field must be mutable for test reflection access
    @SuppressWarnings("FieldMayBeFinal")
    private MessageSource messageSource = createDefaultMessageSource();

    private MessageSource createDefaultMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(true);
        return source;
    }

    /**
     * 메시지 키와 파라미터로 메시지 조회
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * 메시지 키로 메시지 조회 (파라미터 없음)
     */
    public String get(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
