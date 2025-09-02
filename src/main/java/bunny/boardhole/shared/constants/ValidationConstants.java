package bunny.boardhole.shared.constants;

/**
 * 검증 관련 상수를 정의하는 유틸리티 클래스입니다.
 * Spring Validation 어노테이션에서 사용하기 위한 컴파일 타임 상수를 제공합니다.
 * 
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Size(max = ValidationConstants.BOARD_TITLE_MAX_LENGTH)
 * private String title;
 * }</pre>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass") // 상수 유틸리티 클래스는 DataClass 패턴이 정상
public final class ValidationConstants {
    
    // ================================
    // 게시글(Board) 검증 상수
    // ================================
    
    /** 
     * 게시글 제목의 최대 길이입니다.
     * <p>@Size 어노테이션의 max 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>이 제한은 데이터베이스의 board.title 컬럼 길이와 일치해야 합니다.</p>
     */
    public static final int BOARD_TITLE_MAX_LENGTH = 200;
    
    /** 
     * 게시글 내용의 최대 길이입니다.
     * <p>@Size 어노테이션의 max 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>이 제한은 데이터베이스의 board.content 컬럼 길이와 일치해야 합니다.</p>
     */
    public static final int BOARD_CONTENT_MAX_LENGTH = 10_000;

    // ================================
    // 사용자(User) 검증 상수 - 사용자명
    // ================================
    
    /** 
     * 사용자명(username)의 최소 길이입니다.
     * <p>@Size 어노테이션의 min 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>너무 짧은 사용자명을 방지하여 시스템의 일관성을 유지합니다.</p>
     */
    public static final int USER_USERNAME_MIN_LENGTH = 3;
    
    /** 
     * 사용자명(username)의 최대 길이입니다.
     * <p>@Size 어노테이션의 max 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>이 제한은 데이터베이스의 users.username 컬럼 길이와 일치해야 합니다.</p>
     */
    public static final int USER_USERNAME_MAX_LENGTH = 20;
    
    // ================================
    // 사용자(User) 검증 상수 - 비밀번호
    // ================================
    
    /** 
     * 사용자 비밀번호의 최소 길이입니다.
     * <p>@Size 어노테이션의 min 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>보안 강화를 위해 최소 8자 이상을 요구합니다.</p>
     */
    public static final int USER_PASSWORD_MIN_LENGTH = 8;
    
    /** 
     * 사용자 비밀번호의 최대 길이입니다.
     * <p>@Size 어노테이션의 max 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>해시화 이전의 원본 비밀번호 길이 제한입니다.</p>
     */
    public static final int USER_PASSWORD_MAX_LENGTH = 100;
    
    /** 
     * 비밀번호 유효성 검증 정규표현식 패턴입니다.
     * 요구사항: 대소문자, 숫자, 특수문자(@$!%*?&) 조합, 최소 8자
     */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    // ================================
    // 사용자(User) 검증 상수 - 기타 정보
    // ================================
    
    /** 
     * 사용자 이메일 주소의 최대 길이입니다.
     * <p>@Size 어노테이션의 max 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>RFC 5321 표준에 따른 이메일 주소 최대 길이를 고려한 값입니다.</p>
     * <p>이 제한은 데이터베이스의 users.email 컬럼 길이와 일치해야 합니다.</p>
     */
    public static final int USER_EMAIL_MAX_LENGTH = 255;
    
    /** 
     * 사용자 실명(name)의 최소 길이입니다.
     * <p>@Size 어노테이션의 min 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>최소 한 글자 이상의 이름을 요구합니다.</p>
     */
    public static final int USER_NAME_MIN_LENGTH = 1;
    
    /** 
     * 사용자 실명(name)의 최대 길이입니다.
     * <p>@Size 어노테이션의 max 속성에서 사용됩니다.</p>
     * <p>값: {@value}자</p>
     * <p>일반적인 한국어/영어 이름 길이를 고려한 값입니다.</p>
     * <p>이 제한은 데이터베이스의 users.name 컬럼 길이와 일치해야 합니다.</p>
     */
    public static final int USER_NAME_MAX_LENGTH = 50;

    /**
     * 유틸리티 클래스의 인스턴스화를 방지합니다.
     * @throws UnsupportedOperationException 인스턴스 생성을 시도할 때 발생
     */
    private ValidationConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}