package bunny.boardhole.testsupport.config;

import bunny.boardhole.shared.security.EmailVerificationFilter;
import jakarta.servlet.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

import java.io.IOException;

@TestConfiguration
public class TestSecurityOverrides {

    @Bean
    @Primary
    public EmailVerificationFilter emailVerificationBypassFilter() {
        // No-op filter for tests: bypasses email verification checks
        return new EmailVerificationFilter(null, null) {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                chain.doFilter(request, response);
            }
        };
    }
}
