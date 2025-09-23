package dev.xiyo.bunnyholes.boardhole.shared.security;

import java.net.URI;
import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;


/**
 * ProblemDetail 생성을 위한 공통 헬퍼 클래스
 */
@Slf4j
@UtilityClass
public class ProblemDetailsHelper {

    /**
     * ProblemDetail에 공통 속성들을 추가합니다.
     */
    public void addCommonProperties(ProblemDetail pd, HttpServletRequest request, String errorCode) {
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request URI: {}", request.getRequestURI(), e);
        }

        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("method", request.getMethod());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", errorCode);
    }

    /**
     * Problem Type URI를 생성합니다.
     */
    public URI buildType(String slug) {
        try {
            return URI.create("urn:problem-type:" + slug);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid problem type slug: {}", slug, e);
            return URI.create("urn:problem-type:unknown");
        }
    }
}
