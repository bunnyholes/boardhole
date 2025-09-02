package bunny.boardhole.shared.config.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 로깅 메시지 포맷팅의 중심 조정자 역할을 하는 파사드 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 전문 포맷터들을 조합하여 포맷팅 기능을 제공합니다:
 * <ul>
 *   <li>{@link LogPatternFormatter} - 메서드/요청 로그 패턴 포맷팅</li>
 *   <li>{@link LogColorFormatter} - 계층별/상태별 색상 지정</li>
 *   <li>{@link LogIconFormatter} - 계층별/성능별 아이콘 표시</li>
 *   <li>{@link LogObjectFormatter} - 안전한 객체 변환 및 보안 처리</li>
 * </ul>
 * </p>
 *
 * <p>주요 개선사항:</p>
 * <ul>
 *   <li>단일 책임 원칙 적용을 통한 복잡도 감소</li>
 *   <li>전문 포맷터 분리를 통한 유지보수성 향상</li>
 *   <li>의존성 주입을 통한 느슨한 결합 구현</li>
 * </ul>
 *
 * @author BoardHole Development Team
 * @version 2.0
 * @since 1.0
 * @see LogPatternFormatter
 * @see LogColorFormatter
 * @see LogIconFormatter
 * @see LogObjectFormatter
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "boardhole.logging.enabled", havingValue = "true", matchIfMissing = true)
public class LogFormatter {

    /** 로그 패턴 포맷터 - 메시지 패턴 형식화 담당 */
    private final LogPatternFormatter patternFormatter;
    
    /** 로그 색상 포맷터 - 색상 지정 담당 */
    private final LogColorFormatter colorFormatter;
    
    /** 로그 아이콘 포맷터 - 아이콘 표시 담당 */
    private final LogIconFormatter iconFormatter;
    
    /** 로그 객체 포맷터 - 안전한 객체 변환 담당 */
    private final LogObjectFormatter objectFormatter;
    
    /** 로깅 성능 설정 프로퍼티 */
    private final LoggingProperties loggingProperties;

    /**
     * LogFormatter를 생성합니다.
     *
     * @param patternFormatter 로그 패턴 포맷터 (final)
     * @param colorFormatter 로그 색상 포맷터 (final)
     * @param iconFormatter 로그 아이콘 포맷터 (final)
     * @param objectFormatter 로그 객체 포맷터 (final)
     * @param loggingProperties 로깅 성능 설정 프로퍼티 (final)
     */
    public LogFormatter(final LogPatternFormatter patternFormatter,
                        final LogColorFormatter colorFormatter,
                        final LogIconFormatter iconFormatter,
                        final LogObjectFormatter objectFormatter,
                        final LoggingProperties loggingProperties) {
        this.patternFormatter = patternFormatter;
        this.colorFormatter = colorFormatter;
        this.iconFormatter = iconFormatter;
        this.objectFormatter = objectFormatter;
        this.loggingProperties = loggingProperties;
    }

    /**
     * 메서드 시작 로그 메시지를 포맷팅합니다.
     *
     * @param signature 메서드 시그니처 (final)
     * @param args 메서드 인자들 (final)
     * @return 포맷팅된 메서드 시작 로그 메시지
     */
    public String formatMethodStart(final String signature, final Object... args) {
        return patternFormatter.formatMethodStart(signature, args);
    }

    /**
     * 메서드 종료 로그 메시지를 포맷팅합니다.
     *
     * @param signature 메서드 시그니처 (final)
     * @param result 메서드 결과 (final)
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @return 포맷팅된 메서드 종료 로그 메시지
     */
    public String formatMethodEnd(final String signature, final Object result, final long responseTimeMs) {
        return patternFormatter.formatMethodEnd(signature, result, responseTimeMs, loggingProperties);
    }

    /**
     * 메서드 에러 로그 메시지를 포맷팅합니다.
     *
     * @param signature 메서드 시그니처 (final)
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param errorMessage 에러 메시지 (final)
     * @return 포맷팅된 에러 로그 메시지
     */
    public String formatMethodError(final String signature, final long responseTimeMs, final String errorMessage) {
        return patternFormatter.formatMethodError(signature, responseTimeMs, errorMessage);
    }

