package bunny.boardhole.shared.config;

import jakarta.validation.Validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import bunny.boardhole.shared.domain.listener.ValidationListener;

/**
 * Bean Validation 구성
 * - Spring MessageSource를 사용하는 LocalValidatorFactoryBean을 명시적으로 구성
 * - JPA EntityListener가 동일한 Validator를 사용하도록 연결
 */
@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setValidationMessageSource(messageSource);
        return factoryBean;
    }

    @Bean
    public InitializingBean wireEntityValidation(Validator validator) {
        return () -> ValidationListener.setValidator(validator);
    }
}

