package bunny.boardhole.shared.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 프로퍼티 설정 활성화
 * 모든 @ConfigurationProperties 클래스를 스프링 컨테이너에 등록
 */
@Configuration
@EnableConfigurationProperties({
        ProblemProperties.class,
        SecurityProperties.class,
        CorsProperties.class,
        ApiProperties.class,
        DefaultUsersProperties.class
})
public class PropertiesConfiguration {
}