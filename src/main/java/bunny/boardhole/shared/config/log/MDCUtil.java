package bunny.boardhole.shared.config.log;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * MDC(Mapped Diagnostic Context) 관리 유틸리티 클래스
 * SLF4J MDC를 사용하여 로그 컨텍스트 정보를 관리합니다.
 */
public final class MDCUtil {
    
    /** MDC 키 상수들 */
    private static final String USER_ID_KEY = "userId";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String CLIENT_IP_KEY = "clientIp";
    private static final String LAYER_KEY = "layer";
    private static final String OPERATION_KEY = "operation";

    /**
     * 유틸리티 클래스로 인스턴스 생성을 방지합니다.
     */
    private MDCUtil() {
    }

    /**
     * 현재 인증된 사용자 ID를 MDC에 설정합니다.
     */
    public static void setUserId() {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .ifPresent(username -> MDC.put(USER_ID_KEY, username));
    }

    /**
     * HTTP 세션 ID를 MDC에 설정합니다.
     *
     * @param request HTTP 서블릿 요청 객체
     */
    public static void setSessionId(final HttpServletRequest request) {
        Optional.ofNullable(request.getSession(false))
                .ifPresent(session -> MDC.put(SESSION_ID_KEY, session.getId()));
    }

    /**
     * 애플리케이션 계층 정보를 MDC에 설정합니다.
     *
     * @param layer 계층 정보 (예: controller, service, repository)
     */
    public static void setLayer(final String layer) {
        MDC.put(LAYER_KEY, layer);
    }

    /**
     * 수행 중인 오퍼레이션 정보를 MDC에 설정합니다.
     *
     * @param operation 오퍼레이션 이름
     */
    public static void setOperation(final String operation) {
        MDC.put(OPERATION_KEY, operation);
    }

    /**
     * 모든 MDC 컨텍스트를 제거합니다.
     */
    public static void clearAll() {
        MDC.clear();
    }

    /**
     * 요청 범위 MDC 컨텍스트를 제거합니다.
     */
    public static void clearRequest() {
        MDC.remove(LogConstants.TRACE_ID_KEY);
        MDC.remove(USER_ID_KEY);
        MDC.remove(SESSION_ID_KEY);
        MDC.remove(CLIENT_IP_KEY);
    }

    /**
     * 메소드 범위 MDC 컨텍스트를 제거합니다.
     */
    public static void clearMethod() {
        MDC.remove(LAYER_KEY);
        MDC.remove(OPERATION_KEY);
    }

    /**
     * 현재 요청의 추적 ID를 반환합니다.
     *
     * @return 추적 ID
     */
    public static String getTraceId() {
        return MDC.get(LogConstants.TRACE_ID_KEY);
    }

    /**
     * 요청 추적 ID를 MDC에 설정합니다.
     *
     * @param traceId 추적 ID
     */
    public static void setTraceId(final String traceId) {
        MDC.put(LogConstants.TRACE_ID_KEY, traceId);
    }

    /**
     * 현재 설정된 사용자 ID를 반환합니다.
     *
     * @return 사용자 ID
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    /**
     * 현재 설정된 클라이언트 IP 주소를 반환합니다.
     *
     * @return 클라이언트 IP 주소
     */
    public static String getClientIp() {
        return MDC.get(CLIENT_IP_KEY);
    }

    /**
     * 클라이언트 IP 주소를 MDC에 설정합니다.
     *
     * @param clientIp 클라이언트 IP 주소
     */
    public static void setClientIp(final String clientIp) {
        MDC.put(CLIENT_IP_KEY, clientIp);
    }
}