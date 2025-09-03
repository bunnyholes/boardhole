package bunny.boardhole.user.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

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
@Size(max = ValidationConstants.USER_EMAIL_MAX_LENGTH, message = "{validation.user.email.size}")
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