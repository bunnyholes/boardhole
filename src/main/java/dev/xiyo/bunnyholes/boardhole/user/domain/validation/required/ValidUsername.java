package dev.xiyo.bunnyholes.boardhole.user.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import dev.xiyo.bunnyholes.boardhole.user.domain.validation.UserValidationConstants;

/**
 * 사용자명(username) 검증 애너테이션
 * - 필수값 검증
 * - 3-20자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.user.username.required}")
@Size(min = UserValidationConstants.USER_USERNAME_MIN_LENGTH, max = UserValidationConstants.USER_USERNAME_MAX_LENGTH, message = "{validation.user.username.size}")
@Pattern(regexp = UserValidationConstants.USER_USERNAME_PATTERN, message = "{validation.user.username.pattern}")
@Constraint(validatedBy = {})
public @interface ValidUsername {
    String message() default "{validation.user.username.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidUsername[] value();
    }
}
