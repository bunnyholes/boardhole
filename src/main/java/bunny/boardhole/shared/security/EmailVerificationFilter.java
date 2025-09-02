package bunny.boardhole.shared.security;

import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * 이메일 인증이 필요한 사용자를 체크하는 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationFilter implements Filter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // 이메일 인증이 필요하지 않은 경로들
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            ApiPaths.AUTH + "/verify-email",
            ApiPaths.AUTH + "/resend-verification",
            ApiPaths.AUTH + ApiPaths.AUTH_LOGIN,
            ApiPaths.AUTH + ApiPaths.AUTH_LOGOUT,
            "/error",
            "/v3/api-docs",
            "/swagger-ui"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        
        // 제외 경로이거나 정적 리소스인 경우 필터 통과
        if (shouldSkipFilter(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 인증된 사용자인 경우에만 이메일 인증 상태 체크
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            
            String username = authentication.getName();
            User user = userRepository.findOptionalByUsername(username).orElse(null);
            
            if (user != null && !user.isEmailVerified()) {
                // 이메일 미인증 사용자에 대한 응답
                sendEmailVerificationRequiredResponse(httpResponse);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(String requestPath) {
        // 정적 리소스
        if (requestPath.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$")) {
            return true;
        }
        
        // HTML 파일들
        if (requestPath.endsWith(".html") || requestPath.equals("/")) {
            return true;
        }
        
        // 제외 경로들
        return EXCLUDED_PATHS.stream().anyMatch(requestPath::startsWith);
    }

    private void sendEmailVerificationRequiredResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, 
                "이메일 인증이 필요합니다. 가입 시 발송된 인증 이메일을 확인해 주세요."
        );
        problemDetail.setTitle("Email Verification Required");
        problemDetail.setProperty("errorCode", "EMAIL_VERIFICATION_REQUIRED");
        problemDetail.setProperty("resendUrl", ApiPaths.AUTH + "/resend-verification");

        String jsonResponse = objectMapper.writeValueAsString(problemDetail);
        response.getWriter().write(jsonResponse);
        
        log.warn("이메일 미인증 사용자 접근 차단: path={}", response);
    }
}