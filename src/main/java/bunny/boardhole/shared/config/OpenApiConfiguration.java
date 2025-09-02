package bunny.boardhole.shared.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.*;

/**
 * OpenAPI Bean 설정
 * OpenApiConfig properties를 사용하여 OpenAPI 문서를 생성
 */
@Configuration
public class OpenApiConfiguration {

    private final OpenApiConfig openApiConfig;
    private final BuildProperties buildProperties;

    public OpenApiConfiguration(OpenApiConfig openApiConfig,
                                @Autowired(required = false) BuildProperties buildProperties) {
        this.openApiConfig = openApiConfig;
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI boardHoleOpenAPI() {
        // BuildProperties가 있으면 Gradle 정보 사용, 없으면 yml 설정값 사용
        String finalVersion = buildProperties != null ?
                buildProperties.getVersion() : openApiConfig.getVersion();
        String finalDescription = buildProperties != null ?
                openApiConfig.getDescription() + " (Build: " + buildProperties.getTime() + ")" :
                openApiConfig.getDescription();

        return new OpenAPI()
                .info(new Info()
                        .title(openApiConfig.getTitle())
                        .version(finalVersion)
                        .description(finalDescription)
                        .termsOfService(openApiConfig.getTermsOfService())
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name(openApiConfig.getContact().getName())
                                .email(openApiConfig.getContact().getEmail())
                                .url(openApiConfig.getContact().getUrl()))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name(openApiConfig.getLicense().getName())
                                .url(openApiConfig.getLicense().getUrl())))
                .components(new Components()
                        .addSecuritySchemes("session", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(openApiConfig.getSecurity().getSession().getName())
                                .description(openApiConfig.getSecurity().getSession().getDescription())));
    }
}