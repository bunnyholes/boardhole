package bunny.boardhole.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.*;

/**
 * OpenAPI Bean 설정
 * OpenApiConfig properties를 사용하여 OpenAPI 문서를 생성
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final BuildProperties buildProperties;

    @Value("${boardhole.api.title}")
    private String title;

    @Value("${boardhole.api.description}")
    private String description;

    @Value("${boardhole.api.terms-of-service}")
    private String termsOfService;

    @Value("${boardhole.api.contact.name}")
    private String contactName;

    @Value("${boardhole.api.contact.email}")
    private String contactEmail;

    @Value("${boardhole.api.contact.url}")
    private String contactUrl;

    @Value("${boardhole.api.license.name}")
    private String licenseName;

    @Value("${boardhole.api.license.url}")
    private String licenseUrl;

    @Bean
    public OpenAPI boardHoleOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(buildProperties.getVersion())
                        .description(description + " (Build: " + buildProperties.getTime() + ")")
                        .termsOfService(termsOfService)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                ;
    }
}