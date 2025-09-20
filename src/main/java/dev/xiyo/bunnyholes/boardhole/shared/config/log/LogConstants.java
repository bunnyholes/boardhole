package dev.xiyo.bunnyholes.boardhole.shared.config.log;

final class LogConstants {

    // ANSI 색상 코드
    static final String RESET = "\u001B[0m";
    static final String GREEN = "\u001B[32m";
    static final String BLUE = "\u001B[34m";
    static final String YELLOW = "\u001B[33m";
    static final String RED = "\u001B[31m";
    static final String PURPLE = "\u001B[35m";
    static final String CYAN = "\u001B[36m";
    // 레이어별 아이콘
    static final String CONTROLLER_ICON = "🎮";
    static final String SERVICE_ICON = "⚙️";
    static final String REPOSITORY_ICON = "💾";
    static final String DEFAULT_ICON = "🔄";
    // 성능 표시 아이콘
    static final String FAST_ICON = "⚡";
    static final String NORMAL_ICON = "⚠️";
    static final String SLOW_ICON = "🐌";
    // 로그 키워드
    static final String TRACE_ID_KEY = "traceId";
    // 민감정보 필드명
    static final String[] SENSITIVE_FIELDS = {"password", "pwd", "secret", "token", "key", "credential"};

    private LogConstants() {
    }
}