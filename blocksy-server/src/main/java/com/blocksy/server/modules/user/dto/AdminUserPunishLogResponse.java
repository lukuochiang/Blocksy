package com.blocksy.server.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record AdminUserPunishLogResponse(
        @Schema(description = "日志 ID")
        Long id,
        @Schema(description = "用户 ID")
        Long userId,
        @Schema(description = "操作人 ID")
        Long operatorUserId,
        @Schema(description = "动作：BAN/UNBAN")
        String action,
        @Schema(description = "原因")
        String reason,
        @Schema(description = "封禁时长（小时）")
        Integer durationHours,
        @Schema(description = "封禁到期时间")
        OffsetDateTime expiresAt,
        @Schema(description = "创建时间")
        OffsetDateTime createdAt
) {
}
