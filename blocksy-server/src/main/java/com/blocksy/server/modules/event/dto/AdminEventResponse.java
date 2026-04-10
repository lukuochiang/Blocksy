package com.blocksy.server.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminEventResponse(
        @Schema(description = "活动 ID", example = "2001")
        Long id,
        @Schema(description = "发布人 ID", example = "1")
        Long userId,
        @Schema(description = "社区 ID", example = "1")
        Long communityId,
        @Schema(description = "活动标题")
        String title,
        @Schema(description = "活动内容")
        String content,
        @Schema(description = "开始时间")
        LocalDateTime startTime,
        @Schema(description = "状态，1=正常，0=下架")
        Integer status,
        @Schema(description = "创建时间")
        LocalDateTime createdAt,
        @Schema(description = "更新时间")
        LocalDateTime updatedAt
) {
}
