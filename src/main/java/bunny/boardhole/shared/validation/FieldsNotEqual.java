package bunny.boardhole.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 주어진 두 필드가 서로 다른지 검증합니다.
 * 정확히 2개의 필드명을 제공하는 것을 권장합니다.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FieldsNotEqualValidator.class)
public @interface FieldsNotEqual {
    String message() default "{validation.fields.not_equal.mismatch}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 서로 달라야 하는 필드명 목록 (2개 권장)
     */
    String[] fields();
}

