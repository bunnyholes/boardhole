package bunny.boardhole.user.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import bunny.boardhole.email.presentation.dto.EmailUpdateRequest;
import bunny.boardhole.email.presentation.dto.EmailVerificationRequest;
import bunny.boardhole.shared.mapstruct.MapstructConfig;
import bunny.boardhole.user.application.command.CreateUserCommand;
import bunny.boardhole.user.application.command.RequestEmailVerificationCommand;
import bunny.boardhole.user.application.command.UpdateEmailCommand;
import bunny.boardhole.user.application.command.UpdatePasswordCommand;
import bunny.boardhole.user.application.command.UpdateUserCommand;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.presentation.dto.PasswordUpdateRequest;
import bunny.boardhole.user.presentation.dto.UserCreateRequest;
import bunny.boardhole.user.presentation.dto.UserResponse;
import bunny.boardhole.user.presentation.dto.UserUpdateRequest;

/**
 * 사용자 웹 계층 매퍼
 * 사용자 웹 DTO와 애플리케이션 Command/Result 간 매핑을 담당합니다.
 */
@Mapper(config = MapstructConfig.class)
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
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "name", source = "req.name")
    UpdateUserCommand toUpdateCommand(Long id, UserUpdateRequest req);

    /**
     * 패스워드 변경 요청을 명령으로 변환
     *
     * @param id  사용자 ID
     * @param req 패스워드 변경 요청 DTO
     * @return 패스워드 변경 명령
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "currentPassword", source = "req.currentPassword")
    @Mapping(target = "newPassword", source = "req.newPassword")
    UpdatePasswordCommand toUpdatePasswordCommand(Long id, PasswordUpdateRequest req);

    /**
     * 이메일 검증 요청을 명령으로 변환
     *
     * @param id  사용자 ID
     * @param req 이메일 검증 요청 DTO
     * @return 이메일 검증 요청 명령
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "currentPassword", source = "req.currentPassword")
    @Mapping(target = "newEmail", source = "req.newEmail")
    RequestEmailVerificationCommand toRequestEmailVerificationCommand(Long id, EmailVerificationRequest req);

    /**
     * 이메일 변경 요청을 명령으로 변환
     *
     * @param id  사용자 ID
     * @param req 이메일 변경 요청 DTO
     * @return 이메일 변경 명령
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "verificationCode", source = "req.verificationCode")
    UpdateEmailCommand toUpdateEmailCommand(Long id, EmailUpdateRequest req);

}
