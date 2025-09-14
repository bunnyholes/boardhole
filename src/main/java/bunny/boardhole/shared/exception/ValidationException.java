package bunny.boardhole.shared.exception;

import java.io.Serial;

/**
 * 유효성 검증 실패 예외
 */
public class ValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 793805120125587994L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
