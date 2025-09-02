package bunny.boardhole.shared.constants;

/**
 * 권한 타입 상수를 정의하는 유틸리티 클래스입니다.
 * Spring Security의 PermissionEvaluator에서 사용되는 권한 및 대상 타입 상수를 정의합니다.
 * 
 * <p>사용 예시:</p>
 * <pre>{@code
 * @PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE')")
 * public void updateBoard(Long boardId) { ... }
 * }</pre>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass") // 상수 유틸리티 클래스는 DataClass 패턴이 정상
public final class PermissionType {
    
    // ================================
    // 권한 타입 상수
    // ================================
    
    /** 
     * 쓰기 권한 상수입니다.
     * <p>리소스를 수정할 수 있는 권한을 나타냅니다.</p>
     * <p>값: {@value}</p>
     */
    public static final String WRITE = "WRITE";
    
    /** 
     * 삭제 권한 상수입니다.
     * <p>리소스를 삭제할 수 있는 권한을 나타냅니다.</p>
     * <p>값: {@value}</p>
     */
    public static final String DELETE = "DELETE";
    
    /** 
     * 읽기 권한 상수입니다.
     * <p>리소스를 조회할 수 있는 권한을 나타냅니다.</p>
     * <p>값: {@value}</p>
     */
    public static final String READ = "READ";

    // ================================
    // 대상 타입 상수
    // ================================
    
    /** 
     * 게시글 대상 타입 상수입니다.
     * <p>게시글 리소스에 대한 권한 검사에서 사용됩니다.</p>
     * <p>값: {@value}</p>
     */
    public static final String TARGET_BOARD = "BOARD";
    
    /** 
     * 사용자 대상 타입 상수입니다.
     * <p>사용자 리소스에 대한 권한 검사에서 사용됩니다.</p>
     * <p>값: {@value}</p>
     */
    public static final String TARGET_USER = "USER";

    /**
     * 유틸리티 클래스의 인스턴스화를 방지합니다.
     * @throws UnsupportedOperationException 인스턴스 생성을 시도할 때 발생
     */
    private PermissionType() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}