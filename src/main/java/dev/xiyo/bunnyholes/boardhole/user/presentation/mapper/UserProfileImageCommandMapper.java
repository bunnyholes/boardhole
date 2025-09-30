package dev.xiyo.bunnyholes.boardhole.user.presentation.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdateUserProfileImageCommand;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserProfileImageRequest;

@Component
public class UserProfileImageCommandMapper {

    public UpdateUserProfileImageCommand toCommand(String username, UserProfileImageRequest request) {
        Assert.hasText(username, "username must not be blank");
        MultipartFile image = request != null ? request.profileImage() : null;
        boolean remove = request != null && request.removeFlag();
        return new UpdateUserProfileImageCommand(username, image, remove);
    }
}
