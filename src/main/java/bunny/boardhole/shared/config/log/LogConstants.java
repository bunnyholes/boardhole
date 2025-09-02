package bunny.boardhole.shared.config.log;

/**
 * 로깅 시스템 상수 정의 유틸리티 클래스.
 * <p>
 * ANSI 색상 코드, 계층별 아이콘, 성능 임계값, 민감정보 필드명 등을 포함합니다.
 * 모든 상수는 불변이며, ANSI 색상은 비지원 터미널에서 문자로 표시됩니다.
 * 민감정보 필드명 검사는 대소문자를 구분하지 않습니다.
 * </p>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass") // 상수 유틸리티 클래스는 DataClass 패턴이 정상
public final class LogConstants {

    // ANSI 색상 코드
    /** ANSI 색상 리셋 코드 */
    public static final String RESET = "\u001B[0m";
    /** 녹색 ANSI 코드 - 성공/정상 상태 */
    public static final String GREEN = "\u001B[32m";
    /** 파란색 ANSI 코드 - Controller 레이어 */
    public static final String BLUE = "\u001B[34m";
    /** 노란색 ANSI 코드 - 경고/주의 상태 */
    public static final String YELLOW = "\u001B[33m";
    /** 빨간색 ANSI 코드 - 에러/위험 상태 */
    public static final String RED = "\u001B[31m";
    /** 보라색 ANSI 코드 - Repository 레이어 */
    public static final String PURPLE = "\u001B[35m";
    /** 청록색 ANSI 코드 - Service 레이어 */
    public static final String CYAN = "\u001B[36m";
    /** 흰색 ANSI 코드 - 기본 텍스트 */
    public static final String WHITE = "\u001B[37m";
    // 레이어별 아이콘
    /** Controller 레이어를 나타내는 게임패드 아이콘 - 사용자 입력 처리 계층 표시 */
    public static final String CONTROLLER_ICON = "🎮";
    /** Service 레이어를 나타내는 톱니바퀴 아이콘 - 비즈니스 로직 처리 계층 표시 */
    public static final String SERVICE_ICON = "⚙️";
    /** Repository 레이어를 나타내는 디스크 아이콘 - 데이터 접근 계층 표시 */
    public static final String REPOSITORY_ICON = "💾";
    /** 기본 레이어를 나타내는 순환 아이콘 - 분류되지 않은 컴포넌트 표시 */
    public static final String DEFAULT_ICON = "🔄";
    // 성능 표시 아이콘
    /** 빠른 응답을 나타내는 번개 아이콘 - FAST_THRESHOLD 이하 응답시간 표시 */
    public static final String FAST_ICON = "⚡";
    /** 보통 응답을 나타내는 경고 아이콘 - NORMAL_THRESHOLD 이하 응답시간 표시 */
    public static final String NORMAL_ICON = "⚠️";
    /** 느린 응답을 나타내는 달팽이 아이콘 - NORMAL_THRESHOLD 초과 응답시간 표시 */
    public static final String SLOW_ICON = "🐌";
    // 요청 관련 아이콘
    /** HTTP 요청 시작을 나타내는 지구본 아이콘 */
    public static final String REQ_START_ICON = "🌐";
    /** HTTP 요청 완료를 나타내는 체크무늬 깃발 아이콘 */
    public static final String REQ_END_ICON = "🏁";
    /** 메서드 호출 시작을 나타내는 받은편지함 아이콘 */
    public static final String METHOD_START_ICON = "📥";
    /** 메서드 호출 완료를 나타내는 보낸편지함 아이콘 */
    public static final String METHOD_END_ICON = "📤";
    /** 에러 발생을 나타내는 X 아이콘 */
    public static final String ERROR_ICON = "❌";
    // 성능 임계값 (ms)
    /** 빠른 응답 임계값 (밀리초) - 이 값 이하면 FAST로 분류 */
    public static final long FAST_THRESHOLD = 100L;
    /** 보통 응답 임계값 (밀리초) - 이 값 이하면 NORMAL로 분류 */
    public static final long NORMAL_THRESHOLD = 500L;
    
    // 로그 키워드
    /** MDC에서 사용하는 추적 ID 키 - 요청 전체에 걸쳐 로그를 연결하는 식별자 */
    public static final String TRACE_ID_KEY = "traceId";
    
    // 민감정보 필드명
    /** 
     * 로그 출력 시 마스킹해야 하는 민감정보 필드명 목록.
     * <p>대소문자 구분없이 필드명에 포함되어 있으면 마스킹 처리됩니다.</p>
     * <p>CRLF 인젝션 공격 방지를 위해 이 필드들은 특별히 보호됩니다.</p>
     * <p>불변 컬렉션으로 제공되어 외부에서 수정할 수 없습니다.</p>
     */
    public static final java.util.Set<String> SENSITIVE_FIELDS = java.util.Set.of(
            "password", "pwd", "secret", "token", "key", "credential"
    );

    /**
     * 유틸리티 클래스의 인스턴스화를 방지합니다.
     * <p>이 클래스는 정적 상수만을 제공하므로 인스턴스 생성이 불필요합니다.</p>
     * 
     * @throws UnsupportedOperationException 인스턴스 생성을 시도할 때 발생
     */
    private LogConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}