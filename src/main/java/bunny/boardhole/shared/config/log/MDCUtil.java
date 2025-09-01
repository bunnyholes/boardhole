package bunny.boardhole.shared.config.log;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class MDCUtil {

    private MDCUtil() {
    }

    public static void setUserId() {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .ifPresent(username -> MDC.put("userId", username));
    }

    public static void setSessionId(HttpServletRequest request) {
        Optional.ofNullable(request.getSession(false))
                .ifPresent(session -> MDC.put("sessionId", session.getId()));
    }

    public static void setLayer(String layer) {
        MDC.put("layer", layer);
    }

    public static void setOperation(String operation) {
        MDC.put("operation", operation);
    }

    public static void clearAll() {
        MDC.clear();
    }

    public static void clearRequest() {
        MDC.remove(LogConstants.TRACE_ID_KEY);
        MDC.remove("userId");
        MDC.remove("sessionId");
        MDC.remove("clientIp");
    }

    public static void clearMethod() {
        MDC.remove("layer");
        MDC.remove("operation");
    }

    public static String getTraceId() {
        return MDC.get(LogConstants.TRACE_ID_KEY);
    }

    public static void setTraceId(String traceId) {
        MDC.put(LogConstants.TRACE_ID_KEY, traceId);
    }

    public static String getUserId() {
        return MDC.get("userId");
    }

    public static String getClientIp() {
        return MDC.get("clientIp");
    }

    public static void setClientIp(String clientIp) {
        MDC.put("clientIp", clientIp);
    }
}