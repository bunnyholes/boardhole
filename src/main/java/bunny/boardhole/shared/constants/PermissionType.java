package bunny.boardhole.shared.constants;

/**
 * 권한 타입 상수
 * Security 관련 클래스에서 사용
 */
public final class PermissionType {
    // 권한 작업 타입
    public static final String WRITE = "WRITE";
    public static final String DELETE = "DELETE";
    public static final String READ = "READ";

    // 권한 대상 타입
    public static final String TARGET_BOARD = "BOARD";
    public static final String TARGET_USER = "USER";

    private PermissionType() {
        // 인스턴스 생성 방지
    }
}