package bunny.boardhole.user.domain.validation.optional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import bunny.boardhole.user.domain.validation.UserValidationConstants;

/**
 * 사용자 실명 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 1-50자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(min = UserValidationConstants.USER_NAME_MIN_LENGTH, max = UserValidationConstants.USER_NAME_MAX_LENGTH, message = "{validation.user.name.size}")
@Constraint(validatedBy = {})
public @interface OptionalName {
    String message() default "{validation.user.name.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalName[] value();
    }
}