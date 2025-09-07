package bunny.boardhole.shared.util;

import java.util.Optional;
import java.util.function.Supplier;

import bunny.boardhole.shared.exception.ResourceNotFoundException;

/**
 * Repository 공통 유틸리티
 * 자주 사용되는 findById().orElseThrow() 패턴을 추상화하여 코드 중복을 줄입니다.
 */
public final class RepositoryUtils {

    private RepositoryUtils() {
    }

    /**
     * Entity를 ID로 조회하고 없으면 ResourceNotFoundException을 던집니다.
     *
     * @param optional   Optional로 감싸진 Entity
     * @param entityType Entity 타입 (에러 메시지용)
     * @param id         조회할 ID
     * @param <T>        Entity 타입
     * @param <ID>       ID 타입
     * @return 조회된 Entity
     * @throws ResourceNotFoundException Entity를 찾을 수 없는 경우
     */
    public static <T, ID> T findByIdOrThrow(Optional<T> optional, String entityType, ID id) {
        return optional.orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error." + entityType.toLowerCase() + ".not-found.id", id)));
    }

    /**
     * Entity를 ID로 조회하고 없으면 사용자 정의 예외를 던집니다.
     *
     * @param optional          Optional로 감싸진 Entity
     * @param exceptionSupplier 예외 공급자
     * @param <T>               Entity 타입
     * @return 조회된 Entity
     * @throws RuntimeException 사용자 정의 예외
     */
    public static <T> T findByIdOrThrow(Optional<T> optional, Supplier<? extends RuntimeException> exceptionSupplier) {
        return optional.orElseThrow(exceptionSupplier);
    }
}