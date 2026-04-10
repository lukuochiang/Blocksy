package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record AdminUserResponse(
        @Schema(description = "用户 ID")
        Long id,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "昵称")
        String nickname,
        @Schema(description = "状态：1 正常，0 封禁")
        Integer status,
        @Schema(description = "封禁原因")
        String banReason,
        @Schema(description = "封禁到期时间，为空表示永久封禁")
        OffsetDateTime bannedUntil,
        @Schema(description = "封禁时间")
        OffsetDateTime bannedAt,
        @Schema(description = "创建时间")
        OffsetDateTime createdAt
) {
}
