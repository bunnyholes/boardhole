package bunny.boardhole.shared.config.log;

public final class LogConstants {

    // ANSI 색상 코드
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    // 레이어별 아이콘
    public static final String CONTROLLER_ICON = "🎮";
    public static final String SERVICE_ICON = "⚙️";
    public static final String REPOSITORY_ICON = "💾";
    public static final String DEFAULT_ICON = "🔄";
    // 성능 표시 아이콘
    public static final String FAST_ICON = "⚡";
    public static final String NORMAL_ICON = "⚠️";
    public static final String SLOW_ICON = "🐌";
    // 로그 키워드
    public static final String TRACE_ID_KEY = "traceId";
    // 민감정보 필드명
    public static final String[] SENSITIVE_FIELDS = {
            "password", "pwd", "secret", "token", "key", "credential"
    };

    private LogConstants() {
    }
}