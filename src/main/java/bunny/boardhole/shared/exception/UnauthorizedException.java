package bunny.boardhole.shared.exception;

import lombok.experimental.StandardException;
import org.springframework.context.MessageSource;

/**
 * 인증이 실패했을 때 발생하는 예외 클래스입니다.
 * 사용자의 신원 확인이 실패했을 때 발생합니다.
 * HTTP 401 Unauthorized 상태코드로 응답됩니다.
 * 
 * @author 시스템 개발팀
 * @version 1.0
 * @since 1.0.0
 */
@StandardException
public class UnauthorizedException extends RuntimeException {
    
    /** 
     * 직렬화 버전 UID입니다.
     * 클래스 구조 변경 시 반드시 업데이트해야 합니다.
     */
    private static final long serialVersionUID = 1L;
    
    /** 
     * 다국어 메시지 처리를 위한 메시지 소스입니다.
     * 아키텍처 규칙에 따라 모든 예외 클래스에 포함됩니다.
     * transient로 선언하여 직렬화에서 제외합니다.
     */
    protected transient MessageSource messageSource;
}
