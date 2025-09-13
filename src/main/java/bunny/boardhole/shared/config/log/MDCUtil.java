package bunny.boardhole.shared.config.log;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import lombok.experimental.UtilityClass;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
class MDCUtil {

    void setUserId() {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).filter(Authentication::isAuthenticated).map(Authentication::getName).ifPresent(username -> MDC.put("userId", username));
    }

    void setSessionId(HttpServletRequest request) {
        Optional.ofNullable(request.getSession(false)).ifPresent(session -> MDC.put("sessionId", session.getId()));
    }

    void setLayer(String layer) {
        MDC.put("layer", layer);
    }

    void clearRequest() {
        MDC.remove(LogConstants.TRACE_ID_KEY);
        MDC.remove("userId");
        MDC.remove("sessionId");
        MDC.remove("clientIp");
    }

    void clearMethod() {
        MDC.remove("layer");
        MDC.remove("operation");
    }

    void setTraceId(String traceId) {
        MDC.put(LogConstants.TRACE_ID_KEY, traceId);
    }

    String getClientIp() {
        return MDC.get("clientIp");
    }

    void setClientIp(String clientIp) {
        MDC.put("clientIp", clientIp);
    }
}