package bunny.boardhole.user.domain.validation;

import lombok.NoArgsConstructor;

/**
 * User 검증 관련 상수
 * 어노테이션에서 사용하기 위한 컴파일 타임 상수
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class UserValidationConstants {
    // User 검증 상수
    public static final int USER_USERNAME_MIN_LENGTH = 3;
    public static final int USER_USERNAME_MAX_LENGTH = 20;
    public static final int USER_PASSWORD_MIN_LENGTH = 8;
    public static final int USER_PASSWORD_MAX_LENGTH = 100;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final int USER_EMAIL_MAX_LENGTH = 255;
    public static final int USER_NAME_MIN_LENGTH = 1;
    public static final int USER_NAME_MAX_LENGTH = 50;
}