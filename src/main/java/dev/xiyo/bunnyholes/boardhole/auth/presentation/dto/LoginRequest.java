package dev.xiyo.bunnyholes.boardhole.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "로그인 요청 - boardholes 시스템 접근을 위한 인증")
public record LoginRequest(
        @NotBlank(message = "{validation.user.username.required}") @Schema(description = "사용자명 (이메일 형식)", example = "", requiredMode = Schema.RequiredMode.REQUIRED) String username,

        @NotBlank(message = "{validation.user.password.required}") @Schema(description = "비밀번호", example = "", requiredMode = Schema.RequiredMode.REQUIRED) String password) {
}
