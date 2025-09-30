package dev.xiyo.bunnyholes.boardhole.user.application.result;

public record UserProfileImageResult(byte[] data, String contentType, long size) {
}
