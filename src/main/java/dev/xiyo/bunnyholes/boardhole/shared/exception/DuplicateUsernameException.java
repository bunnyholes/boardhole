package dev.xiyo.bunnyholes.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;

@StandardException
public class DuplicateUsernameException extends ConflictException {
    @Serial
    private static final long serialVersionUID = 9179374908205298201L;
}
