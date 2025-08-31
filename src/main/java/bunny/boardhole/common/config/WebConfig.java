package bunny.boardhole.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${boardhole.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${boardhole.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${boardhole.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${boardhole.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${boardhole.cors.max-age}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }
}