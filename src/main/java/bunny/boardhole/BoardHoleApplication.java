package bunny.boardhole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import bunny.boardhole.shared.config.log.LoggingProperties;

@SpringBootApplication
@EnableConfigurationProperties(LoggingProperties.class)
@EnableJpaAuditing
public class BoardHoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardHoleApplication.class, args);
    }

}
