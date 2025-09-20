package dev.xiyo.bunnyholes.boardhole.shared.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@TestConfiguration
public class ValidationEnabledTestConfig {

    /**
     * AOP 기반 메서드 검증을 활성화하는 PostProcessor
     *
     * @return MethodValidationPostProcessor Bean
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}