package bunny.boardhole.shared.validation;

import java.lang.reflect.Method;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConfirmedValidator implements ConstraintValidator<PasswordConfirmed, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        try {
            Method newPwd = value.getClass().getMethod("newPassword");
            Method confirm = value.getClass().getMethod("confirmPassword");
            Object n = newPwd.invoke(value);
            Object c = confirm.invoke(value);
            if (n == null || c == null)
                return true; // 개별 @NotBlank/@ValidPassword가 처리
            return n.equals(c);
        } catch (ReflectiveOperationException e) {
            // 대상 타입이 요구 메서드를 제공하지 않으면 해당 검증은 건너뜀
            return true;
        }
    }
}

