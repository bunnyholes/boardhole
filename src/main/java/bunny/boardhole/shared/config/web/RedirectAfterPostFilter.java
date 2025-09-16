package bunny.boardhole.shared.config.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API 요청에서 redirect 쿼리 파라미터가 있을 때
 * 2xx 성공 응답을 302 리디렉션으로 자동 변환하는 필터
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RedirectAfterPostFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // /api/* 경로만 처리
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // redirect 쿼리 파라미터 확인
        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // ResponseWrapper로 응답 래핑
        RedirectResponseWrapper wrappedResponse = new RedirectResponseWrapper(response, redirectUrl);
        
        try {
            filterChain.doFilter(request, wrappedResponse);
            
            // 응답이 2xx이고 아직 커밋되지 않았다면 리디렉션 처리
            if (wrappedResponse.shouldRedirect() && !response.isCommitted()) {
                log.debug("Redirecting after successful API call: {} -> {}", request.getRequestURI(), redirectUrl);
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            log.error("Error in RedirectAfterPostFilter", e);
            throw e;
        }
    }
}