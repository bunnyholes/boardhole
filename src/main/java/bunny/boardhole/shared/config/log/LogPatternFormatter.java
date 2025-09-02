package bunny.boardhole.shared.config.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 로그 메시지의 패턴 포맷팅을 담당하는 전용 포맷터 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 패턴 포맷팅 기능을 제공합니다:
 * <ul>
 *   <li>메소드 시작/종료 로깅 패턴</li>
 *   <li>HTTP 요청/응답 로깅 패턴</li>
 *   <li>에러 로깅 패턴</li>
 *   <li>다국어 메시지 지원</li>
 * </ul>
 * </p>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class LogPatternFormatter {

    /** 다국어 메시지 지원을 위한 MessageSource */
    private final MessageSource messageSource;
    /** 로깅 색상 포맷터 */
    private final LogColorFormatter colorFormatter;
    /** 로깅 아이콘 포맷터 */
    private final LogIconFormatter iconFormatter;
    /** 로깅 색상 포맷터 */
    private final LogObjectFormatter objectFormatter;

    /**
     * LogPatternFormatter를 생성합니다.
     *
     * @param messageSource 다국어 메시지 소스 (final)
     * @param colorFormatter 로깅 색상 포맷터 (final)
     * @param iconFormatter 로깅 아이콘 포맷터 (final)
     * @param objectFormatter 로깅 객체 포맷터 (final)
     */
    public LogPatternFormatter(final MessageSource messageSource,
                               final LogColorFormatter colorFormatter,
                               final LogIconFormatter iconFormatter,
                               final LogObjectFormatter objectFormatter) {
        this.messageSource = messageSource;
        this.colorFormatter = colorFormatter;
        this.iconFormatter = iconFormatter;
        this.objectFormatter = objectFormatter;
    }

    /**
     * 메소드 시작 로그 메시지를 포맷팅합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @param args 메소드 인자들 (final)
     * @return 포맷팅된 메소드 시작 로그 메시지
     */
    public String formatMethodStart(final String signature, final Object... args) {
        final String layerColor = colorFormatter.getLayerColor(signature);
        final String layerIcon = iconFormatter.getLayerIcon(signature);
        final String argsString = formatArguments(args);

        return messageSource.getMessage("log.method.start",
                new Object[]{layerColor + layerIcon + signature + LogConstants.RESET, argsString},
                LocaleContextHolder.getLocale());
    }

    /**
     * 메소드 종료 로그 메시지를 포맷팅합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @param result 메소드 결과 (final)
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param loggingProperties 로깅 설정 프로퍼티 (final)
     * @return 포맷팅된 메소드 종료 로그 메시지
     */
    public String formatMethodEnd(final String signature,
                                  final Object result,
                                  final long responseTimeMs,
                                  final LoggingProperties loggingProperties) {
        final String layerColor = colorFormatter.getLayerColor(signature);
        final String layerIcon = iconFormatter.getLayerIcon(signature);
        final String perfColor = colorFormatter.getPerformanceColor(responseTimeMs, loggingProperties);
        final String perfIcon = iconFormatter.getPerformanceIcon(responseTimeMs, loggingProperties);

        return messageSource.getMessage("log.method.end",
                new Object[]{layerColor + layerIcon + signature + LogConstants.RESET,
                        objectFormatter.safeResultToString(result),
                        perfColor + perfIcon + responseTimeMs + LogConstants.RESET},
                LocaleContextHolder.getLocale());
    }

    /**
     * 메소드 에러 로그 메시지를 포맷팅합니다.
     *
     * @param signature 메소드 시그니처 (final)
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param errorMessage 에러 메시지 (final)
     * @return 포맷팅된 에러 로그 메시지
     */
    public String formatMethodError(final String signature,
                                    final long responseTimeMs,
                                    final String errorMessage) {
        return messageSource.getMessage("log.method.error",
                new Object[]{LogConstants.RED + signature + LogConstants.RESET, responseTimeMs, errorMessage},
                LocaleContextHolder.getLocale());
    }

    /**
     * HTTP 요청 시작 로그 메시지를 포맷팅합니다.
     *
     * @param method HTTP 메소드 (final)
     * @param uri 요청 URI (final)
     * @param remoteAddr 원격 주소 (final)
     * @return 포맷팅된 요청 시작 로그 메시지
     */
    public String formatRequestStart(final String method,
                                     final String uri,
                                     final String remoteAddr) {
        return messageSource.getMessage("log.request.start",
                new Object[]{method, uri, remoteAddr},
                LocaleContextHolder.getLocale());
    }

    /**
     * HTTP 요청 종료 로그 메시지를 포맷팅합니다.
     *
     * @param method HTTP 메소드 (final)
     * @param uri 요청 URI (final)
     * @param status HTTP 상태코드 (final)
     * @param responseTimeMs 응답시간 (밀리초) (final)
     * @param loggingProperties 로깅 설정 프로퍼티 (final)
     * @return 포맷팅된 요청 종료 로그 메시지
     */
    public String formatRequestEnd(final String method,
                                   final String uri,
                                   final int status,
                                   final long responseTimeMs,
                                   final LoggingProperties loggingProperties) {
        final String statusColor = colorFormatter.getStatusColor(status);
        final String perfIcon = iconFormatter.getPerformanceIcon(responseTimeMs, loggingProperties);

        return messageSource.getMessage("log.request.end",
                new Object[]{method, uri, statusColor + status + LogConstants.RESET, perfIcon + responseTimeMs},
                LocaleContextHolder.getLocale());
    }

    /**
     * 메소드 인자들을 안전하게 문자열로 변환합니다.
     *
     * @param args 돌사할 인자 배열 (final)
     * @return 안전하게 변환된 인자 문자열
     */
    private String formatArguments(final Object... args) {
        return Arrays.stream(args)
                .map(objectFormatter::safeToString)
                .collect(Collectors.joining(", "));
    }
}
