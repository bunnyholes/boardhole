package bunny.boardhole.shared.domain.listener;

import java.util.Set;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * JPA Entity Listener for automatic Bean Validation
 * 엔티티가 저장되거나 업데이트되기 전에 자동으로 검증을 수행합니다.
 *
 * 스프링 컨텍스트의 Validator(LocalValidatorFactoryBean)를 사용하도록
 * 외부에서 setValidator로 주입할 수 있으며, 주입되지 않은 경우
 * 기본 Validator(표준)로 폴백합니다.
 */
public class ValidationListener {

    private static volatile Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static void setValidator(Validator validator) {
        if (validator != null)
            VALIDATOR = validator;
    }

    /**
     * 엔티티 저장 전 검증
     */
    @PrePersist
    public static void validateBeforePersist(Object entity) {
        validate(entity);
    }

    /**
     * 엔티티 업데이트 전 검증
     */
    @PreUpdate
    public static void validateBeforeUpdate(Object entity) {
        validate(entity);
    }

    /**
     * 실제 검증 수행
     */
    private static void validate(Object entity) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(entity);

        // 모든 검증 오류를 포함하여 예외 발생
        if (!violations.isEmpty())
            throw new ConstraintViolationException(violations);
    }
}
