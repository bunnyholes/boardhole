package bunny.boardhole.shared.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import bunny.boardhole.shared.properties.CorsProperties;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration cfg = new CorsConfiguration();
        // allowCredentials=true 이고 "*" 포함시 Spring은 allowedOrigins+credentials 조합을 허용하지 않음
        // 이런 경우 allowedOriginPatterns 사용으로 전환
        var origins = corsProperties.allowedOrigins();
        boolean usePatterns = corsProperties.allowCredentials() && origins != null && origins.stream().anyMatch(o -> "*".equals(o) || o.contains("*"));
        if (usePatterns)
            cfg.setAllowedOriginPatterns(origins);
        else
            cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(corsProperties.allowedMethods());
        cfg.setAllowedHeaders(corsProperties.allowedHeaders());
        cfg.setExposedHeaders(corsProperties.exposedHeaders());
        cfg.setAllowCredentials(corsProperties.allowCredentials());
        cfg.setMaxAge(corsProperties.maxAge());

        for (String pattern : corsProperties.pathPatterns())
            source.registerCorsConfiguration(pattern, cfg);
        return source;
    }
}
