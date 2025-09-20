package dev.xiyo.bunnyholes.boardhole.auth.application.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.LogoutCommand;
import dev.xiyo.bunnyholes.boardhole.auth.application.result.AuthResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

/**
 * 인증 애플리케이션 계층 매퍼
 * 도메인 객체와 애플리케이션 Result 간 매핑을 담당합니다.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface AuthMapper {

    /**
     * 사용자 도메인 객체를 인증 결과로 변환
     *
     * @param user 사용자 도메인 엔티티
     * @return 인증 결과 DTO
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "role", expression = "java(user.getRoles().iterator().next().name())")
    @Mapping(target = "authenticated", constant = "true")
    AuthResult toAuthResult(User user);

    /**
     * userId로 로그아웃 명령 생성
     *
     * @param userId 사용자 ID
     * @return 로그아웃 명령
     */
    LogoutCommand toLogoutCommand(UUID userId);
}