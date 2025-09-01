package bunny.boardhole.user.presentation.mapper;

import bunny.boardhole.shared.mapstruct.MapstructConfig;
import bunny.boardhole.user.application.command.CreateUserCommand;
import bunny.boardhole.user.application.command.UpdateUserCommand;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.presentation.dto.UserCreateRequest;
import bunny.boardhole.user.presentation.dto.UserResponse;
import bunny.boardhole.user.presentation.dto.UserUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 사용자 웹 계층 매퍼
 * 사용자 웹 DTO와 애플리케이션 Command/Result 간 매핑을 담당합니다.
 */
@Mapper(config = MapstructConfig.class)
public interface UserWebMapper {

    /**
     * 사용자 결과를 웹 응답으로 변환
     *
     * @param result 사용자 조회 결과
     * @return 웹 응답 DTO
     */
    UserResponse toResponse(UserResult result);

    /**
     * 사용자 생성 요청을 명령으로 변환
     *
     * @param req 사용자 생성 요청 DTO
     * @return 사용자 생성 명령
     */
    @Mapping(target = "username", source = "req.username")
    @Mapping(target = "password", source = "req.password")
    @Mapping(target = "name", source = "req.name")
    @Mapping(target = "email", source = "req.email")
    CreateUserCommand toCreateCommand(UserCreateRequest req);

    /**
     * 사용자 수정 요청을 명령으로 변환
     *
     * @param id  수정할 사용자 ID
     * @param req 사용자 수정 요청 DTO
     * @return 사용자 수정 명령
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "name", source = "req.name")
    @Mapping(target = "email", source = "req.email")
    @Mapping(target = "password", source = "req.password")
    UpdateUserCommand toUpdateCommand(Long id, UserUpdateRequest req);
}
