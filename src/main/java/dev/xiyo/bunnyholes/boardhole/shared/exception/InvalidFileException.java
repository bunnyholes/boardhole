package dev.xiyo.bunnyholes.boardhole.shared.exception;

import java.io.Serial;

import lombok.experimental.StandardException;

@StandardException
public class InvalidFileException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4215980200288868673L;
}
