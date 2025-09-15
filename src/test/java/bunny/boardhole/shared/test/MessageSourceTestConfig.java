package bunny.boardhole.shared.test;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

import bunny.boardhole.shared.util.MessageUtils;

@TestConfiguration
public class MessageSourceTestConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        return ms;
    }

    @Bean
    public InitializingBean messageUtilsInitializer(MessageSource messageSource) {
        return () -> MessageUtils.setMessageSource(messageSource);
    }
}

