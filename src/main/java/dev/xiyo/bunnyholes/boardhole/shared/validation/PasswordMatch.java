package dev.xiyo.bunnyholes.boardhole.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 비밀번호와 비밀번호 확인이 일치하는지 검증합니다.
 * 클래스 레벨 검증이지만 confirmPassword 필드의 에러로 등록됩니다.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = PasswordMatchValidator.class)
public @interface PasswordMatch {
    String message() default "{validation.field.match.mismatch}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}