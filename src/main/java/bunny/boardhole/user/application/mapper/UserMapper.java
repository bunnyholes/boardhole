package bunny.boardhole.user.application.mapper;

import bunny.boardhole.shared.mapstruct.MapstructConfig;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.User;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfig.class)
public interface UserMapper {
    UserResult toResult(User user);
}
