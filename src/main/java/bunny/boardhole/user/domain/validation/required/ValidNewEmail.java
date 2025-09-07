package bunny.boardhole.user.domain.validation.required;

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

/**
 * 새 이메일 주소 검증을 위한 커스텀 validation 애너테이션
 */
@NotBlank(message = "{validation.email.verification.newEmail.required}")
@Email(message = "{validation.email.verification.newEmail.format}")
@Size(max = 320, message = "{validation.email.verification.newEmail.size}")
@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNewEmail {
    String message() default "{validation.email.verification.newEmail.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}