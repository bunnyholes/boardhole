package bunny.boardhole.user.domain.validation.required;

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

import bunny.boardhole.user.domain.validation.UserValidationConstants;

/**
 * 비밀번호 검증 애너테이션
 * - 필수값 검증
 * - 8-100자 제한
 * - 영문 대문자, 소문자, 숫자, 특수문자 모두 포함 필수
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.user.password.required}")
@Size(min = UserValidationConstants.USER_PASSWORD_MIN_LENGTH, max = UserValidationConstants.USER_PASSWORD_MAX_LENGTH, message = "{validation.user.password.size}")
@Pattern(regexp = UserValidationConstants.PASSWORD_PATTERN, message = "{validation.user.password.pattern}")
@Constraint(validatedBy = {})
public @interface ValidPassword {
    String message() default "{validation.user.password.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidPassword[] value();
    }
}