package bunny.boardhole.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 유효성 검증 실패 시 발생하는 예외 클래스입니다.
 * HTTP 400 Bad Request 상태코드로 응답됩니다.
 * 
 * @author 시스템 개발팀
 * @version 1.0
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    
    /** 
     * 직렬화 버전 UID입니다.
     * 클래스 구조 변경 시 반드시 업데이트해야 합니다.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 메시지만 포함하는 유효성 검증 예외를 생성합니다.
     * 
     * @param message 예외 메시지 (null 허용되지 않음)
     * @throws IllegalArgumentException 메시지가 null이거나 빈 문자열인 경우
     */
    public ValidationException(final String message) {
        super(sanitizeMessage(message));
        validateMessage(message);
    }

    /**
     * 메시지와 원인 예외를 포함하는 유효성 검증 예외를 생성합니다.
     * 
     * @param message 예외 메시지 (null 허용되지 않음)
     * @param cause 원인 예외 (null 허용)
     * @throws IllegalArgumentException 메시지가 null이거나 빈 문자열인 경우
     */
    public ValidationException(final String message, final Throwable cause) {
        super(sanitizeMessage(message), cause);
        validateMessage(message);
    }
    
    /**
     * 예외 메시지의 유효성을 검증합니다.
     * 
     * @param message 검증할 메시지
     * @throws IllegalArgumentException 메시지가 null이거나 빈 문자열인 경우
     */
    private static void validateMessage(final String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("예외 메시지는 null이거나 빈 문자열일 수 없습니다.");
        }
    }
    
    /**
     * 로그 인젝션 공격을 방지하기 위해 메시지를 sanitize합니다.
     * 
     * @param message sanitize할 메시지
     * @return sanitize된 메시지
     */
    private static String sanitizeMessage(final String message) {
        if (message == null) {
            return "알 수 없는 유효성 검증 오류가 발생했습니다.";
        }
        
        // CRLF 및 제어 문자 제거
        final String sanitized = message.replaceAll("[\\r\\n\\t\\p{Cntrl}]", "_");
        
        // 메시지 길이 제한 (DoS 방지)
        if (sanitized.length() > 500) {
            return sanitized.substring(0, 497) + "...";
        }
        
        return sanitized;
    }
}