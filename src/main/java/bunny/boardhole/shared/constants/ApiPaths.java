package bunny.boardhole.shared.constants;

/**
 * REST API 경로 상수를 정의하는 유틸리티 클래스입니다.
 * Spring MVC Controller의 RequestMapping 어노테이션에서 사용되는 URL 경로들을 중앙 집중식으로 관리합니다.
 * 
 * <p>사용 예시:</p>
 * <pre>{@code
 * @RequestMapping(ApiPaths.AUTH)
 * public class AuthController {
 *     @PostMapping(ApiPaths.AUTH_LOGIN)
 *     public ResponseEntity<?> login() { ... }
 * }
 * }</pre>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass") // 상수 유틸리티 클래스는 DataClass 패턴이 정상
public final class ApiPaths {
    
    // ================================
    // API 기본 설정
    // ================================
    
    /** 
     * 모든 API의 기본 접두사입니다.
     * <p>예: {@code /api/auth}, {@code /api/users}</p>
     */
    public static final String API_PREFIX = "/api";

    // ================================
    // Controller 레벨 기본 경로
    // ================================
    
    /** 
     * 인증 관련 엔드포인트의 기본 경로입니다.
     * <p>AuthController의 @RequestMapping에서 사용됩니다.</p>
     * <p>실제 경로: {@value}</p>
     */
    public static final String AUTH = API_PREFIX + "/auth";
    
    /** 
     * 사용자 관련 엔드포인트의 기본 경로입니다.
     * <p>UserController의 @RequestMapping에서 사용됩니다.</p>
     * <p>실제 경로: {@value}</p>
     */
    public static final String USERS = API_PREFIX + "/users";
    
    /** 
     * 게시글 관련 엔드포인트의 기본 경로입니다.
     * <p>BoardController의 @RequestMapping에서 사용됩니다.</p>
     * <p>실제 경로: {@value}</p>
     */
    public static final String BOARDS = API_PREFIX + "/boards";
    
    /** 
     * 관리자 관련 엔드포인트의 기본 경로입니다.
     * <p>AdminController의 @RequestMapping에서 사용됩니다.</p>
     * <p>실제 경로: {@value}</p>
     */
    public static final String ADMIN = API_PREFIX + "/admin";

    // ================================
    // 인증(Authentication) 엔드포인트 경로
    // ================================
    
    /** 
     * 회원가입 엔드포인트 경로입니다.
     * <p>@PostMapping에서 사용: {@code @PostMapping(AUTH_SIGNUP)}</p>
     * <p>완전한 URL: {@code /api/auth/signup}</p>
     */
    public static final String AUTH_SIGNUP = "/signup";
    
    /** 
     * 로그인 엔드포인트 경로입니다.
     * <p>@PostMapping에서 사용: {@code @PostMapping(AUTH_LOGIN)}</p>
     * <p>완전한 URL: {@code /api/auth/login}</p>
     */
    public static final String AUTH_LOGIN = "/login";
    
    /** 
     * 로그아웃 엔드포인트 경로입니다.
     * <p>@PostMapping에서 사용: {@code @PostMapping(AUTH_LOGOUT)}</p>
     * <p>완전한 URL: {@code /api/auth/logout}</p>
     */
    public static final String AUTH_LOGOUT = "/logout";
    
    /** 
     * 관리자 전용 엔드포인트 경로입니다.
     * <p>관리자 권한이 필요한 테스트/데모 엔드포인트에 사용됩니다.</p>
     * <p>완전한 URL: {@code /api/auth/admin-only}</p>
     */
    public static final String AUTH_ADMIN_ONLY = "/admin-only";
    
    /** 
     * 사용자 접근 테스트 엔드포인트 경로입니다.
     * <p>일반 사용자 권한이 필요한 테스트/데모 엔드포인트에 사용됩니다.</p>
     * <p>완전한 URL: {@code /api/auth/user-access}</p>
     */
    public static final String AUTH_USER_ACCESS = "/user-access";
    
    /** 
     * 공개 접근 테스트 엔드포인트 경로입니다.
     * <p>인증이 필요하지 않은 테스트/데모 엔드포인트에 사용됩니다.</p>
     * <p>완전한 URL: {@code /api/auth/public-access}</p>
     */
    public static final String AUTH_PUBLIC_ACCESS = "/public-access";

    // ================================
    // 사용자(User) 엔드포인트 경로
    // ================================
    
    /** 
     * 현재 사용자 정보 조회 엔드포인트 경로입니다.
     * <p>@GetMapping에서 사용: {@code @GetMapping(USERS_ME)}</p>
     * <p>완전한 URL: {@code /api/users/me}</p>
     * <p>인증된 사용자의 본인 정보를 조회할 때 사용합니다.</p>
     */
    public static final String USERS_ME = "/me";
    
    /** 
     * ID로 특정 사용자 조회 엔드포인트 경로입니다.
     * <p>@GetMapping에서 사용: {@code @GetMapping(USER_BY_ID)}</p>
     * <p>완전한 URL: {@code /api/users/{id}}</p>
     * <p>경로 변수 {id}는 사용자의 고유 식별자입니다.</p>
     */
    public static final String USER_BY_ID = "/{id}";
    
    /** 
     * 사용자 비밀번호 변경 엔드포인트 경로입니다.
     * <p>@PutMapping에서 사용: {@code @PutMapping(USER_PASSWORD)}</p>
     * <p>완전한 URL: {@code /api/users/{id}/password}</p>
     * <p>경로 변수 {id}는 비밀번호를 변경할 사용자의 고유 식별자입니다.</p>
     */
    public static final String USER_PASSWORD = "/{id}/password";
    
    /** 
     * 이메일 인증 엔드포인트 경로입니다.
     * <p>@PostMapping에서 사용: {@code @PostMapping(USER_EMAIL_VERIFICATION)}</p>
     * <p>완전한 URL: {@code /api/users/{id}/email/verification}</p>
     * <p>사용자의 이메일 주소 인증을 처리합니다.</p>
     */
    public static final String USER_EMAIL_VERIFICATION = "/{id}/email/verification";
    
    /** 
     * 사용자 이메일 변경 엔드포인트 경로입니다.
     * <p>@PutMapping에서 사용: {@code @PutMapping(USER_EMAIL)}</p>
     * <p>완전한 URL: {@code /api/users/{id}/email}</p>
     * <p>경로 변수 {id}는 이메일을 변경할 사용자의 고유 식별자입니다.</p>
     */
    public static final String USER_EMAIL = "/{id}/email";

    // ================================
    // 게시글(Board) 엔드포인트 경로  
    // ================================
    
    /** 
     * ID로 특정 게시글 조회 엔드포인트 경로입니다.
     * <p>@GetMapping에서 사용: {@code @GetMapping(BOARD_BY_ID)}</p>
     * <p>완전한 URL: {@code /api/boards/{id}}</p>
     * <p>경로 변수 {id}는 게시글의 고유 식별자입니다.</p>
     */
    public static final String BOARD_BY_ID = "/{id}";

    // ================================
    // 관리자(Admin) 엔드포인트 경로
    // ================================
    
    /** 
     * 관리자 통계 조회 엔드포인트 경로입니다.
     * <p>@GetMapping에서 사용: {@code @GetMapping(ADMIN_STATS)}</p>
     * <p>완전한 URL: {@code /api/admin/stats}</p>
     * <p>시스템 통계 정보를 관리자에게 제공합니다.</p>
     */
    public static final String ADMIN_STATS = "/stats";

    /**
     * 유틸리티 클래스의 인스턴스화를 방지합니다.
     * @throws UnsupportedOperationException 인스턴스 생성을 시도할 때 발생
     */
    private ApiPaths() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}