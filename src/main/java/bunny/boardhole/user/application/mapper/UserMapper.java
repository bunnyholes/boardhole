package bunny.boardhole.user.application.mapper;

import bunny.boardhole.common.mapstruct.MapstructConfig;
import bunny.boardhole.user.application.dto.UserResult;
import bunny.boardhole.user.domain.User;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface UserMapper {
    UserResult toResult(User user);
}
