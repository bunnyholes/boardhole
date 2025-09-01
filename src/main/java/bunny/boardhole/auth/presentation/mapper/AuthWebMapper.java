package bunny.boardhole.auth.presentation.mapper;

import bunny.boardhole.auth.application.command.LoginCommand;
import bunny.boardhole.auth.presentation.dto.*;
import bunny.boardhole.shared.mapstruct.MapstructConfig;
import bunny.boardhole.user.domain.User;
import org.mapstruct.*;


/**
 * 인증 웹 계층 매퍼
 * 인증 웹 DTO와 애플리케이션 Command/Query/Result 간 매핑을 담당합니다.
 */
@Mapper(config = MapstructConfig.class)
public interface AuthWebMapper {

    /**
     * 로그인 요청 DTO를 로그인 명령으로 변환
     *
     * @param request 로그인 요청 DTO
     * @return 로그인 명령
     */
    LoginCommand toLoginCommand(LoginRequest request);


    /**
     * 사용자 도메인 객체를 현재 사용자 응답으로 변환 (기존 호환성 유지)
     *
     * @param user 사용자 도메인 엔티티
     * @return 현재 사용자 응답 DTO
     */
    @Mapping(target = "userId", source = "id")
    CurrentUserResponse toCurrentUser(User user);
}
