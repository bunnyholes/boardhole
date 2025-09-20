package dev.xiyo.bunnyholes.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;

@StandardException
public class DuplicateEmailException extends ConflictException {
    @Serial
    private static final long serialVersionUID = 4220929815126162123L;
}
