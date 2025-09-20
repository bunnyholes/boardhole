package dev.xiyo.bunnyholes.boardhole.user.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.UserValidationConstants;

/**
 * 이메일 검증 애너테이션
 * - 필수값 검증
 * - 이메일 형식 검증
 * - 최대 255자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.user.email.required}")
@Email(message = "{validation.user.email.format}")
@Size(max = UserValidationConstants.USER_EMAIL_MAX_LENGTH, message = "{validation.user.email.size}")
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "{validation.user.email.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidEmail[] value();
    }
}