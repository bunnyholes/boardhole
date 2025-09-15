package bunny.boardhole.shared.util;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

class MessageUtilsTest {

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        MessageUtils.setMessageSource(ms);
    }

    @Test
    void testDefaultLocale() {
        // 시스템 기본 로케일 확인
        System.out.println("System default locale: " + Locale.getDefault());
        System.out.println("LocaleContextHolder locale: " + LocaleContextHolder.getLocale());

        String message = MessageUtils.get("error.user.not-found.id", 1L);
        System.out.println("Message without setting locale: " + message);

        // 영어일 가능성이 높음
        assertThat(message).isNotNull();
    }

    @Test
    void testKoreanLocale() {
        // LocaleContextHolder를 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREAN);

        String message = MessageUtils.get("error.user.not-found.id", 1L);
        System.out.println("Message with Korean locale: " + message);

        assertThat(message).isEqualTo("사용자를 찾을 수 없습니다. ID: 1");
    }

    @Test
    void testEnglishLocale() {
        // LocaleContextHolder를 영어로 설정
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        String message = MessageUtils.get("error.user.not-found.id", 1L);
        System.out.println("Message with English locale: " + message);

        assertThat(message).isEqualTo("User not found. ID: 1");
    }
}
