package dev.xiyo.bunnyholes.boardhole.shared.constants;

import lombok.NoArgsConstructor;

/**
 * API 경로 상수
 * Controller의 RequestMapping에서 사용
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ApiPaths {
    public static final String AUTH_SIGNUP = "/signup";
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_LOGOUT = "/logout";
    public static final String AUTH_ADMIN_ONLY = "/admin-only";
    public static final String AUTH_USER_ACCESS = "/user-access";
    public static final String AUTH_PUBLIC_ACCESS = "/public-access";
    public static final String USERS_ME = "/me";
    // 기본 경로
    private static final String API_PREFIX = "/api";
    // 메인 엔드포인트 (RequestMapping용)
    public static final String AUTH = API_PREFIX + "/auth";
    public static final String USERS = API_PREFIX + "/users";
    public static final String BOARDS = API_PREFIX + "/boards";

}