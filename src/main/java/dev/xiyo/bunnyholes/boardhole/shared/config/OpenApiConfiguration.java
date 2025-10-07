package dev.xiyo.bunnyholes.boardhole.shared.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.xiyo.bunnyholes.boardhole.shared.properties.ApiProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI Bean 설정
 * ApiProperties를 사용하여 OpenAPI 문서를 생성
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final ApiProperties apiProperties;

    @Bean
    public OpenAPI boardHoleOpenAPI() {
        ApiProperties.Contact contact = apiProperties.contact();
        ApiProperties.License license = apiProperties.license();

        final String basicAuthSchemeName = "basicAuth";

        return new OpenAPI()
                .info(new Info()
                        .title(apiProperties.title())
                        .version(apiProperties.version())
                        .description(apiProperties.description())
                        .termsOfService(apiProperties.termsOfService())
                        .contact(new Contact()
                                .name(contact.name())
                                .email(contact.email())
                                .url(contact.url()))
                        .license(new License()
                                .name(license.name())
                                .url(license.url())))
                .components(new Components()
                        .addSecuritySchemes(basicAuthSchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("HTTP Basic Authentication (username/password)")));
    }
}
