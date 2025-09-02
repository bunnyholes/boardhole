package bunny.boardhole.shared.util;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * JPA 엔티티에서 사용할 수 있는 메시지 제공자
 * Spring 컨텍스트 없이도 동작하며, 테스트 환경에서도 일관된 동작을 보장합니다.
 */
@Component
public class EntityMessageProvider {

    private static MessageUtils messageUtils;

    public EntityMessageProvider(MessageUtils messageUtils) {
        EntityMessageProvider.messageUtils = messageUtils;
    }

    /**
     * 메시지 키를 사용하여 현재 로케일에 맞는 메시지를 반환합니다.
     * MessageSource가 사용할 수 없는 경우 영어 폴백 메시지를 반환합니다.
     *
     * @param key 메시지 키
     * @param fallbackMessage 폴백 메시지 (영어)
     * @param args 메시지 파라미터
     * @return 로케일에 맞는 메시지 또는 폴백 메시지
     */
    public static String getMessage(String key, String fallbackMessage, Object... args) {
        try {
            if (messageUtils != null) {
                return messageUtils.getMessage(key, args);
            }
        } catch (Exception e) {
            // MessageSource 사용 불가능한 경우 폴백
        }
        
        // 폴백 메시지에 파라미터 적용
        if (args != null && args.length > 0) {
            return String.format(fallbackMessage, args);
        }
        return fallbackMessage;
    }

    /**
     * 파라미터 없는 메시지 조회
     */
    public static String getMessage(String key, String fallbackMessage) {
        return getMessage(key, fallbackMessage, new Object[0]);
    }

    /**
     * 테스트를 위한 메시지 초기화
     */
    public static void setMessageUtilsForTest(MessageUtils testMessageUtils) {
        messageUtils = testMessageUtils;
    }

    /**
     * 테스트 후 정리
     */
    public static void clearForTest() {
        // 실제 MessageUtils는 유지하되, 테스트 후 초기화만 수행
    }
}