package bunny.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;


@StandardException
public class UnauthorizedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 3166168243664239212L;
}
