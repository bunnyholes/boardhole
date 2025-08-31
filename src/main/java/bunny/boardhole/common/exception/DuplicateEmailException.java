package bunny.boardhole.common.exception;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}

