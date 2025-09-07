package bunny.boardhole.shared.exception;

/**
 * 이메일 인증이 필요할 때 발생하는 예외
 */
public class EmailVerificationRequiredException extends RuntimeException {
    
    public EmailVerificationRequiredException(String message) {
        super(message);
    }
    
    public EmailVerificationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}