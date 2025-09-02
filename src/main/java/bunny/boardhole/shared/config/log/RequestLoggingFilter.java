package bunny.boardhole.shared.config.log;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 요청/응답에 대한 로깅을 담당하는 서블릿 필터입니다.
 * <p>
 * 이 필터는 다음과 같은 기능을 제공합니다:
 * <ul>
 *   <li>요청 추적 ID 생성 및 관리 - X-Request-Id 헤더를 통한 요청 추적</li>
 *   <li>요청/응답 로깅 - HTTP 메서드, URI, 상태 코드, 응답 시간 기록</li>
 *   <li>MDC 컨텍스트 설정 - 요청별 메타데이터 관리</li>
 *   <li>CRLF 인젝션 방지 - 추적 ID 검증을 통한 보안 강화</li>
 *   <li>성능 모니터링 - 요청 처리 시간 측정</li>
 * </ul>
 * </p>
 *
 * <p>보안 고려사항:</p>
 * <ul>
 *   <li>외부에서 전달된 X-Request-Id 헤더를 검증하여 CRLF 인젝션 공격 방지</li>
 *   <li>추적 ID 길이 제한으로 DoS 공격 방지 (최대 64자)</li>
 *   <li>허용된 문자만 사용하도록 필터링 (알파벳, 숫자, 하이픈만 허용)</li>
 * </ul>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 * @see MDCUtil
 * @see LogFormatter
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "boardhole.logging.enabled", havingValue = "true", matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = LogConstants.TRACE_ID_KEY;
    private final MessageSource messageSource;
    private final LogFormatter logFormatter;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String existing = request.getHeader("X-Request-Id");
        String traceId;
        
        // CRLF 인젝션 방지를 위한 외부 추적 ID 검증 및 살균 처리
        if (existing != null && !existing.isBlank() && isValidTraceId(existing)) {
            traceId = sanitizeTraceId(existing);
            // 로그 레벨 가드 적용
            if (log.isDebugEnabled()) {
                log.debug(messageSource.getMessage("log.trace.reuse", new Object[]{traceId}, 
                        org.springframework.context.i18n.LocaleContextHolder.getLocale()));
            }
        } else {
            traceId = UUID.randomUUID().toString().replace("-", "");
            // 외부 추적 ID가 유효하지 않은 경우 경고 로깅
            if (existing != null && !existing.isBlank() && log.isWarnEnabled()) {
                log.warn(messageSource.getMessage("log.trace.invalid", new Object[]{existing.length()}, 
                        org.springframework.context.i18n.LocaleContextHolder.getLocale()));
            }
        }
        MDCUtil.setTraceId(traceId);
        MDCUtil.setSessionId(request);
        MDCUtil.setClientIp(request.getRemoteAddr());
        response.setHeader("X-Request-Id", traceId);

        long start = System.nanoTime();
        try {
            log.info(logFormatter.formatRequestStart(
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr()));
            filterChain.doFilter(request, response);
        } finally {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            log.info(logFormatter.formatRequestEnd(
                    request.getMethod(), request.getRequestURI(), response.getStatus(), tookMs));
            MDCUtil.clearRequest();
        }
    }
    
    /**
     * 추적 ID가 유효한 형식인지 검증합니다.
     * <p>
     * CRLF 인젝션 공격을 방지하고 적절한 길이의 추적 ID만 허용합니다.
     * </p>
     *
     * @param traceId 검증할 추적 ID
     * @return 유효한 추적 ID인 경우 true
     */
    private boolean isValidTraceId(final String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return false;
        }
        
        // CRLF 인젝션 방지: 개행문자 체크
        if (traceId.contains("\r") || traceId.contains("\n") || traceId.contains("\t")) {
            return false;
        }
        
        // 길이 제한: DoS 공격 방지
        return traceId.length() <= 64 && traceId.matches("[a-zA-Z0-9\\-]+");
    }
    
    /**
     * 추적 ID를 살균 처리합니다.
     * <p>
     * 외부에서 전달된 추적 ID에서 사해할 수 있는 문자를 제거하여
     * 로깅 시스템에 안전한 입력만 허용합니다.
     * </p>
     *
     * @param traceId 살균 처리할 추적 ID
     * @return 살균 처리된 추적 ID
     */
    private String sanitizeTraceId(final String traceId) {
        if (traceId == null) {
            return "";
        }
        
        // 개행문자 제거
        String sanitized = traceId.replaceAll("[\\r\\n\\t]", "");
        
        // 길이 제한
        if (sanitized.length() > 64) {
            sanitized = sanitized.substring(0, 64);
        }
        
        // 허용된 문자만 유지
        return sanitized.replaceAll("[^a-zA-Z0-9\\-]", "");
    }

}
