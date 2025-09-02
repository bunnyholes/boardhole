package bunny.boardhole.shared.constants;

/**
 * 도메인 엔티티 검증을 위한 메시지 키 상수
 * 다국어 지원을 위해 하드코딩된 메시지 대신 메시지 키를 사용합니다.
 */
public final class ValidationMessages {

    // User entity validation messages
    public static final String USER_USERNAME_REQUIRED = "validation.user.username.required";
    public static final String USER_USERNAME_TOO_LONG = "validation.user.username.too-long";
    public static final String USER_PASSWORD_REQUIRED = "validation.user.password.required";
    public static final String USER_NAME_REQUIRED = "validation.user.name.required";
    public static final String USER_NAME_TOO_LONG = "validation.user.name.too-long";
    public static final String USER_EMAIL_REQUIRED = "validation.user.email.required";
    public static final String USER_EMAIL_TOO_LONG = "validation.user.email.too-long";
    // Board entity validation messages
    public static final String BOARD_TITLE_REQUIRED = "validation.board.title.required";
    public static final String BOARD_TITLE_TOO_LONG = "validation.board.title.too-long";
    public static final String BOARD_CONTENT_REQUIRED = "validation.board.content.required";
    public static final String BOARD_CONTENT_TOO_LONG = "validation.board.content.too-long";
    public static final String BOARD_AUTHOR_REQUIRED = "validation.board.author.required";
    // EmailVerification entity validation messages
    public static final String EMAIL_VERIFICATION_CODE_REQUIRED = "validation.email-verification.code.required";
    public static final String EMAIL_VERIFICATION_USER_ID_REQUIRED = "validation.email-verification.user-id.required";
    public static final String EMAIL_VERIFICATION_NEW_EMAIL_REQUIRED = "validation.email-verification.new-email.required";
    public static final String EMAIL_VERIFICATION_EXPIRES_AT_REQUIRED = "validation.email-verification.expires-at.required";
    public static final String EMAIL_VERIFICATION_TYPE_REQUIRED = "validation.email-verification.type.required";
    // Fallback messages (English)
    public static final String USER_USERNAME_REQUIRED_FALLBACK = "Username is required";
    public static final String USER_USERNAME_TOO_LONG_FALLBACK = "Username cannot exceed %d characters";
    public static final String USER_PASSWORD_REQUIRED_FALLBACK = "Password is required";
    public static final String USER_NAME_REQUIRED_FALLBACK = "Name is required";
    public static final String USER_NAME_TOO_LONG_FALLBACK = "Name cannot exceed %d characters";
    public static final String USER_EMAIL_REQUIRED_FALLBACK = "Email is required";
    public static final String USER_EMAIL_TOO_LONG_FALLBACK = "Email cannot exceed %d characters";
    public static final String BOARD_TITLE_REQUIRED_FALLBACK = "Title is required";
    public static final String BOARD_TITLE_TOO_LONG_FALLBACK = "Title cannot exceed %d characters";
    public static final String BOARD_CONTENT_REQUIRED_FALLBACK = "Content is required";
    public static final String BOARD_CONTENT_TOO_LONG_FALLBACK = "Content cannot exceed %d characters";
    public static final String BOARD_AUTHOR_REQUIRED_FALLBACK = "Author is required";
    public static final String EMAIL_VERIFICATION_CODE_REQUIRED_FALLBACK = "Verification code is required";
    public static final String EMAIL_VERIFICATION_USER_ID_REQUIRED_FALLBACK = "User ID is required";
    public static final String EMAIL_VERIFICATION_NEW_EMAIL_REQUIRED_FALLBACK = "New email is required";
    public static final String EMAIL_VERIFICATION_EXPIRES_AT_REQUIRED_FALLBACK = "Expiration time is required";
    public static final String EMAIL_VERIFICATION_TYPE_REQUIRED_FALLBACK = "Verification type is required";
    private ValidationMessages() {
        throw new UnsupportedOperationException("Constants class");
    }
}