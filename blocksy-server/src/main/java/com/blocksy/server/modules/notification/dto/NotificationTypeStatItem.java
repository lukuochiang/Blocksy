package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationTypeStatItem(
        @Schema(description = "通知类型")
        String type,
        @Schema(description = "总数")
        Long totalCount,
        @Schema(description = "已读数")
        Long readCount,
        @Schema(description = "未读数")
        Long unreadCount
) {
}
