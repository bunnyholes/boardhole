package bunny.boardhole.testsupport.mvc;

import org.springframework.test.web.servlet.ResultMatcher;

public final class MatchersUtil {
    private MatchersUtil() {
    }

    public static ResultMatcher all(ResultMatcher... matchers) {
        return result -> {
            for (ResultMatcher m : matchers)
                m.match(result);
        };
    }
}

