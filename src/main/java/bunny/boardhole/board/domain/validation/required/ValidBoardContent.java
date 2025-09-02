package bunny.boardhole.board.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 게시글 내용 검증 애너테이션
 * - 필수값 검증
 * - 최대 10,000자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{board.validation.content.required}")
@Size(max = ValidationConstants.BOARD_CONTENT_MAX_LENGTH, message = "{board.validation.content.size}")
@Constraint(validatedBy = {})
public @interface ValidBoardContent {
    String message() default "{board.validation.content.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidBoardContent[] value();
    }
}