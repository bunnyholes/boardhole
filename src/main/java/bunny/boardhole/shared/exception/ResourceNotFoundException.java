package bunny.boardhole.shared.exception;

import lombok.experimental.StandardException;

import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;

@StandardException
public class ResourceNotFoundException extends RuntimeException {
    // Architecture rule: exception classes must have MessageSource field
    @Nullable
    protected transient MessageSource messageSource = null;
}
