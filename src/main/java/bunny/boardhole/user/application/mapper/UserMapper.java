package bunny.boardhole.user.application.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import bunny.boardhole.user.application.command.UpdateUserCommand;
import bunny.boardhole.user.application.event.UserCreatedEvent;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface UserMapper {
    UserResult toResult(User user);

    /**
     * 사용자 업데이트 - null이 아닌 필드만 업데이트
     *
     * @param command 업데이트 명령
     * @param user    업데이트할 사용자 엔티티
     */
    default void updateUserFromCommand(UpdateUserCommand command, User user) {
        if (command.name() != null)
            user.changeName(command.name());
        // 향후 다른 필드 업데이트가 필요할 때 여기에 추가
    }

    /**
     * 사용자 생성 이벤트 생성 - MapStruct가 자동으로 record 생성자에 매핑
     *
     * @param user              생성된 사용자
     * @param verificationToken 이메일 인증 토큰
     * @param expiresAt         토큰 만료 시간
     * @return UserCreatedEvent
     */
    UserCreatedEvent toUserCreatedEvent(User user, String verificationToken, LocalDateTime expiresAt);
}
