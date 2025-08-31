package bunny.boardhole.common.exception;

public class DuplicateUsernameException extends ConflictException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}

