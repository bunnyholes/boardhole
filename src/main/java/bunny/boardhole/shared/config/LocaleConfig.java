package bunny.boardhole.shared.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

/**
 * 국제화(i18n) 설정
 * 시스템 로케일에 관계없이 애플리케이션의 기본 로케일을 한국어로 고정
 */
@Configuration
public class LocaleConfig implements WebMvcConfigurer {

    /**
     * 고정 로케일 리졸버를 사용하여 항상 한국어를 기본 로케일로 사용
     * 시스템 로케일(en_KR 같은 이상한 설정)의 영향을 받지 않음
     */
    @Bean
    public LocaleResolver localeResolver() {
        FixedLocaleResolver resolver = new FixedLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);  // 한국어로 고정
        return resolver;
    }
}