package dev.xiyo.bunnyholes.boardhole.reply.domain.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplyValidationConstants {

    public static final int CONTENT_MIN_LENGTH = 1;
    public static final int CONTENT_MAX_LENGTH = 2000;
    public static final int MAX_DEPTH = 5;

}
