package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NotificationStatsResponse(
        @Schema(description = "通知总量")
        Long totalCount,
        @Schema(description = "已读总量")
        Long readCount,
        @Schema(description = "未读总量")
        Long unreadCount,
        @Schema(description = "今日新增")
        Long todayCount,
        @Schema(description = "已读率(0~1)")
        Double readRate,
        @Schema(description = "按类型统计")
        List<NotificationTypeStatItem> typeStats
) {
}
