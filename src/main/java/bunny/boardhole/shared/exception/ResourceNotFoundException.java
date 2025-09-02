package bunny.boardhole.shared.exception;

import lombok.experimental.StandardException;
import org.springframework.context.MessageSource;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외 클래스입니다.
 * 요청된 리소스가 데이터베이스에 존재하지 않을 때 발생합니다.
 * HTTP 404 Not Found 상태코드로 응답됩니다.
 * 
 * @author 시스템 개발팀
 * @version 1.0
 * @since 1.0.0
 */
@StandardException
public class ResourceNotFoundException extends RuntimeException {
    
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
