package bunny.boardhole.shared.config;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import bunny.boardhole.shared.properties.ApiProperties;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * OpenAPI Bean 설정
 * ApiProperties를 사용하여 OpenAPI 문서를 생성
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final BuildProperties buildProperties;
    private final ApiProperties apiProperties;

    @Bean
    public OpenAPI boardHoleOpenAPI() {
        ApiProperties.Contact contact = apiProperties.contact();
        ApiProperties.License license = apiProperties.license();

        return new OpenAPI()
                .info(new Info()
                        .title(apiProperties.title())
                        .version(buildProperties.getVersion())
                        .description(apiProperties.description() + " (Build: " + buildProperties.getTime() + ")")
                        .termsOfService(apiProperties.termsOfService())
                        .contact(new Contact()
                                .name(contact.name())
                                .email(contact.email())
                                .url(contact.url()))
                        .license(new License()
                                .name(license.name())
                                .url(license.url())));
    }
}
