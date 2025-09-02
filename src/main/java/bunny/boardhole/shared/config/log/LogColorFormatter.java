package bunny.boardhole.shared.config.log;

import org.springframework.stereotype.Component;

/**
 * 로그 메시지에 대한 색상 지정을 담당하는 전용 포맷터 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 색상 기능을 제공합니다:
 * <ul>
 *   <li>레이어별 색상 - Controller/Service/Repository 계층별 구분</li>
 *   <li>성능별 색상 - 응답시간에 따른 시각적 피드백</li>
 *   <li>상태별 색상 - HTTP 상태코드에 따른 색상 구분</li>
 * </ul>
 * </p>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class LogColorFormatter {

    /** HTTP 상태코드 범위 상수 */
    private static final int SUCCESS_STATUS_MIN = 200;
    private static final int SUCCESS_STATUS_MAX = 300;
    private static final int REDIRECTION_STATUS_MIN = 300;
    private static final int REDIRECTION_STATUS_MAX = 400;
    private static final int CLIENT_ERROR_STATUS_MIN = 400;
    private static final int CLIENT_ERROR_STATUS_MAX = 500;
    private static final int SERVER_ERROR_STATUS_MIN = 500;

    /**
     * 메소드 시그니처에 따라 적절한 레이어 색상을 반환합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @return 레이어에 해당하는 ANSI 색상 코드
     */
    public String getLayerColor(final String signature) {
        String result = LogConstants.RESET;
        if (signature.contains("Controller")) {
            result = LogConstants.BLUE;
        } else if (signature.contains("Service")) {
            result = LogConstants.CYAN;
        } else if (signature.contains("Repository")) {
            result = LogConstants.PURPLE;
        }
        return result;
    }

    /**
     * 응답시간에 따라 적절한 성능 색상을 반환합니다.
     *
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param loggingProperties 로깅 설정 프로퍼티 (final)
     * @return 성능에 해당하는 ANSI 색상 코드
     */
    public String getPerformanceColor(final long responseTimeMs, final LoggingProperties loggingProperties) {
        String result = LogConstants.RED;
        if (loggingProperties.isFast(responseTimeMs)) {
            result = LogConstants.GREEN;
        } else if (loggingProperties.isNormal(responseTimeMs)) {
            result = LogConstants.YELLOW;
        }
        return result;
    }

    /**
     * HTTP 상태코드에 따라 적절한 상태 색상을 반환합니다.
     *
     * @param status HTTP 상태코드 (final)
     * @return 상태에 해당하는 ANSI 색상 코드
     */
    public String getStatusColor(final int status) {
        String result = LogConstants.RESET;
        if (isSuccessStatus(status)) {
            result = LogConstants.GREEN;
        } else if (isRedirectionStatus(status)) {
            result = LogConstants.YELLOW;
        } else if (isClientErrorStatus(status)) {
            result = LogConstants.RED;
        } else if (isServerErrorStatus(status)) {
            result = LogConstants.PURPLE;
        }
        return result;
    }

    /**
     * 2xx 성공 상태코드인지 확인합니다.
     *
     * @param status HTTP 상태코드 (final)
     * @return 성공 상태코드이면 true
     */
    private boolean isSuccessStatus(final int status) {
        return status >= SUCCESS_STATUS_MIN && status < SUCCESS_STATUS_MAX;
    }

    /**
     * 3xx 리다이렉션 상태코드인지 확인합니다.
     *
     * @param status HTTP 상태코드 (final)
     * @return 리다이렉션 상태코드이면 true
     */
    private boolean isRedirectionStatus(final int status) {
        return status >= REDIRECTION_STATUS_MIN && status < REDIRECTION_STATUS_MAX;
    }

    /**
     * 4xx 클라이언트 에러 상태코드인지 확인합니다.
     *
     * @param status HTTP 상태코드 (final)
     * @return 클라이언트 에러 상태코드이면 true
     */
    private boolean isClientErrorStatus(final int status) {
        return status >= CLIENT_ERROR_STATUS_MIN && status < CLIENT_ERROR_STATUS_MAX;
    }

    /**
     * 5xx 서버 에러 상태코드인지 확인합니다.
     *
     * @param status HTTP 상태코드 (final)
     * @return 서버 에러 상태코드이면 true
     */
    private boolean isServerErrorStatus(final int status) {
        return status >= SERVER_ERROR_STATUS_MIN;
    }
}
