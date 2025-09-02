package bunny.boardhole.board.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 게시글 제목 검증 애너테이션
 * - 필수값 검증
 * - 최대 200자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{board.validation.title.required}")
@Size(max = ValidationConstants.BOARD_TITLE_MAX_LENGTH, message = "{board.validation.title.size}")
@Constraint(validatedBy = {})
public @interface ValidBoardTitle {
    String message() default "{board.validation.title.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidBoardTitle[] value();
    }
}