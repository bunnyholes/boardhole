package bunny.boardhole.shared.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.properties.ProblemProperties;
import bunny.boardhole.shared.util.MessageUtils;

/**
 * ProblemDetail 생성을 위한 Builder 유틸리티
 * 중복 코드를 제거하고 일관된 에러 응답 생성을 보장합니다.
 */
public class ProblemDetailBuilder {

    private final ProblemDetail problemDetail;
    private final ProblemProperties problemProperties;
    private @Nullable HttpServletRequest request;

    private ProblemDetailBuilder(HttpStatus status, String detail, ProblemProperties problemProperties) {
        problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        this.problemProperties = problemProperties;
    }

    public static ProblemDetailBuilder forStatus(HttpStatus status, ProblemProperties problemProperties) {
        return new ProblemDetailBuilder(status, "", problemProperties);
    }

    public static ProblemDetailBuilder forStatusAndDetail(HttpStatus status, String detail, ProblemProperties problemProperties) {
        return new ProblemDetailBuilder(status, detail, problemProperties);
    }

    public ProblemDetailBuilder title(String titleKey) {
        problemDetail.setTitle(MessageUtils.get(titleKey));
        return this;
    }

    public ProblemDetailBuilder detail(String detailKey, Object... args) {
        problemDetail.setDetail(MessageUtils.get(detailKey, args));
        return this;
    }

    public ProblemDetailBuilder code(ErrorCode errorCode) {
        problemDetail.setProperty("code", errorCode.getCode());
        return this;
    }

    public ProblemDetailBuilder property(String key, Object value) {
        problemDetail.setProperty(key, value);
        return this;
    }

    public ProblemDetailBuilder type(String slug) {
        String baseUri = problemProperties.baseUri();
        URI typeUri = Optional.ofNullable(baseUri)
                              .filter(base -> !base.isBlank())
                              .map(base -> base.endsWith("/") ? base : base + "/")
                              .map(base -> {
                                  try {
                                      return URI.create(base + slug);
                                  } catch (IllegalArgumentException ignored) {
                                      return null;
                                  }
                              })
                              .orElse(URI.create("urn:problem-type:" + slug));
        problemDetail.setType(typeUri);
        return this;
    }

    public ProblemDetailBuilder withRequest(@Nullable HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public ProblemDetailBuilder withCommonProperties(@Nullable HttpServletRequest request) {
        this.request = request;
        addCommonProperties();
        return this;
    }

    private void addCommonProperties() {
        // TraceId 추가
        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID))
                .filter(traceId -> !traceId.isBlank())
                .ifPresent(traceId -> problemDetail.setProperty("traceId", traceId));

        // Request 정보 추가
        Optional.ofNullable(request).ifPresent(req -> {
            problemDetail.setProperty("path", req.getRequestURI());
            problemDetail.setProperty("method", req.getMethod());
            try {
                problemDetail.setInstance(URI.create(req.getRequestURI()));
            } catch (IllegalArgumentException ignored) {
            }
        });

        // Timestamp 추가
        problemDetail.setProperty("timestamp", Instant.now().toString());
    }

    public ProblemDetail build() {
        if (request != null && problemDetail.getProperties() != null &&
                !problemDetail.getProperties().containsKey("timestamp"))
            addCommonProperties();
        return problemDetail;
    }
}
