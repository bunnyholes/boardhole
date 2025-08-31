package bunny.boardhole.common.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class MessageUtils {
    
    private final MessageSource messageSource;
    
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
    
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
    
    // Static access를 위한 singleton 패턴
    private static MessageUtils instance;
    
    @PostConstruct
    public void init() {
        instance = this;
    }
    
    public static String getMessageStatic(String key, Object... args) {
        if (instance == null) {
            return key; // fallback to key if not initialized
        }
        return instance.getMessage(key, args);
    }
}