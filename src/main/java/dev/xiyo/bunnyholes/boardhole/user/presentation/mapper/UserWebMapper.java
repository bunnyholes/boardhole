package dev.xiyo.bunnyholes.boardhole.user.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import dev.xiyo.bunnyholes.boardhole.user.application.command.CreateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdatePasswordCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.PasswordUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserCreateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserResponse;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserUpdateRequest;

/**
 * 사용자 웹 계층 매퍼
 * 사용자 웹 DTO와 애플리케이션 Command/Result 간 매핑을 담당합니다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
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
    @Mapping(target = "username", source = "username")
    @Mapping(target = "name", source = "req.name")
    UpdateUserCommand toUpdateCommand(String username, UserUpdateRequest req);

    /**
     * 패스워드 변경 요청을 명령으로 변환
     *
     * @param id  사용자 ID
     * @param req 패스워드 변경 요청 DTO
     * @return 패스워드 변경 명령
     */
    @Mapping(target = "username", source = "username")
    @Mapping(target = "currentPassword", source = "req.currentPassword")
    @Mapping(target = "newPassword", source = "req.newPassword")
    @Mapping(target = "confirmPassword", source = "req.confirmPassword")
    UpdatePasswordCommand toUpdatePasswordCommand(String username, PasswordUpdateRequest req);

}
