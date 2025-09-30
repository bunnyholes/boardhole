package dev.xiyo.bunnyholes.boardhole.shared.config;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import dev.xiyo.bunnyholes.boardhole.shared.properties.CorsProperties;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorsConfig 단위 테스트")
class CorsConfigTest {

    @Test
    @DisplayName("와일드카드 Origin과 Credentials 허용 시 allowedOriginPatterns 를 사용한다")
    void shouldUseOriginPatternsWhenWildcardAndCredentials() {
        CorsProperties properties = new CorsProperties(
                List.of("/api/**"),
                List.of("https://*.example.com"),
                List.of("GET", "POST"),
                List.of("Content-Type", "Authorization"),
                List.of("X-Custom-Header"),
                true,
                3_600
        );

        CorsConfig config = new CorsConfig(properties);
        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/boards");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).isNull();
        assertThat(configuration.getAllowedOriginPatterns()).containsExactly("https://*.example.com");
        assertThat(configuration.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(configuration.getAllowedHeaders()).containsExactlyInAnyOrder("Content-Type", "Authorization");
        assertThat(configuration.getExposedHeaders()).containsExactly("X-Custom-Header");
        assertThat(configuration.getMaxAge()).isEqualTo(3_600);
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("정확한 Origin 목록은 allowedOrigins 로 등록된다")
    void shouldUseExactOriginsWhenNoWildcard() {
        CorsProperties properties = new CorsProperties(
                List.of("/api/**"),
                List.of("https://app.example.com"),
                List.of("GET"),
                List.of("Content-Type"),
                List.of(),
                false,
                1_800
        );

        CorsConfig config = new CorsConfig(properties);
        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("https://app.example.com");
        assertThat(configuration.getAllowedOriginPatterns()).isNull();
        assertThat(configuration.getAllowCredentials()).isFalse();
        assertThat(configuration.getMaxAge()).isEqualTo(1_800);
    }
}
