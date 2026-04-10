package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NotificationPageResponse(
        @Schema(description = "当前页")
        Integer page,
        @Schema(description = "每页大小")
        Integer pageSize,
        @Schema(description = "总数")
        Long total,
        @Schema(description = "列表数据")
        List<NotificationResponse> items
) {
}
