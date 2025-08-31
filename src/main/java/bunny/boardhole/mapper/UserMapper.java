package bunny.boardhole.mapper;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.user.UserDto;
import bunny.boardhole.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    UserDto toDto(User user);
    
    UserResponse toResponse(User user);
}