package bunny.boardhole.shared.util;

import org.springframework.beans.BeansException;
import org.springframework.context.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 메시지 유틸리티 클래스
 * Spring MessageSource를 래핑하여 편리한 메시지 접근 제공
 * Static 메서드를 통해 어디서든 접근 가능
 */
@Component
public class MessageUtils implements ApplicationContextAware {

    private static MessageSource messageSource;

    /**
     * 메시지 키와 파라미터로 메시지 조회
     */
    public static String get(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * 메시지 키로 메시지 조회 (파라미터 없음)
     */
    public static String get(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * 기존 호환성을 위한 인스턴스 메서드 (deprecated)
     */
    @Deprecated
    public static String getMessage(String key, Object... args) {
        return get(key, args);
    }

    /**
     * 기존 호환성을 위한 인스턴스 메서드 (deprecated)
     */
    @Deprecated
    public static String getMessage(String key) {
        return get(key);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MessageUtils.messageSource = applicationContext.getBean(MessageSource.class);
    }
}