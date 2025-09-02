package bunny.boardhole.user.domain.validation;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 사용자명(username) 검증 애너테이션
 * - 필수값 검증
 * - 3-20자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{user.validation.username.required}")
@Size(min = 3, max = 20, message = "{user.validation.username.size}")
@Constraint(validatedBy = {})
public @interface ValidUsername {
    String message() default "{user.validation.username.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidUsername[] value();
    }
}