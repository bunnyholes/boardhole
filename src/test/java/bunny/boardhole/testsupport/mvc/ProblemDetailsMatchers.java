package bunny.boardhole.testsupport.mvc;

import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultMatcher;

import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.util.MessageUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class ProblemDetailsMatchers {

    private ProblemDetailsMatchers() {
    }

    public static ResultMatcher[] unauthorized() {
        return new ResultMatcher[]{jsonPath("$.type").value("urn:problem-type:unauthorized"), jsonPath("$.title").value(MessageUtils.get("exception.title.unauthorized")), jsonPath("$.status").value(401), jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode())};
    }

    public static ResultMatcher[] forbidden() {
        return new ResultMatcher[]{jsonPath("$.type").value("urn:problem-type:forbidden"), jsonPath("$.title").value(MessageUtils.get("exception.title.access-denied")), jsonPath("$.status").value(403), jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode())};
    }

    public static ResultMatcher[] notFound() {
        return new ResultMatcher[]{jsonPath("$.type").value("urn:problem-type:not-found"), jsonPath("$.title").value(MessageUtils.get("exception.title.not-found")), jsonPath("$.status").value(404)};
    }

    public static ResultMatcher[] validationError() {
        return new ResultMatcher[]{jsonPath("$.type").value("urn:problem-type:validation-error"), jsonPath("$.title").value(MessageUtils.get("exception.title.validation-failed")), jsonPath("$.status").value(400), jsonPath("$.errors").isArray()};
    }

    public static ResultMatcher fieldErrorExists(String field) {
        return jsonPath("$.errors[?(@.field == '" + field + "')]").exists();
    }

    public static ResultMatcher hasPath(String path) {
        return jsonPath("$.path").value(path);
    }

    public static ResultMatcher hasMethod(String method) {
        return jsonPath("$.method").value(method);
    }

    public static ResultMatcher hasTimestamp() {
        return jsonPath("$.timestamp").exists();
    }

    public static ResultMatcher hasTraceId() {
        return jsonPath("$.traceId").value(Matchers.notNullValue());
    }
}

