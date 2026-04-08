package com.blocksy.server.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "用户 ID", example = "1")
        Long userId,
        @Schema(description = "用户名", example = "demo")
        String username,
        @Schema(description = "JWT 访问令牌")
        String token
) {
}
