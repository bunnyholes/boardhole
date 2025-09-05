package bunny.boardhole.shared.exception;

import org.springframework.context.MessageSource;

import lombok.experimental.StandardException;

@StandardException
public class ConflictException extends RuntimeException {
    // Architecture rule: exception classes must have MessageSource field
    protected transient MessageSource messageSource;
}
