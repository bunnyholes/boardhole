package bunny.boardhole;

import bunny.boardhole.shared.config.OpenApiConfig;
import bunny.boardhole.shared.config.log.LoggingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({LoggingProperties.class, OpenApiConfig.class})
public class BoardHoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardHoleApplication.class, args);
    }

}
