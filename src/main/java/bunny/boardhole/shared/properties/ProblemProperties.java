package bunny.boardhole.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Problem Details API 관련 설정
 * RFC 7807 Problem Details 표준에 따른 에러 응답 설정
 */
@ConfigurationProperties(prefix = "boardhole.problem")
public record ProblemProperties(
        /**
         * 문제 유형(type) 링크의 베이스 URI
         * 비워두면 urn:problem-type:{slug} 형식으로 자동 생성
         */
        String baseUri
) {
}