package dev.xiyo.bunnyholes.boardhole.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 주어진 필드들이 모두 서로 같은지 검증합니다.
 * 레코드/빈에서 무인자 접근자 메서드(필드명과 동일)를 통해 값을 읽어 비교합니다.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = FieldsMatchValidator.class)
public @interface FieldsMatch {
    String message() default "{validation.fields.match.mismatch}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 서로 동일해야 하는 필드명 목록 (2개 이상 권장)
     */
    String[] fields();
}

