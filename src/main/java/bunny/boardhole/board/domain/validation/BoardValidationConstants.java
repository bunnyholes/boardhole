package bunny.boardhole.board.domain.validation;

import lombok.NoArgsConstructor;

/**
 * Board 검증 관련 상수
 * 어노테이션에서 사용하기 위한 컴파일 타임 상수
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BoardValidationConstants {
    // Board 검증 상수
    public static final int BOARD_TITLE_MAX_LENGTH = 200;
    public static final int BOARD_CONTENT_MAX_LENGTH = 10000;
}