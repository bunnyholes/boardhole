package dev.xiyo.bunnyholes.boardhole.user.domain.validation.optional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.UserValidationConstants;

/**
 * 이메일 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 이메일 형식 검증
 * - 값이 있을 경우 최대 255자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Email(message = "{validation.user.email.format}")
@Size(max = UserValidationConstants.USER_EMAIL_MAX_LENGTH, message = "{validation.user.email.size}")
@Constraint(validatedBy = {})
public @interface OptionalEmail {
    String message() default "{validation.user.email.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalEmail[] value();
    }
}