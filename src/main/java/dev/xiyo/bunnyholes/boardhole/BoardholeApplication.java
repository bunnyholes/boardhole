package dev.xiyo.bunnyholes.boardhole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import dev.xiyo.bunnyholes.boardhole.shared.config.log.LoggingProperties;

@SpringBootApplication
@EnableConfigurationProperties(LoggingProperties.class)
@EnableJpaAuditing
public class BoardholeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BoardholeApplication.class, args);
    }

}
