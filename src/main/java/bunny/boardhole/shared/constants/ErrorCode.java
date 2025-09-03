package bunny.boardhole.shared.constants;

import lombok.Getter;

/**
 * 에러 코드 열거형
 * GlobalExceptionHandler에서 사용
 */
@Getter
public enum ErrorCode {
    CONFLICT("CONFLICT"),
    UNAUTHORIZED("UNAUTHORIZED"),
    FORBIDDEN("FORBIDDEN"),
    VALIDATION_ERROR("VALIDATION_ERROR"),
    BAD_REQUEST("BAD_REQUEST"),
    INVALID_JSON("INVALID_JSON"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE"),
    MISSING_PARAMETER("MISSING_PARAMETER"),
    TYPE_MISMATCH("TYPE_MISMATCH"),
    INTERNAL_ERROR("INTERNAL_ERROR"),
    USER_DUPLICATE_USERNAME("USER_DUPLICATE_USERNAME"),
    USER_DUPLICATE_EMAIL("USER_DUPLICATE_EMAIL");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }
}