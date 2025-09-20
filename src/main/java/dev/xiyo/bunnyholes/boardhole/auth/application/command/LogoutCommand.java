package dev.xiyo.bunnyholes.boardhole.auth.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * 로그아웃 명령.
 */
public record LogoutCommand(@NotNull UUID userId) {
}

