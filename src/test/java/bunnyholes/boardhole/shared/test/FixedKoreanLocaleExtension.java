package dev.xiyo.bunnyholes.boardhole.shared.test;

import java.util.Locale;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 테스트에서 메시지 문자열을 비교해야 하는 경우에만 사용한다.
 * 기본 애플리케이션 설정에는 로케일을 고정하지 않는다.
 */
public class FixedKoreanLocaleExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        LocaleContextHolder.setLocale(Locale.KOREAN);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        LocaleContextHolder.resetLocaleContext();
    }
}

