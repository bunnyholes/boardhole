package bunny.boardhole.shared.util;

/**
 * JPA 엔티티에서 사용할 수 있는 메시지 제공자
 * 도메인 계층은 로케일/스프링 컨텍스트에 의존하지 않도록 폴백 메시지를 일관되게 반환합니다.
 */
public class EntityMessageProvider {

    private EntityMessageProvider() {}

    /**
     * 메시지 키를 사용하더라도, 엔티티 레벨에서는 폴백 메시지를 그대로 사용합니다.
     * 포맷 파라미터가 있는 경우 String.format을 적용합니다.
     */
    public static String getMessage(String key, String fallbackMessage, Object... args) {
        if (args != null && args.length > 0) {
            return String.format(fallbackMessage, args);
        }
        return fallbackMessage;
    }

    /**
     * 파라미터 없는 메시지 조회
     */
    public static String getMessage(String key, String fallbackMessage) {
        return fallbackMessage;
    }

    /** 테스트용 훅 - 더 이상 필요 없지만 시그니처 유지 */
    public static void setMessageUtilsForTest(MessageUtils testMessageUtils) {
        // no-op
    }

    /** 테스트 후 정리 - no-op */
    public static void clearForTest() {
        // no-op
    }
}