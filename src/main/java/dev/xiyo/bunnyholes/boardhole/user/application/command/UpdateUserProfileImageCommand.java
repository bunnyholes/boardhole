package dev.xiyo.bunnyholes.boardhole.user.application.command;

import org.springframework.web.multipart.MultipartFile;

public record UpdateUserProfileImageCommand(String username, MultipartFile image, boolean remove) {
}
