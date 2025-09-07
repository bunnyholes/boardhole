package bunny.boardhole.shared.domain.listener;

import java.util.Set;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * JPA Entity Listener for automatic Bean Validation
 * 엔티티가 저장되거나 업데이트되기 전에 자동으로 검증을 수행합니다.
 */
public class ValidationListener {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

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