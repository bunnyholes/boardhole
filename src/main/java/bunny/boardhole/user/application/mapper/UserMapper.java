package bunny.boardhole.user.application.mapper;

import bunny.boardhole.shared.mapstruct.MapstructConfig;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 사용자 애플리케이션 계층 매퍼
 * 사용자 도메인 엔티티와 애플리케이션 Result 간 매핑을 담당합니다.
 */
@Mapper(config = MapstructConfig.class)
public interface UserMapper {
    /**
     * User entity를 UserResult로 변환
     *
     * @param userEntity 사용자 엔티티
     * @return 사용자 결과 DTO
     */
    UserResult toResult(User userEntity);
}
