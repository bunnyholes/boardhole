package dev.xiyo.bunnyholes.boardhole.user.presentation.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserProfileImageRequest", description = "사용자 프로필 이미지 업로드 요청")
public record UserProfileImageRequest(
        @Schema(description = "업로드할 프로필 이미지 파일", type = "string", format = "binary") MultipartFile profileImage,
        @Schema(description = "true일 경우 기존 프로필 이미지를 삭제", example = "false") Boolean remove) {

    public boolean removeFlag() {
        return Boolean.TRUE.equals(remove);
    }
}
