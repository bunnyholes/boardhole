package bunny.boardhole.common.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 국제화(i18n) 설정
 * 다국어 메시지 소스, 로케일 해결자, 언어 변경 기능을 담당합니다.
 */
@Configuration
@Schema(name = "InternationalizationConfig", description = "Spring Boot i18n 국제화 설정 - 다국어 지원 및 로케일 관리")
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * 메시지 소스 빈 설정
     * @return 다국어 메시지 소스
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
            "classpath:messages",
            "classpath:ValidationMessages"
        );
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.KOREAN);
        messageSource.setCacheSeconds(300); // 5분 캐시
        messageSource.setFallbackToSystemLocale(true);
        return messageSource;
    }

    /**
     * 로케일 해결자 빈 설정
     * @return 세션 기반 로케일 해결자
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREAN);
        return localeResolver;
    }

    /**
     * 로케일 변경 인터셉터 빈 설정
     * @return 언어 변경 인터셉터
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=en 으로 언어 변경 가능
        return interceptor;
    }

    /**
     * 인터셉터 등록
     * @param registry 인터셉터 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
    
}