package bunny.boardhole.board.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 게시글 제목 필수 검증을 위한 커스텀 어노테이션입니다.
 * 
 * <p>다음 조건들을 만족해야 합니다:
 * <ul>
 *   <li>필수값 검증: null, 빈 문자열, 공백 문자만 포함된 문자열 허용하지 않음</li>
 *   <li>길이 제한: 최대 200자까지 허용</li>
 * </ul>
 * 
 * @see ValidationConstants#BOARD_TITLE_MAX_LENGTH
 * @since 1.0
 * @author BoardHole Development Team
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{board.validation.title.required}")
@Size(max = ValidationConstants.BOARD_TITLE_MAX_LENGTH, message = "{board.validation.title.size}")
@Constraint(validatedBy = {})
public @interface ValidBoardTitle {
    /**
     * 검증 실패 시 반환될 메시지를 정의합니다.
     * @return 검증 오류 메시지 (기본값: "{board.validation.title.invalid}")
     */
    String message() default "{board.validation.title.invalid}";

    /**
     * 검증 그룹을 정의합니다.
     * @return 검증 그룹 배열 (기본값: 빈 배열)
     */
    Class<?>[] groups() default {};

    /**
     * 검증 페이로드를 정의합니다.
     * @return 페이로드 타입 배열 (기본값: 빈 배열)
     */
    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /**
         * 동일한 요소에 여러 ValidBoardTitle 어노테이션을 적용할 때 사용됩니다.
         * @return ValidBoardTitle 어노테이션 배열
         */
        ValidBoardTitle[] value();
    }
}