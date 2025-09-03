package bunny.boardhole.user.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 사용자 실명 검증 애너테이션
 * - 필수값 검증
 * - 1-50자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.user.name.required}")
@Size(min = ValidationConstants.USER_NAME_MIN_LENGTH, max = ValidationConstants.USER_NAME_MAX_LENGTH, message = "{validation.user.name.size}")
@Constraint(validatedBy = {})
public @interface ValidName {
    String message() default "{validation.user.name.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidName[] value();
    }
}