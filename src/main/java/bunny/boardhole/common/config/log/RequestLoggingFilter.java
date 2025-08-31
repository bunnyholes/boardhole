package bunny.boardhole.common.config.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final MessageSource messageSource;
    private final LogFormatter logFormatter;

    public static final String TRACE_ID = LogConstants.TRACE_ID_KEY;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String existing = request.getHeader("X-Request-Id");
        String traceId = (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString().replace("-", "");
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

}
