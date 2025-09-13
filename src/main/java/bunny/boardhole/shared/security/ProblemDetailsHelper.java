package bunny.boardhole.shared.security;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;

/**
 * ProblemDetail 생성을 위한 공통 헬퍼 클래스
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class ProblemDetailsHelper {

    /**
     * ProblemDetail에 공통 속성들을 추가합니다.
     */
    static void addCommonProperties(ProblemDetail pd, HttpServletRequest request, String errorCode) {
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request URI: {}", request.getRequestURI(), e);
        }

        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID)).filter(traceId -> !traceId.isBlank()).ifPresent(traceId -> pd.setProperty("traceId", traceId));

        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("method", request.getMethod());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", errorCode);
    }

    /**
     * Problem Type URI를 생성합니다.
     */
    static URI buildType(String problemBaseUri, String slug) {
        return Optional.ofNullable(problemBaseUri).filter(base -> !base.isBlank()).map(base -> base.endsWith("/") ? base : base + "/").map(base -> {
            try {
                return URI.create(base + slug);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid problem type URI: {}{}", base, slug, e);
                return null;
            }
        }).orElse(URI.create("urn:problem-type:" + slug));
    }
}