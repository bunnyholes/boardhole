package bunny.boardhole;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Board Hole API", version = "v1.0.0", description = "Board Hole 애플리케이션 REST API"))
public class BoardHoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardHoleApplication.class, args);
    }

}
