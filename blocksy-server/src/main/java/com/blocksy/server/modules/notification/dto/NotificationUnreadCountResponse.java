package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationUnreadCountResponse(
        @Schema(description = "未读通知数", example = "3")
        Long unreadCount
) {
}
