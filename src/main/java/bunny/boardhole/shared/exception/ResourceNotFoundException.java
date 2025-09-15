package bunny.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;


@StandardException
public class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3895458239875733961L;
}
