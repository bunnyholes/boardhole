package bunny.boardhole.testsupport.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import bunny.boardhole.shared.security.EmailVerificationFilter;

@TestConfiguration
public class TestSecurityOverrides {

    @Bean
    @Primary
    public EmailVerificationFilter emailVerificationBypassFilter() {
        // No-op filter for tests: bypasses email verification checks
        // Suppress null warning: dependencies not used in overridden doFilter method
        @SuppressWarnings("DataFlowIssue") EmailVerificationFilter filter = new EmailVerificationFilter(null, null) {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                chain.doFilter(request, response);
            }
        };
        return filter;
    }
}
