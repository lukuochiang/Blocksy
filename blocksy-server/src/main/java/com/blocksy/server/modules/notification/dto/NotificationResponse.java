package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(description = "通知 ID", example = "9001")
        Long id,
        @Schema(description = "通知类型", example = "COMMENT")
        String type,
        @Schema(description = "标题")
        String title,
        @Schema(description = "内容")
        String content,
        @Schema(description = "是否已读")
        Boolean isRead,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}
