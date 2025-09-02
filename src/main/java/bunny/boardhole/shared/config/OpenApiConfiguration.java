package bunny.boardhole.shared.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.*;

/**
 * OpenAPI Bean 설정 - properties를 사용하여 문서 생성
 */
@Configuration
public class OpenApiConfiguration {

    /** OpenAPI 설정 프로퍼티 */
    private final OpenApiConfig openApiConfig;
    
    /** 빌드 프로퍼티 (선택적) */
    private final BuildProperties buildProperties;

    /**
     * OpenAPI 설정 생성자
     *
     * @param openApiConfig OpenAPI 설정 프로퍼티
     * @param buildProperties      빌드 프로퍼티 (선택적)
     */
    public OpenApiConfiguration(final OpenApiConfig openApiConfig,
                                @Autowired(required = false) final BuildProperties buildProperties) {
        this.openApiConfig = openApiConfig;
        this.buildProperties = buildProperties;
    }

    /**
     * Board Hole OpenAPI 스펙 빈 생성
     *
     * @return 구성된 OpenAPI 스펙
     */
    @Bean
    public OpenAPI boardHoleOpenAPI() {
        // 상수 선언
        final String sessionSchemeKey = "session";
        final String buildTimePrefix = " (Build: ";
        final String buildTimeSuffix = ")";
        
        // BuildProperties가 있으면 Gradle 정보 사용, 없으면 yml 설정값 사용
        final String configVersion = openApiConfig.getVersion();
        final String configDescription = openApiConfig.getDescription();
        final String finalVersion = buildProperties != null ?
                buildProperties.getVersion() : configVersion;
        final String finalDescription = buildProperties != null ?
                configDescription + buildTimePrefix + buildProperties.getTime() + buildTimeSuffix :
                configDescription;

        return new OpenAPI()
                .info(new Info()
                        .title(openApiConfig.getTitle())
                        .version(finalVersion)
                        .description(finalDescription)
                        .termsOfService(openApiConfig.getTermsOfService())
                        .contact(createContactInfo())
                        .license(createLicenseInfo()))
                .components(new Components()
                        .addSecuritySchemes(sessionSchemeKey, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(getSessionName())
                                .description(getSessionDescription())));
    }

    /**
     * Contact 정보 생성
     */
    private io.swagger.v3.oas.models.info.Contact createContactInfo() {
        final OpenApiConfig.Contact contact = openApiConfig.getContact();
        final String contactName = contact.getName();
        final String contactEmail = contact.getEmail();
        final String contactUrl = contact.getUrl();
        return new io.swagger.v3.oas.models.info.Contact()
                .name(contactName)
                .email(contactEmail)
                .url(contactUrl);
    }

    /**
     * License 정보 생성
     */
    private io.swagger.v3.oas.models.info.License createLicenseInfo() {
        final OpenApiConfig.License license = openApiConfig.getLicense();
        final String licenseName = license.getName();
        final String licenseUrl = license.getUrl();
        return new io.swagger.v3.oas.models.info.License()
                .name(licenseName)
                .url(licenseUrl);
    }

    /**
     * 세션 이름 조회
     */
    private String getSessionName() {
        final OpenApiConfig.Security security = openApiConfig.getSecurity();
        final OpenApiConfig.Security.Session session = security.getSession();
        return session.getName();
    }

    /**
     * 세션 설명 조회
     */
    private String getSessionDescription() {
        final OpenApiConfig.Security security = openApiConfig.getSecurity();
        final OpenApiConfig.Security.Session session = security.getSession();
        return session.getDescription();
    }
}