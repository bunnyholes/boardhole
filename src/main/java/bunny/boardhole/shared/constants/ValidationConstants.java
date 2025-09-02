package bunny.boardhole.shared.constants;

/**
 * 검증 관련 상수
 * 어노테이션에서 사용하기 위한 컴파일 타임 상수
 * 실제 검증 로직에서는 ValidationProperties 사용 권장
 */
public final class ValidationConstants {
    // Board 검증 상수
    public static final int BOARD_TITLE_MAX_LENGTH = 200;
    public static final int BOARD_CONTENT_MAX_LENGTH = 10000;

    // User 검증 상수
    public static final int USER_USERNAME_MIN_LENGTH = 3;
    public static final int USER_USERNAME_MAX_LENGTH = 20;
    public static final int USER_PASSWORD_MIN_LENGTH = 8;
    public static final int USER_PASSWORD_MAX_LENGTH = 100;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final int USER_EMAIL_MAX_LENGTH = 255;
    public static final int USER_NAME_MIN_LENGTH = 1;
    public static final int USER_NAME_MAX_LENGTH = 50;

    private ValidationConstants() {
        // 인스턴스 생성 방지
    }
}