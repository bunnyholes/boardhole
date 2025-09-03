package bunny.boardhole.shared.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
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
                                .description(openApiConfig.getSecurity().getSession().getDescription()))
                        // Page<BoardResponse> 스키마 정의
                        .addSchemas("PageBoardResponse", new Schema<>()
                                .type("object")
                                .description("페이지네이션된 게시글 목록")
                                .addProperty("content", new ArraySchema()
                                        .items(new Schema<>().$ref("#/components/schemas/BoardResponse"))
                                        .description("게시글 목록"))
                                .addProperty("pageable", new Schema<>()
                                        .type("object")
                                        .description("페이지네이션 정보")
                                        .addProperty("pageNumber", new Schema<>().type("integer").description("현재 페이지 번호"))
                                        .addProperty("pageSize", new Schema<>().type("integer").description("페이지 크기"))
                                        .addProperty("sort", new Schema<>()
                                                .type("object")
                                                .addProperty("sorted", new Schema<>().type("boolean"))
                                                .addProperty("empty", new Schema<>().type("boolean"))))
                                .addProperty("totalElements", new Schema<>().type("integer").description("전체 요소 수"))
                                .addProperty("totalPages", new Schema<>().type("integer").description("전체 페이지 수"))
                                .addProperty("last", new Schema<>().type("boolean").description("마지막 페이지 여부"))
                                .addProperty("first", new Schema<>().type("boolean").description("첫 번째 페이지 여부"))
                                .addProperty("numberOfElements", new Schema<>().type("integer").description("현재 페이지 요소 수"))
                                .addProperty("empty", new Schema<>().type("boolean").description("빈 페이지 여부"))));
    }
}