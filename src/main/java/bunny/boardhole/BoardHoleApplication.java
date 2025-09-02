package bunny.boardhole;

import bunny.boardhole.shared.config.OpenApiConfig;
import bunny.boardhole.shared.config.log.LoggingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Board Hole 애플리케이션의 메인 클래스입니다.
 * Spring Boot 애플리케이션의 진입점 역할을 수행합니다.
 */
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot 애플리케이션 진입점 - 유틸리티 클래스가 아님
@SpringBootApplication
@EnableConfigurationProperties({LoggingProperties.class, OpenApiConfig.class})
public class BoardHoleApplication {

    /**
     * 애플리케이션 메인 메소드입니다.
     *
     * @param arguments 명령행 인수 배열
     */
    public static void main(final String[] arguments) {
        SpringApplication.run(BoardHoleApplication.class, arguments);
    }

}
