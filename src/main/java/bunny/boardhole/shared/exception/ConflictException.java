package bunny.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;

@StandardException
class ConflictException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5270944463690949893L;
}
