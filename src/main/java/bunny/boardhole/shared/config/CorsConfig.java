package bunny.boardhole.shared.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;

@Configuration
public class CorsConfig {

    @Value("#{'${boardhole.cors.path-patterns}'.split(',')}")
    private List<String> pathPatterns;

    @Value("#{'${boardhole.cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${boardhole.cors.allowed-methods}'.split(',')}")
    private List<String> allowedMethods;

    @Value("#{'${boardhole.cors.allowed-headers}'.split(',')}")
    private List<String> allowedHeaders;

    @Value("#{'${boardhole.cors.exposed-headers:}'.trim().isEmpty() ? T(java.util.Collections).emptyList() : T(java.util.Arrays).asList('${boardhole.cors.exposed-headers:}'.split(','))}")
    private List<String> exposedHeaders;

    @Value("${boardhole.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${boardhole.cors.max-age}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(allowedMethods);
        cfg.setAllowedHeaders(allowedHeaders);
        cfg.setExposedHeaders(exposedHeaders);
        cfg.setAllowCredentials(allowCredentials);
        cfg.setMaxAge(maxAge);

        for (String pattern : pathPatterns) source.registerCorsConfiguration(pattern, cfg);
        return source;
    }
}
