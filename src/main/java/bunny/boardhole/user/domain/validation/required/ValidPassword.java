package bunny.boardhole.user.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

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
@Size(min = ValidationConstants.USER_PASSWORD_MIN_LENGTH, max = ValidationConstants.USER_PASSWORD_MAX_LENGTH, message = "{validation.user.password.size}")
@Pattern(
        regexp = ValidationConstants.PASSWORD_PATTERN,
        message = "{validation.user.password.pattern}"
)
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