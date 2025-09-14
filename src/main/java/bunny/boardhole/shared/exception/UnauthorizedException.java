package bunny.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;

import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;

@StandardException
public class UnauthorizedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 3166168243664239212L;
    @Nullable
    protected transient MessageSource messageSource = null;
}
