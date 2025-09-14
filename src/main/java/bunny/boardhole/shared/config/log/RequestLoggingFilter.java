package bunny.boardhole.shared.config.log;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@ConditionalOnProperty(name = "boardhole.logging.enabled", havingValue = "true", matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = LogConstants.TRACE_ID_KEY;
    private final LogFormatter logFormatter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String existing = request.getHeader("X-Request-Id");
        String traceId = (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString().replace("-", "");
        MDCUtil.setTraceId(traceId);
        MDCUtil.setSessionId(request);
        MDCUtil.setClientIp(request.getRemoteAddr());
        response.setHeader("X-Request-Id", traceId);

        long start = System.nanoTime();
        try {
            try {
                log.info(logFormatter.formatRequestStart(request.getMethod(), request.getRequestURI(), request.getRemoteAddr()));
            } catch (Throwable formatEx) {
                log.warn("Request log formatting failed (start): {} {} - {}", request.getMethod(), request.getRequestURI(), formatEx.toString());
            }
            filterChain.doFilter(request, response);
        } finally {
            long tookMs = (System.nanoTime() - start) / 1_000_000;
            try {
                log.info(logFormatter.formatRequestEnd(request.getMethod(), request.getRequestURI(), response.getStatus(), tookMs));
            } catch (Throwable formatEx) {
                log.warn("Request log formatting failed (end): {} {} [{}] - {}", request.getMethod(), request.getRequestURI(), response.getStatus(),
                        formatEx.toString());
            }
            MDCUtil.clearRequest();
        }
    }

}
