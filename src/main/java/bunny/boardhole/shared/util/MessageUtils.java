package bunny.boardhole.shared.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 메시지 유틸리티 클래스
 * Spring MessageSource를 래핑하여 편리한 메시지 접근 제공
 */
@Component
public class MessageUtils {

    private static MessageSource staticMessageSource;
    private final MessageSource messageSource;

    public MessageUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
        MessageUtils.staticMessageSource = messageSource;
    }

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}