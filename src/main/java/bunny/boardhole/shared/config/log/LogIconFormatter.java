package bunny.boardhole.shared.config.log;

import org.springframework.stereotype.Component;

/**
 * 로그 메시지에 대한 아이콘 지정을 담당하는 전용 포맷터 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 아이콘 기능을 제공합니다:
 * <ul>
 *   <li>레이어별 아이콘 - Controller/Service/Repository 계층별 시각적 구분</li>
 *   <li>성능별 아이콘 - 응답시간에 따른 시각적 피드백</li>
 * </ul>
 * </p>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class LogIconFormatter {

    /**
     * 메소드 시그니처에 따라 적절한 레이어 아이콘을 반환합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @return 레이어에 해당하는 아이콘
     */
    public String getLayerIcon(final String signature) {
        String result = LogConstants.DEFAULT_ICON;
        if (signature.contains("Controller")) {
            result = LogConstants.CONTROLLER_ICON;
        } else if (signature.contains("Service")) {
            result = LogConstants.SERVICE_ICON;
        } else if (signature.contains("Repository")) {
            result = LogConstants.REPOSITORY_ICON;
        }
        return result;
    }

    /**
     * 응답시간에 따라 적절한 성능 아이콘을 반환합니다.
     *
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param loggingProperties 로깅 설정 프로퍼티 (final)
     * @return 성능에 해당하는 아이콘
     */
    public String getPerformanceIcon(final long responseTimeMs, final LoggingProperties loggingProperties) {
        String result = LogConstants.SLOW_ICON;
        if (loggingProperties.isFast(responseTimeMs)) {
            result = LogConstants.FAST_ICON;
        } else if (loggingProperties.isNormal(responseTimeMs)) {
            result = LogConstants.NORMAL_ICON;
        }
        return result;
    }

    /**
     * 성능 경고 여부를 확인합니다.
     *
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param loggingProperties 로깅 설정 프로퍼티 (final)
     * @return 느린 응답이면 true
     */
    public boolean shouldWarnPerformance(final long responseTimeMs, final LoggingProperties loggingProperties) {
        return loggingProperties.isSlow(responseTimeMs);
    }
}
