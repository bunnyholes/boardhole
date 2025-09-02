package bunny.boardhole.shared.exception;

import lombok.experimental.StandardException;
import org.springframework.context.MessageSource;

/**
 * 비즈니스 로직 충돌 시 발생하는 예외 클래스입니다.
 * HTTP 409 Conflict 상태코드로 응답됩니다.
 * 
 * <p>주요 사용 사례: 중복 데이터 생성, 동시성 충돌, 비즈니스 규칙 위반</p>
 * 
 * @author 시스템 개발팀
 * @version 1.0
 * @since 1.0.0
 */
@StandardException
public class ConflictException extends RuntimeException {
    
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
