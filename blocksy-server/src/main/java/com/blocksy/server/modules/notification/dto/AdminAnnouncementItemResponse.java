package com.blocksy.server.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminAnnouncementItemResponse(
        @Schema(description = "公告 ID")
        Long id,
        @Schema(description = "标题")
        String title,
        @Schema(description = "内容")
        String content,
        @Schema(description = "状态，1=有效，0=已撤回")
        Integer status,
        @Schema(description = "总下发次数")
        Integer dispatchCount,
        @Schema(description = "最近下发时间")
        LocalDateTime lastDispatchedAt,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}
