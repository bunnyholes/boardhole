package bunny.boardhole.user.presentation.mapper;

import bunny.boardhole.shared.mapstruct.MapstructConfig;
import bunny.boardhole.user.application.command.*;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.presentation.dto.*;
import org.mapstruct.*;

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
     * @param request 사용자 생성 요청 DTO
     * @return 사용자 생성 명령
     */
    @Mapping(target = "username", source = "request.username") // 사용자명 매핑
    @Mapping(target = "password", source = "request.password") // 비밀번호 매핑
    @Mapping(target = "name", source = "request.name") // 이름 매핑
    @Mapping(target = "email", source = "request.email") // 이메일 매핑
    CreateUserCommand toCreateCommand(UserCreateRequest request);

    /**
     * 사용자 수정 요청을 명령으로 변환
     *
     * @param identifier  수정할 사용자 ID
     * @param request 사용자 수정 요청 DTO
     * @return 사용자 수정 명령
     */
    @Mapping(target = "userId", source = "identifier") // 사용자 ID 매핑
    @Mapping(target = "name", source = "request.name") // 이름 매핑
    UpdateUserCommand toUpdateCommand(Long identifier, UserUpdateRequest request);

    /**
     * 패스워드 변경 요청을 명령으로 변환
     *
     * @param identifier  사용자 ID
     * @param request 패스워드 변경 요청 DTO
     * @return 패스워드 변경 명령
     */
    @Mapping(target = "userId", source = "identifier") // 사용자 ID 매핑
    @Mapping(target = "currentPassword", source = "request.currentPassword") // 현재 비밀번호 매핑
    @Mapping(target = "newPassword", source = "request.newPassword") // 새 비밀번호 매핑
    UpdatePasswordCommand toUpdatePasswordCommand(Long identifier, PasswordUpdateRequest request);

    /**
     * 이메일 검증 요청을 명령으로 변환
     *
     * @param identifier  사용자 ID
     * @param request 이메일 검증 요청 DTO
     * @return 이메일 검증 요청 명령
     */
    @Mapping(target = "userId", source = "identifier") // 사용자 ID 매핑
    @Mapping(target = "currentPassword", source = "request.currentPassword") // 현재 비밀번호 매핑
    @Mapping(target = "newEmail", source = "request.newEmail") // 새 이메일 매핑
    RequestEmailVerificationCommand toRequestEmailVerificationCommand(Long identifier, EmailVerificationRequest request);

    /**
     * 이메일 변경 요청을 명령으로 변환
     *
     * @param identifier  사용자 ID
     * @param request 이메일 변경 요청 DTO
     * @return 이메일 변경 명령
     */
    @Mapping(target = "userId", source = "identifier") // 사용자 ID 매핑
    @Mapping(target = "verificationCode", source = "request.verificationCode") // 검증 코드 매핑
    UpdateEmailCommand toUpdateEmailCommand(Long identifier, EmailUpdateRequest request);
}
