package bunny.boardhole.shared.constants;

/**
 * API 경로 상수
 * Controller의 RequestMapping에서 사용
 */
public final class ApiPaths {
    // 기본 경로
    public static final String API_PREFIX = "/api";

    // 메인 엔드포인트 (RequestMapping용)
    public static final String AUTH = API_PREFIX + "/auth";
    public static final String USERS = API_PREFIX + "/users";
    public static final String BOARDS = API_PREFIX + "/boards";

    // Auth 경로
    public static final String AUTH_BASE = API_PREFIX + "/auth";
    public static final String AUTH_SIGNUP = "/signup";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_ADMIN_ONLY = "/admin-only";
    public static final String AUTH_USER_ACCESS = "/user-access";
    public static final String AUTH_PUBLIC_ACCESS = "/public-access";

    // User 경로
    public static final String USER_BASE = API_PREFIX + "/users";
    public static final String USERS_ME = "/me";
    public static final String USER_ME = "/me";
    public static final String USER_BY_ID = "/{id}";
    public static final String USER_PASSWORD = "/{id}/password";
    public static final String USER_EMAIL_VERIFICATION = "/{id}/email/verification";
    public static final String USER_EMAIL = "/{id}/email";

    // Board 경로
    public static final String BOARD_BASE = API_PREFIX + "/boards";
    public static final String BOARD_BY_ID = "/{id}";


    private ApiPaths() {
        // 인스턴스 생성 방지
    }
}