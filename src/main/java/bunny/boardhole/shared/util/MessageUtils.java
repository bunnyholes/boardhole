package bunny.boardhole.shared.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 메시지 유틸리티 클래스입니다.
 * Spring MessageSource를 래핑하여 편리한 다국어 메시지 접근을 제공합니다.
 * 사용자의 현재 로케일을 기반으로 메시지를 반환합니다.
 * 
 * <p>이 클래스는 Spring Framework의 MessageSource를 사용하여
 * 국제화(i18n) 메시지를 조회하는 기능을 제공합니다.</p>
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>현재 사용자의 로케일을 기반으로 한 메시지 조회</li>
 *   <li>메시지 형식화 인자 지원</li>
 *   <li>Spring Component로 등록되어 의존성 주입 지원</li>
 * </ul>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class MessageUtils {

    /** 메시지 인자가 없을 때 사용하는 상수 */
    private static final Object[] NO_ARGUMENTS = null;
    
    /** 다국어 메시지 처리를 위한 메시지 소스 */
    private final MessageSource messageSource;

    /**
     * MessageUtils 생성자입니다.
     * 주입된 MessageSource를 인스턴스와 정적 사용 모두에 설정합니다.
     * 
     * @param messageSource 다국어 메시지 처리를 위한 메시지 소스
     */
    public MessageUtils(final MessageSource messageSource) {
        this.messageSource = messageSource;
        // MessageSource 초기화 완료
    }

    /**
     * 지정된 키와 인자들로 다국어 메시지를 조회합니다.
     * 사용자의 현재 로케일을 자동으로 사용합니다.
     * 
     * @param key 메시지 키
     * @param arguments 메시지 형식 인자들
     * @return 다국어처리된 메시지 문자열
     */
    public String getMessage(final String key, final Object... arguments) {
        return messageSource.getMessage(key, arguments, LocaleContextHolder.getLocale());
    }

    /**
     * 지정된 키로 다국어 메시지를 조회합니다.
     * 추가 인자 없이 순수한 메시지를 반환합니다.
     * 
     * @param key 메시지 키
     * @return 다국어처리된 메시지 문자열
     */
    public String getMessage(final String key) {
        return messageSource.getMessage(key, NO_ARGUMENTS, LocaleContextHolder.getLocale());
    }
}