    /**
     * HTTP 요청 시작 로그 메시지를 포맷팅합니다.
     *
     * @param method HTTP 메소드 (final)
     * @param uri 요청 URI (final)
     * @param remoteAddr 원격 주소 (final)
     * @return 포맷팅된 요청 시작 로그 메시지
     */
    public String formatRequestStart(final String method, final String uri, final String remoteAddr) {
        return patternFormatter.formatRequestStart(method, uri, remoteAddr);
    }

    /**
     * HTTP 요청 종료 로그 메시지를 포맷팅합니다.
     *
     * @param method HTTP 메소드 (final)
     * @param uri 요청 URI (final)
     * @param status HTTP 상태코드 (final)
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @return 포맷팅된 요청 종료 로그 메시지
     */
    public String formatRequestEnd(final String method, final String uri, final int status, final long responseTimeMs) {
        return patternFormatter.formatRequestEnd(method, uri, status, responseTimeMs, loggingProperties);
    }

    /**
     * 메소드 시그니처에 따라 적절한 레이어 색상을 반환합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @return 레이어에 해당하는 ANSI 색상 코드
     */
    public String getLayerColor(final String signature) {
        return colorFormatter.getLayerColor(signature);
    }

    /**
     * 메소드 시그니처에 따라 적절한 레이어 아이콘을 반환합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @return 레이어에 해당하는 아이콘
     */
    public String getLayerIcon(final String signature) {
        return iconFormatter.getLayerIcon(signature);
    }

    /**
     * 응답시간에 따라 적절한 성능 색상을 반환합니다.
     *
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @return 성능에 해당하는 ANSI 색상 코드
     */
    public String getPerformanceColor(final long responseTimeMs) {
        return colorFormatter.getPerformanceColor(responseTimeMs, loggingProperties);
    }

    /**
     * 응답시간에 따라 적절한 성능 아이콘을 반환합니다.
     *
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @return 성능에 해당하는 아이콘
     */
    public String getPerformanceIcon(final long responseTimeMs) {
        return iconFormatter.getPerformanceIcon(responseTimeMs, loggingProperties);
    }

    /**
     * 성능 경고 여부를 확인합니다.
     *
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @return 느린 응답이면 true
     */
    public boolean shouldWarnPerformance(final long responseTimeMs) {
        return iconFormatter.shouldWarnPerformance(responseTimeMs, loggingProperties);
    }

    /**
     * HTTP 상태코드에 따라 적절한 상태 색상을 반환합니다.
     *
     * @param status HTTP 상태코드 (final)
     * @return 상태에 해당하는 ANSI 색상 코드
     */
    public String getStatusColor(final int status) {
        return colorFormatter.getStatusColor(status);
    }

    /**
     * 객체를 안전하게 문자열로 변환합니다.
     *
     * @param arg 변환할 객체 (final)
     * @return 안전하게 변환된 문자열
     */
    public String safeToString(final Object arg) {
        return objectFormatter.safeToString(arg);
    }

    /**
     * 결과 객체를 안전하게 문자열로 변환합니다.
     *
     * @param result 변환할 결과 객체 (final)
     * @return 안전하게 변환된 결과 문자열
     */
    public String safeResultToString(final Object result) {
        return objectFormatter.safeResultToString(result);
    }


    /**
     * 로깅 예외를 안전하게 처리하여 로그 포맷팅 오류가 비즈니스 로직에 영향을 주지 않도록 합니다.
     * <p>
     * 이 메서드는 다음과 같은 기능을 제공합니다:
     * <ul>
     *   <li>메시지 포맷팅 예외 처리 - 포맷팅 실패 시 원본 메시지 반환</li>
     *   <li>인자 안전 변환 - 모든 인자를 안전한 문자열로 변환</li>
     *   <li>CRLF 인젝션 방지 - 개행문자 등 악성 입력 차단</li>
     *   <li>성능 최적화 - 로그 레벨 가드를 통한 불필요한 처리 방지</li>
     * </ul>
     * </p>
     * 
     * @param message 포맷팅할 메시지 템플릿 (null 허용) (final)
     * @param args 메시지에 삽입할 인자들 (null 허용) (final)
     * @return 안전하게 포맷팅된 로그 메시지
     */
    public String safeLog(final String message, final Object... args) {
        return objectFormatter.safeLog(message, args);
    }
}
