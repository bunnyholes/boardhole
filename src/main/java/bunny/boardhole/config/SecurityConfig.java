package bunny.boardhole.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { 
        return new BCryptPasswordEncoder(); 
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Static resources and common locations
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/", "/index.html").permitAll()
                // Swagger UI - explicitly permit
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                // Error page
                .requestMatchers("/error").permitAll()
                // Public auth endpoints (backup for @PermitAll)
                .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/public-access").permitAll()
                // Public board endpoints (backup for @PermitAll)
                .requestMatchers(HttpMethod.GET, "/api/boards", "/api/boards/**").permitAll()
                // Public user endpoints (backup for @PermitAll)
                .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").permitAll()
                // All other requests require authentication by default
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(problemDetailsAuthenticationEntryPoint())
                .accessDeniedHandler(problemDetailsAccessDeniedHandler())
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false))
            .securityContext((securityContext) -> securityContext
                .securityContextRepository(securityContextRepository));
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public bunny.boardhole.security.ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint() {
        return new bunny.boardhole.security.ProblemDetailsAuthenticationEntryPoint();
    }

    @Bean
    public bunny.boardhole.security.ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler() {
        return new bunny.boardhole.security.ProblemDetailsAccessDeniedHandler();
    }
}
