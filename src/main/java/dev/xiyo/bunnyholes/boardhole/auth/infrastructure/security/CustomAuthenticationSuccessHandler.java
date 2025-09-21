package dev.xiyo.bunnyholes.boardhole.auth.infrastructure.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;

/**
 * 커스텀 인증 성공 핸들러
 * Spring Security Form 로그인 성공 시 최근 로그인 시간을 업데이트합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    private final UserCommandService userCommandService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        
        try {
            // 최근 로그인 시간 업데이트
            userCommandService.updateLastLogin(username);
            log.info("Login succeeded, last login timestamp updated for user: {}", username);
        } catch (Exception e) {
            // 로그인 시간 업데이트 실패해도 로그인은 성공으로 처리
            log.error("Failed to update last login timestamp for user: {}", username, e);
        }
        
        // 기본 동작 수행 (SavedRequest가 있으면 해당 URL로, 없으면 defaultSuccessUrl로 리다이렉트)
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
