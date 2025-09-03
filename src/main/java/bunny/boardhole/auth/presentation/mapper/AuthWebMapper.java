package bunny.boardhole.auth.presentation.mapper;

import bunny.boardhole.auth.application.command.LoginCommand;
import bunny.boardhole.auth.presentation.dto.LoginRequest;
import bunny.boardhole.shared.mapstruct.MapstructConfig;
import org.mapstruct.Mapper;


/**
 * 인증 웹 계층 매퍼
 * 인증 웹 DTO와 애플리케이션 Command 간 매핑을 담당합니다.
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
}
