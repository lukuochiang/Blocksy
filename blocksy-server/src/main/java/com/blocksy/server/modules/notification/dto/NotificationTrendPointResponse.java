package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationTrendPointResponse(
        @Schema(description = "日期，yyyy-MM-dd")
        String day,
        @Schema(description = "发送总量")
        Long totalCount,
        @Schema(description = "已读量")
        Long readCount,
        @Schema(description = "已读率")
        Double readRate
) {
}
