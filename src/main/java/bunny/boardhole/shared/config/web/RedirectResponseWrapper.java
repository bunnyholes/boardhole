package bunny.boardhole.shared.config.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import lombok.Getter;

/**
 * HTTP 응답을 래핑하여 2xx 상태 코드 감지 및 리디렉션 플래그 관리
 */
public class RedirectResponseWrapper extends HttpServletResponseWrapper {

    /**
     * -- GETTER --
     * 리디렉션 URL 반환
     */
    @Getter
    private final String redirectUrl;
    private int statusCode = 200; // 기본값
    private boolean shouldRedirect = false;

    RedirectResponseWrapper(HttpServletResponse response, String redirectUrl) {
        super(response);
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void setStatus(int sc) {
        statusCode = sc;
        checkForRedirect();
        super.setStatus(sc);
    }

    @Override
    public void sendError(int sc) throws java.io.IOException {
        statusCode = sc;
        checkForRedirect();
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws java.io.IOException {
        statusCode = sc;
        checkForRedirect();
        super.sendError(sc, msg);
    }

    /**
     * 2xx 응답 상태 코드인지 확인하고 리디렉션 플래그 설정
     */
    private void checkForRedirect() {
        if (statusCode >= 200 && statusCode < 300)
            shouldRedirect = true;
    }

    /**
     * 리디렉션이 필요한지 여부 반환
     */
    boolean shouldRedirect() {
        return shouldRedirect;
    }

}