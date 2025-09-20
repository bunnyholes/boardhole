package dev.xiyo.bunnyholes.boardhole.auth.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.LoginCommand;
import dev.xiyo.bunnyholes.boardhole.auth.presentation.dto.LoginRequest;

/**
 * 인증 웹 계층 매퍼
 * 인증 웹 DTO와 애플리케이션 Command 간 매핑을 담당합니다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface AuthWebMapper {

    /**
     * 로그인 요청 DTO를 로그인 명령으로 변환
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 명령
     */
    LoginCommand toLoginCommand(LoginRequest request);
}
