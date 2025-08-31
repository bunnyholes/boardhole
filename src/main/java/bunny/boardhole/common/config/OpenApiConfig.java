package bunny.boardhole.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI boardHoleOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Board-Hole API")
                        .description("간단한 게시판 시스템 REST API - Spring Boot 3.5와 CQRS 패턴을 사용한 현대적인 웹 애플리케이션")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Board-Hole 개발팀")
                                .email("admin@boardhole.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("session", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("세션 기반 인증 - 로그인 후 자동으로 설정되는 세션 쿠키")))
                .addSecurityItem(new SecurityRequirement().addList("session"));
    }
}