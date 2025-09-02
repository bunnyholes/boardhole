package bunny.boardhole.shared.constants;

/**
 * 에러 코드 열거형입니다.
 * <p>
 * GlobalExceptionHandler에서 사용되어 일관된 에러 코드를 제공합니다.
 * RFC 7807 Problem Details 표준에 따른 오류 식별자를 정의합니다.
 * </p>
 * 
 * <p>사용 예시:</p>
 * <pre>{@code
 * throw new ConflictException(ErrorCode.USER_DUPLICATE_EMAIL, "이미 등록된 이메일입니다.");
 * }</pre>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
public enum ErrorCode {
    
    // ================================
    // HTTP 상태 기반 에러 코드
    // ================================
    
    /** 리소스 충돌 에러 */
    CONFLICT("CONFLICT"),
    
    /** 인증되지 않은 요청 에러 */
    UNAUTHORIZED("UNAUTHORIZED"),
    
    /** 권한이 없는 요청 에러 */
    FORBIDDEN("FORBIDDEN"),
    
    /** 검증 실패 에러 */
    VALIDATION_ERROR("VALIDATION_ERROR"),
    
    /** 잘못된 요청 에러 */
    BAD_REQUEST("BAD_REQUEST"),
    
    /** JSON 파싱 오류 */
    INVALID_JSON("INVALID_JSON"),
    
    /** 허용되지 않은 HTTP 메서드 에러 */
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED"),
    
    /** 지원하지 않는 미디어 타입 에러 */
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE"),
    
    /** 필수 파라미터 누락 에러 */
    MISSING_PARAMETER("MISSING_PARAMETER"),
    
    /** 타입 변환 실패 에러 */
    TYPE_MISMATCH("TYPE_MISMATCH"),
    
    /** 내부 서버 에러 */
    INTERNAL_ERROR("INTERNAL_ERROR"),
    
    // ================================
    // 도메인별 비즈니스 에러 코드
    // ================================
    
    /** 사용자명 중복 에러 */
    USER_DUPLICATE_USERNAME("USER_DUPLICATE_USERNAME"),
    
    /** 이메일 중복 에러 */
    USER_DUPLICATE_EMAIL("USER_DUPLICATE_EMAIL");

    /** 
     * 에러 코드 문자열 값입니다.
     * <p>클라이언트에게 전달되는 에러 식별자로 사용됩니다.</p>
     */
    private final String code;

    /**
     * ErrorCode 열거형 생성자입니다.
     * 
     * @param code 에러 코드 문자열
     */
    ErrorCode(final String code) {
        this.code = code;
    }

    /**
     * 에러 코드 문자열을 반환합니다.
     * 
     * @return 에러 코드 문자열
     */
    public String getCode() {
        return code;
    }
}